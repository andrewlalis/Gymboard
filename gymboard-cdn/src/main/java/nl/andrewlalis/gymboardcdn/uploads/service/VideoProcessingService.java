package nl.andrewlalis.gymboardcdn.uploads.service;

import nl.andrewlalis.gymboardcdn.files.FileMetadata;
import nl.andrewlalis.gymboardcdn.files.FileStorageService;
import nl.andrewlalis.gymboardcdn.files.util.ULID;
import nl.andrewlalis.gymboardcdn.uploads.model.VideoProcessingTask;
import nl.andrewlalis.gymboardcdn.uploads.model.VideoProcessingTaskRepository;
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

		Path tempFilePath = fileStorageService.getStoragePathForFile(task.getUploadFileId());
		if (Files.notExists(tempFilePath) || !Files.isReadable(tempFilePath)) {
			log.error("Temp file {} doesn't exist or isn't readable.", tempFilePath);
			updateTask(task, VideoProcessingTask.Status.FAILED);
			return;
		}

		// Then begin running the actual FFMPEG processing.
		Path tempDir = tempFilePath.getParent();
		Path ffmpegOutputFile = tempDir.resolve(task.getUploadFileId() + "-video-out");
		Path ffmpegThumbnailOutputFile = tempDir.resolve(task.getUploadFileId() + "-thumbnail-out");
		try {
			generateThumbnailWithFFMPEG(tempDir, tempFilePath, ffmpegThumbnailOutputFile);
			processVideoWithFFMPEG(tempDir, tempFilePath, ffmpegOutputFile);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("""
					Video processing failed for task {}:
					  Input file:        {}
					  Output file:       {}
					  Exception message: {}""",
					task.getId(),
					tempFilePath,
					ffmpegOutputFile,
					e.getMessage()
			);
			updateTask(task, VideoProcessingTask.Status.FAILED);
			return;
		}

		// And finally, copy the output to the final location.
		try (
				var videoIn = Files.newInputStream(ffmpegOutputFile);
				var thumbnailIn = Files.newInputStream(ffmpegThumbnailOutputFile)
		) {
			// Save the video to a final file location.
			var originalMetadata = fileStorageService.getMetadata(task.getUploadFileId());
			FileMetadata metadata = new FileMetadata(
					originalMetadata.filename(),
					originalMetadata.mimeType(),
					true
			);
			fileStorageService.save(ULID.parseULID(task.getVideoFileId()), videoIn, metadata, Files.size(ffmpegOutputFile));
			// Save the thumbnail too.
			FileMetadata thumbnailMetadata = new FileMetadata(
					"thumbnail.jpeg",
					"image/jpeg",
					true
			);
			fileStorageService.save(thumbnailIn, thumbnailMetadata, Files.size(ffmpegThumbnailOutputFile));
			updateTask(task, VideoProcessingTask.Status.COMPLETED);
			log.info("Finished processing task {}.", task.getId());

			// TODO: Send HTTP POST to API, with video id and thumbnail id.
		} catch (IOException e) {
			log.error("Failed to copy processed video to final storage location.", e);
			updateTask(task, VideoProcessingTask.Status.FAILED);
		} finally {
			try {
				fileStorageService.delete(task.getUploadFileId());
				Files.deleteIfExists(ffmpegOutputFile);
				Files.deleteIfExists(ffmpegThumbnailOutputFile);
			} catch (IOException e) {
				log.error("Couldn't delete temporary FFMPEG output file: {}", ffmpegOutputFile);
				e.printStackTrace();
			}
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
