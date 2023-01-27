package nl.andrewlalis.gymboard_api.service.submission;

import nl.andrewlalis.gymboard_api.dao.StoredFileRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseSubmissionRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseSubmissionTempFileRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseSubmissionVideoFileRepository;
import nl.andrewlalis.gymboard_api.model.StoredFile;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmission;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmissionTempFile;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmissionVideoFile;
import nl.andrewlalis.gymboard_api.service.CommandFailedException;
import nl.andrewlalis.gymboard_api.service.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * This service is responsible for the logic of processing new exercise
 * submissions and tasks immediately related to that.
 */
@Service
public class SubmissionProcessingService {
	private static final Logger log = LoggerFactory.getLogger(SubmissionProcessingService.class);

	private final ExerciseSubmissionRepository exerciseSubmissionRepository;
	private final Executor taskExecutor;
	private final ExerciseSubmissionTempFileRepository tempFileRepository;
	private final ExerciseSubmissionVideoFileRepository videoFileRepository;
	private final StoredFileRepository fileRepository;

	public SubmissionProcessingService(ExerciseSubmissionRepository exerciseSubmissionRepository,
									   Executor taskExecutor,
									   ExerciseSubmissionTempFileRepository tempFileRepository,
									   ExerciseSubmissionVideoFileRepository videoFileRepository,
									   StoredFileRepository fileRepository) {
		this.exerciseSubmissionRepository = exerciseSubmissionRepository;
		this.taskExecutor = taskExecutor;
		this.tempFileRepository = tempFileRepository;
		this.videoFileRepository = videoFileRepository;
		this.fileRepository = fileRepository;
	}

	/**
	 * Simple scheduled task that periodically checks for new submissions
	 * that are waiting to be processed, and queues tasks to do so.
	 */
	@Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
	public void processWaitingSubmissions() {
		List<ExerciseSubmission> waitingSubmissions = exerciseSubmissionRepository.findAllByStatus(ExerciseSubmission.Status.WAITING);
		for (var submission : waitingSubmissions) {
			taskExecutor.execute(() -> processSubmission(submission.getId()));
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
	private void processSubmission(String submissionId) {
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
		Path dir = UploadService.SUBMISSION_TEMP_FILE_DIR;
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
		videoFileRepository.save(new ExerciseSubmissionVideoFile(
				submission,
				file
		));
		submission.setStatus(ExerciseSubmission.Status.COMPLETED);
		submission.setComplete(true);
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

	/**
	 * Uses the `ffmpeg` system command to process a raw input video and produce
	 * a compressed, reduced-size output video that's ready for usage in the
	 * application.
	 * @param dir The working directory.
	 * @param inFile The input file to read from.
	 * @param outFile The output file to write to. MUST have a ".mp4" extension.
	 * @throws IOException If a filesystem error occurs.
	 * @throws CommandFailedException If the ffmpeg command fails.
	 * @throws InterruptedException If the ffmpeg command is interrupted.
	 */
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

		// Delete the logs if everything was successful.
		Files.deleteIfExists(tmpStdout);
		Files.deleteIfExists(tmpStderr);
	}

	@Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
	public void removeOldUploadedFiles() {
		// First remove any temp files older than 10 minutes.
		LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
		var tempFiles = tempFileRepository.findAllByCreatedAtBefore(cutoff);
		for (var file : tempFiles) {
			try {
				Files.deleteIfExists(Path.of(file.getPath()));
				tempFileRepository.delete(file);
				log.info("Removed temporary submission file {} at {}.", file.getId(), file.getPath());
			} catch (IOException e) {
				log.error(String.format("Could not delete submission temp file %d at %s.", file.getId(), file.getPath()), e);
			}
		}

		// Then remove any files in the directory which don't correspond to a valid file in the db.
		if (Files.notExists(UploadService.SUBMISSION_TEMP_FILE_DIR)) return;
		try (var s = Files.list(UploadService.SUBMISSION_TEMP_FILE_DIR)) {
			for (var path : s.toList()) {
				if (!tempFileRepository.existsByPath(path.toString())) {
					try {
						Files.delete(path);
					} catch (IOException e) {
						log.error("Couldn't delete orphan temp file: " + path, e);
					}
				}
			}
		} catch (IOException e) {
			log.error("Couldn't get list of temp files.", e);
		}
	}
}
