package nl.andrewlalis.gymboard_api.util.sample_data;

import nl.andrewlalis.gymboard_api.domains.api.dao.ExerciseRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.domains.submission.dao.SubmissionRepository;
import nl.andrewlalis.gymboard_api.domains.api.model.Exercise;
import nl.andrewlalis.gymboard_api.domains.api.model.Gym;
import nl.andrewlalis.gymboard_api.domains.api.model.WeightUnit;
import nl.andrewlalis.gymboard_api.domains.submission.model.Submission;
import nl.andrewlalis.gymboard_api.domains.api.service.cdn_client.CdnClient;
import nl.andrewlalis.gymboard_api.domains.api.service.cdn_client.UploadsClient;
import nl.andrewlalis.gymboard_api.domains.auth.dao.UserRepository;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.domains.submission.model.SubmissionProperties;
import nl.andrewlalis.gymboard_api.util.ULID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Profile("development")
public class SampleSubmissionGenerator implements SampleDataGenerator {
	private static final Logger log = LoggerFactory.getLogger(SampleSubmissionGenerator.class);

	private final GymRepository gymRepository;
	private final UserRepository userRepository;
	private final ExerciseRepository exerciseRepository;
	private final SubmissionRepository submissionRepository;
	private final ULID ulid;
	private final CdnClient cdnClient;

	public SampleSubmissionGenerator(GymRepository gymRepository, UserRepository userRepository, ExerciseRepository exerciseRepository, SubmissionRepository submissionRepository, ULID ulid, CdnClient cdnClient) {
		this.gymRepository = gymRepository;
		this.userRepository = userRepository;
		this.exerciseRepository = exerciseRepository;
		this.submissionRepository = submissionRepository;
		this.ulid = ulid;
		this.cdnClient = cdnClient;
	}

	@Override
	public void generate() throws Exception {
		// First we generate a small set of uploaded files that all the
		// submissions can link to, instead of having them all upload new content.
		var uploads = generateUploads();

		// Now that uploads are complete, we can proceed with generating the submissions.
		List<Gym> gyms = gymRepository.findAll();
		List<User> users = userRepository.findAll();
		List<Exercise> exercises = exerciseRepository.findAll();

		final int count = 10000;
		final LocalDateTime earliestSubmission = LocalDateTime.now().minusYears(3);
		final LocalDateTime latestSubmission = LocalDateTime.now();

		Random random = new Random(1);
		List<Submission> submissions = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			Submission submission = generateRandomSubmission(
					gyms,
					users,
					exercises,
					uploads,
					earliestSubmission,
					latestSubmission,
					random
			);
			submissions.add(submission);
		}
		submissionRepository.saveAll(submissions);
	}

	private Submission generateRandomSubmission(
			List<Gym> gyms,
			List<User> users,
			List<Exercise> exercises,
			Map<Long, Pair<String, String>> uploads,
			LocalDateTime earliestSubmission,
			LocalDateTime latestSubmission,
			Random random
	) {
		LocalDateTime time = randomTime(earliestSubmission, latestSubmission, random);
		BigDecimal metricWeight = new BigDecimal(random.nextInt(20, 250));
		BigDecimal rawWeight = new BigDecimal(metricWeight.toString());
		WeightUnit weightUnit = WeightUnit.KILOGRAMS;
		if (random.nextDouble() > 0.5) {
			weightUnit = WeightUnit.POUNDS;
			rawWeight = metricWeight.multiply(new BigDecimal("2.2046226218"));
		}
		SubmissionProperties properties = new SubmissionProperties(
				randomChoice(exercises, random),
				time,
				rawWeight,
				weightUnit,
				random.nextInt(13) + 1
		);

		var submission = new Submission(
			ulid.nextULID(),
			randomChoice(gyms, random),
			randomChoice(users, random),
			randomChoice(new ArrayList<>(uploads.keySet()), random),
			properties
		);
		submission.setVerified(true);
		submission.setProcessing(false);
		var uploadData = uploads.get(submission.getVideoProcessingTaskId());
		submission.setVideoFileId(uploadData.getFirst());
		submission.setThumbnailFileId(uploadData.getSecond());
		return submission;
	}

	@Override
	public Collection<Class<? extends SampleDataGenerator>> dependencies() {
		return Set.of(SampleExerciseGenerator.class, SampleUserGenerator.class, SampleGymGenerator.class);
	}

	private <T> T randomChoice(List<T> items, Random rand) {
		return items.get(rand.nextInt(items.size()));
	}

	private LocalDateTime randomTime(LocalDateTime start, LocalDateTime end, Random rand) {
		Duration dur = Duration.between(start, end);
		return start.plusSeconds(rand.nextLong(dur.toSeconds() + 1));
	}

	/**
	 * Generates a set of sample video uploads to use for all the sample
	 * submissions.
	 * @return A map containing keys representing video processing task ids, and
	 * values being a pair of video and thumbnail file ids.
	 * @throws Exception If an error occurs.
	 */
	private Map<Long, Pair<String, String>> generateUploads() throws Exception {
		List<Long> taskIds = new ArrayList<>();
		taskIds.add(cdnClient.uploads.uploadVideo(Path.of("sample_data", "sample_video_curl.mp4"), "video/mp4"));
		taskIds.add(cdnClient.uploads.uploadVideo(Path.of("sample_data", "sample_video_ohp.mp4"), "video/mp4"));

		Map<Long, UploadsClient.VideoProcessingTaskStatusResponse> taskStatus = new HashMap<>();
		for (long taskId : taskIds) {
			cdnClient.uploads.startTask(taskId);
			taskStatus.put(taskId, cdnClient.uploads.getVideoProcessingTaskStatus(taskId));
		}

		// Wait for all video uploads to complete.
		while (
				taskStatus.values().stream()
						.map(UploadsClient.VideoProcessingTaskStatusResponse::status)
						.anyMatch(status -> !List.of("COMPLETED", "FAILED").contains(status.toUpperCase()))
		) {
			log.info("Waiting for sample video upload tasks to finish...");
			Thread.sleep(1000);
			for (long taskId : taskIds) taskStatus.put(taskId, cdnClient.uploads.getVideoProcessingTaskStatus(taskId));
		}

		// If any upload failed, throw an exception and cancel this generator.
		if (taskStatus.values().stream().anyMatch(r -> r.status().equalsIgnoreCase("FAILED"))) {
			throw new IOException("Video upload task processing failed.");
		}

		// Prepare the final data structure.
		Map<Long, Pair<String, String>> finalResults = new HashMap<>();
		for (var entry : taskStatus.entrySet()) {
			finalResults.put(entry.getKey(), Pair.of(entry.getValue().videoFileId(), entry.getValue().thumbnailFileId()));
		}
		return finalResults;
	}
}
