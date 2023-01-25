package nl.andrewlalis.gymboard_api.service;

import nl.andrewlalis.gymboard_api.controller.dto.ExerciseSubmissionPayload;
import nl.andrewlalis.gymboard_api.controller.dto.ExerciseSubmissionResponse;
import nl.andrewlalis.gymboard_api.controller.dto.RawGymId;
import nl.andrewlalis.gymboard_api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.dao.StoredFileRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseSubmissionRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseSubmissionTempFileRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseSubmissionVideoFileRepository;
import nl.andrewlalis.gymboard_api.model.Gym;
import nl.andrewlalis.gymboard_api.model.StoredFile;
import nl.andrewlalis.gymboard_api.model.exercise.Exercise;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmission;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmissionTempFile;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmissionVideoFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Service which handles the logic behind accepting, validating, and processing
 * exercise submissions.
 */
@Service
public class ExerciseSubmissionService {
	private static final Logger log = LoggerFactory.getLogger(ExerciseSubmissionService.class);

	private final GymRepository gymRepository;
	private final StoredFileRepository fileRepository;
	private final ExerciseRepository exerciseRepository;
	private final ExerciseSubmissionRepository exerciseSubmissionRepository;
	private final ExerciseSubmissionTempFileRepository tempFileRepository;
	private final ExerciseSubmissionVideoFileRepository submissionVideoFileRepository;

	public ExerciseSubmissionService(GymRepository gymRepository,
									 StoredFileRepository fileRepository,
									 ExerciseRepository exerciseRepository,
									 ExerciseSubmissionRepository exerciseSubmissionRepository,
									 ExerciseSubmissionTempFileRepository tempFileRepository,
									 ExerciseSubmissionVideoFileRepository submissionVideoFileRepository) {
		this.gymRepository = gymRepository;
		this.fileRepository = fileRepository;
		this.exerciseRepository = exerciseRepository;
		this.exerciseSubmissionRepository = exerciseSubmissionRepository;
		this.tempFileRepository = tempFileRepository;
		this.submissionVideoFileRepository = submissionVideoFileRepository;
	}


