package nl.andrewlalis.gymboardcdn.service;

import nl.andrewlalis.gymboardcdn.model.VideoProcessingTask;
import nl.andrewlalis.gymboardcdn.model.VideoProcessingTaskRepository;
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
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
public class VideoProcessingService {
	private static final Logger log = LoggerFactory.getLogger(VideoProcessingService.class);

	private final Executor taskExecutor;
	private final VideoProcessingTaskRepository taskRepo;
	private final FileStorageService fileStorageService;

	public VideoProcessingService(Executor taskExecutor, VideoProcessingTaskRepository taskRepo, FileStorageService fileStorageService) {
		this.taskExecutor = taskExecutor;
		this.taskRepo = taskRepo;
		this.fileStorageService = fileStorageService;
	}

	@Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
	public void startWaitingTasks() {
		List<VideoProcessingTask> waitingTasks = taskRepo.findAllByStatusOrderByCreatedAtDesc(VideoProcessingTask.Status.WAITING);
		for (var task : waitingTasks) {
			log.info("Queueing processing of task {}.", task.getId());
			updateTask(task, VideoProcessingTask.Status.IN_PROGRESS);
			taskExecutor.execute(() -> processVideo(task));
		}
	}

	@Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
	public void removeOldTasks() {
		LocalDateTime cutoff = LocalDateTime.now().minusHours(12);
		List<VideoProcessingTask> oldTasks = taskRepo.findAllByCreatedAtBefore(cutoff);
		for (var task : oldTasks) {
			if (task.getStatus() == VideoProcessingTask.Status.COMPLETED) {
				log.info("Deleting completed task {}.", task.getId());
				taskRepo.delete(task);
			} else if (task.getStatus() == VideoProcessingTask.Status.FAILED) {
				log.info("Deleting failed task {}.", task.getId());
				taskRepo.delete(task);
			} else if (task.getStatus() == VideoProcessingTask.Status.IN_PROGRESS) {
				log.info("Task {} was in progress for too long; deleting.", task.getId());
				taskRepo.delete(task);
			} else if (task.getStatus() == VideoProcessingTask.Status.WAITING) {
				log.info("Task {} was waiting for too long; deleting.", task.getId());
				taskRepo.delete(task);
			}
		}
	}

	private void processVideo(VideoProcessingTask task) {
		log.info("Started processing task {}.", task.getId());

		Path tempFilePath = fileStorageService.getStoragePathForFile(task.getRawUploadFileId());
		if (Files.notExists(tempFilePath) || !Files.isReadable(tempFilePath)) {
			log.error("Temp file {} doesn't exist or isn't readable.", tempFilePath);
			updateTask(task, VideoProcessingTask.Status.FAILED);
			return;
		}

		// Then begin running the actual FFMPEG processing.
		Path tempDir = tempFilePath.getParent();
		Files.createTempFile()
		Path ffmpegOutputFile = tempDir.resolve(task.getVideoIdentifier());
		try {
			processVideoWithFFMPEG(tempDir, tempFile, ffmpegOutputFile);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("""
					Video processing failed for video {}:
					  Input file:        {}
					  Output file:       {}
					  Exception message: {}""",
					task.getVideoIdentifier(),
					tempFile,
					ffmpegOutputFile,
					e.getMessage()
			);
			updateTask(task, VideoProcessingTask.Status.FAILED);
			return;
		}

		// And finally, copy the output to the final location.
		try {
			StoredFile storedFile = new StoredFile(
					task.getVideoIdentifier(),
					task.getFilename(),
					"video/mp4",
					Files.size(ffmpegOutputFile),
					task.getCreatedAt()
			);
			Path finalFilePath = fileService.getStoragePathForFile(storedFile);
			Files.move(ffmpegOutputFile, finalFilePath);
			Files.deleteIfExists(tempFile);
			Files.deleteIfExists(ffmpegOutputFile);
			storedFileRepository.saveAndFlush(storedFile);
			updateTask(task, VideoProcessingTask.Status.COMPLETED);
			log.info("Finished processing video {}.", task.getVideoIdentifier());
		} catch (IOException e) {
			log.error("Failed to copy processed video to final storage location.", e);
			updateTask(task, VideoProcessingTask.Status.FAILED);
		}
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
	private void processVideoWithFFMPEG(Path dir, Path inFile, Path outFile) throws IOException, InterruptedException {
		Path tmpStdout = Files.createTempFile(dir, "stdout-", ".log");
		Path tmpStderr = Files.createTempFile(dir, "stderr-", ".log");
		final String[] command = {
				"ffmpeg",
				"-i", inFile.getFileName().toString(),
				"-vf", "scale=640x480:flags=lanczos",
				"-vcodec", "libx264",
				"-crf", "28",
				"-f", "mp4",
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

	private void updateTask(VideoProcessingTask task, VideoProcessingTask.Status status) {
		task.setStatus(status);
		taskRepo.saveAndFlush(task);
	}
}
