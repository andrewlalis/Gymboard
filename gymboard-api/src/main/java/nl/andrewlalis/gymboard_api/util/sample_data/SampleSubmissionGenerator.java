package nl.andrewlalis.gymboard_api.util.sample_data;

import nl.andrewlalis.gymboard_api.domains.api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.exercise.ExerciseRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.exercise.ExerciseSubmissionRepository;
import nl.andrewlalis.gymboard_api.domains.api.model.Gym;
import nl.andrewlalis.gymboard_api.domains.api.model.WeightUnit;
import nl.andrewlalis.gymboard_api.domains.api.model.exercise.Exercise;
import nl.andrewlalis.gymboard_api.domains.api.model.exercise.ExerciseSubmission;
import nl.andrewlalis.gymboard_api.domains.api.service.cdn_client.CdnClient;
import nl.andrewlalis.gymboard_api.domains.api.service.submission.ExerciseSubmissionService;
import nl.andrewlalis.gymboard_api.domains.auth.dao.UserRepository;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.util.ULID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Profile("development")
public class SampleSubmissionGenerator implements SampleDataGenerator {
	private final GymRepository gymRepository;
	private final UserRepository userRepository;
	private final ExerciseRepository exerciseRepository;
	private final ExerciseSubmissionService submissionService;
	private final ExerciseSubmissionRepository submissionRepository;
	private final ULID ulid;

	@Value("${app.cdn-origin}")
	private String cdnOrigin;

	public SampleSubmissionGenerator(GymRepository gymRepository, UserRepository userRepository, ExerciseRepository exerciseRepository, ExerciseSubmissionService submissionService, ExerciseSubmissionRepository submissionRepository, ULID ulid) {
		this.gymRepository = gymRepository;
		this.userRepository = userRepository;
		this.exerciseRepository = exerciseRepository;
		this.submissionService = submissionService;
		this.submissionRepository = submissionRepository;
		this.ulid = ulid;
	}

	@Override
	public void generate() throws Exception {
		final CdnClient cdnClient = new CdnClient(cdnOrigin);

		List<String> videoIds = new ArrayList<>();
		var video1 = cdnClient.uploads.uploadVideo(Path.of("sample_data", "sample_video_curl.mp4"), "video/mp4");
		var video2 = cdnClient.uploads.uploadVideo(Path.of("sample_data", "sample_video_ohp.mp4"), "video/mp4");
		videoIds.add(video1.id());
		videoIds.add(video2.id());

		List<Gym> gyms = gymRepository.findAll();
		List<User> users = userRepository.findAll();
		List<Exercise> exercises = exerciseRepository.findAll();

		final int count = 10000;
		final LocalDateTime earliestSubmission = LocalDateTime.now().minusYears(3);
		final LocalDateTime latestSubmission = LocalDateTime.now();

		Random random = new Random(1);
		for (int i = 0; i < count; i++) {
			generateRandomSubmission(
					gyms,
					users,
					exercises,
					videoIds,
					earliestSubmission,
					latestSubmission,
					random
			);
		}
	}

	private void generateRandomSubmission(
			List<Gym> gyms,
			List<User> users,
			List<Exercise> exercises,
			List<String> videoIds,
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

		submissionRepository.save(new ExerciseSubmission(
				ulid.nextULID(),
				randomChoice(gyms, random),
				randomChoice(exercises, random),
				randomChoice(users, random),
				time,
				randomChoice(videoIds, random),
				rawWeight,
				weightUnit,
				metricWeight,
				random.nextInt(13)
		));
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
}