	/**
	 * Handles the creation of a new exercise submission. This involves a few steps:
	 * <ol>
	 *     <li>Pre-fetch all of the referenced data, like exercise and video file.</li>
	 *     <li>Check that the submission is legitimate.</li>
	 *     <li>Begin video processing.</li>
	 *     <li>Save the submission with the PROCESSING status.</li>
	 * </ol>
	 * Once the asynchronous submission processing is complete, the submission
	 * status will change to COMPLETE.
	 * @param id The gym id.
	 * @param payload The submission data.
	 * @return The saved submission, which will be in the PROCESSING state at first.
	 */
	@Transactional
	public ExerciseSubmissionResponse createSubmission(RawGymId id, ExerciseSubmissionPayload payload) {
		Gym gym = gymRepository.findByRawId(id.gymName(), id.cityCode(), id.countryCode())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Exercise exercise = exerciseRepository.findById(payload.exerciseShortName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid exercise."));
		ExerciseSubmissionTempFile tempFile = tempFileRepository.findById(payload.videoId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid video id."));

		// TODO: Validate the submission data.

		// Create the submission.
		ExerciseSubmission submission = exerciseSubmissionRepository.save(new ExerciseSubmission(
				gym,
				exercise,
				payload.name(),
				BigDecimal.valueOf(payload.weight()),
				payload.reps()
		));
		// Then link it to the temporary video file so the async task can find it.
		tempFile.setSubmission(submission);
		tempFileRepository.save(tempFile);
		// The submission will be picked up eventually to be processed.

		return new ExerciseSubmissionResponse(submission);
	}

	/**
	 * Simple scheduled task that periodically checks for new submissions
	 * that are waiting to be processed, and queues tasks to do so.
	 */
	@Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
	public void processWaitingSubmissions() {
		List<ExerciseSubmission> waitingSubmissions = exerciseSubmissionRepository.findAllByStatus(ExerciseSubmission.Status.WAITING);
		for (var submission : waitingSubmissions) {
			processSubmission(submission.getId());
		}
	}

	/**
	 * Asynchronous task that's started after a submission is submitted, which
	 * handles video processing and anything else that might need to be done
	 * before the submission can be marked as COMPLETED.
	 * <p>
	 *     Note: This method is intentionally NOT transactional, since it may
	 *     have a long duration, and we want real-time status updates.
	 * </p>
	 * @param submissionId The submission's id.
	 */
	@Async
	public void processSubmission(long submissionId) {
		log.info("Starting processing of submission {}.", submissionId);
		// First try and fetch the submission.
		Optional<ExerciseSubmission> optionalSubmission = exerciseSubmissionRepository.findById(submissionId);
		if (optionalSubmission.isEmpty()) {
			log.warn("Submission id {} is not associated with a submission.", submissionId);
			return;
		}
		ExerciseSubmission submission = optionalSubmission.get();
		if (submission.getStatus() != ExerciseSubmission.Status.WAITING) {
			log.warn("Submission {} cannot be processed because its status {} is not WAITING.", submission.getId(), submission.getStatus());
			return;
		}

		// Set the status to processing.
		submission.setStatus(ExerciseSubmission.Status.PROCESSING);
		exerciseSubmissionRepository.save(submission);

		// Then try and fetch the temporary video file associated with it.
		Optional<ExerciseSubmissionTempFile> optionalTempFile = tempFileRepository.findBySubmission(submission);
		if (optionalTempFile.isEmpty()) {
			log.warn("Submission {} failed because the temporary video file couldn't be found.", submission.getId());
			submission.setStatus(ExerciseSubmission.Status.FAILED);
			exerciseSubmissionRepository.save(submission);
			return;
		}
		ExerciseSubmissionTempFile tempFile = optionalTempFile.get();
		Path tempFilePath = Path.of(tempFile.getPath());
		if (!Files.exists(tempFilePath) || !Files.isReadable(tempFilePath)) {
			log.error("Submission {} failed because the temporary video file {} isn't readable.", submission.getId(), tempFilePath);
			submission.setStatus(ExerciseSubmission.Status.FAILED);
			exerciseSubmissionRepository.save(submission);
			return;
		}

		// Now we can try to process the video file into a compressed format that can be stored in the DB.
		Path dir = tempFilePath.getParent();
		String tempFileName = tempFilePath.getFileName().toString();
		String tempFileBaseName = tempFileName.substring(0, tempFileName.length() - ".tmp".length());
		Path outFilePath = dir.resolve(tempFileBaseName + "-out.mp4");
		StoredFile file;
		try {
			processVideo(dir, tempFilePath, outFilePath);
			file = fileRepository.save(new StoredFile(
					"compressed.mp4",
					"video/mp4",
					Files.size(outFilePath),
					Files.readAllBytes(outFilePath)
			));
		} catch (Exception e) {
			log.error("""
					Video processing failed for submission {}:
					  Input file: {}
					  Output file: {}
					  Exception message: {}""",
					submission.getId(),
					tempFilePath,
					outFilePath,
					e.getMessage()
			);
			submission.setStatus(ExerciseSubmission.Status.FAILED);
			exerciseSubmissionRepository.save(submission);
			return;
		}

		// After we've saved the processed file, we can link it to the submission, and set the submission's status.
		submissionVideoFileRepository.save(new ExerciseSubmissionVideoFile(
				submission,
				file
		));
		submission.setStatus(ExerciseSubmission.Status.COMPLETED);
		exerciseSubmissionRepository.save(submission);
		// And delete the temporary files.
		try {
			Files.delete(tempFilePath);
			Files.delete(outFilePath);
			tempFileRepository.delete(tempFile);
		} catch (IOException e) {
			log.error("Couldn't delete temporary files after submission completed.", e);
		}
		log.info("Processing of submission {} complete.", submission.getId());
	}

	private void processVideo(Path dir, Path inFile, Path outFile) throws IOException, InterruptedException {
		Path tmpStdout = Files.createTempFile(dir, "stdout-", ".log");
		Path tmpStderr = Files.createTempFile(dir, "stderr-", ".log");
		final String[] command = {
				"ffmpeg", "-i", inFile.getFileName().toString(),
				"-vf", "scale=640x480:flags=lanczos",
				"-vcodec", "libx264",
				"-crf", "28",
				outFile.getFileName().toString()
		};

		long startSize = Files.size(inFile);
		Instant startTime = Instant.now();

		Process ffmpegProcess = new ProcessBuilder()
				.command(command)
				.redirectOutput(tmpStdout.toFile())
				.redirectError(tmpStderr.toFile())
				.directory(dir.toFile())
				.start();
		int result = ffmpegProcess.waitFor();
		if (result != 0) throw new CommandFailedException(command, result, tmpStdout, tmpStderr);

		long endSize = Files.size(outFile);
		Duration dur = Duration.between(startTime, Instant.now());
		double reductionFactor = startSize / (double) endSize;
		String reductionFactorStr = String.format("%.3f%%", reductionFactor * 100);
		log.info("Processed video from {} bytes to {} bytes in {} seconds, {} reduction.", startSize, endSize, dur.getSeconds(), reductionFactorStr);

		Files.deleteIfExists(tmpStdout);
		Files.deleteIfExists(tmpStderr);
	}
}