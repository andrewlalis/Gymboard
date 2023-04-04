package nl.andrewlalis.gymboardcdn.uploads.service;

import nl.andrewlalis.gymboardcdn.files.FileMetadata;
import nl.andrewlalis.gymboardcdn.files.FileStorageService;
import nl.andrewlalis.gymboardcdn.files.util.ULID;
import nl.andrewlalis.gymboardcdn.uploads.model.VideoProcessingTask;
import nl.andrewlalis.gymboardcdn.uploads.model.VideoProcessingTaskRepository;
import nl.andrewlalis.gymboardcdn.uploads.service.process.ThumbnailGenerator;
import nl.andrewlalis.gymboardcdn.uploads.service.process.VideoProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
public class VideoProcessingService {
	private static final Logger log = LoggerFactory.getLogger(VideoProcessingService.class);

	private final Executor videoProcessingExecutor;
	private final VideoProcessingTaskRepository taskRepo;
	private final FileStorageService fileStorageService;
	private final VideoProcessor videoProcessor;
	private final ThumbnailGenerator thumbnailGenerator;

	public VideoProcessingService(Executor videoProcessingExecutor,
								  VideoProcessingTaskRepository taskRepo,
								  FileStorageService fileStorageService,
								  VideoProcessor videoProcessor,
								  ThumbnailGenerator thumbnailGenerator) {
		this.videoProcessingExecutor = videoProcessingExecutor;
		this.taskRepo = taskRepo;
		this.fileStorageService = fileStorageService;
		this.videoProcessor = videoProcessor;
		this.thumbnailGenerator = thumbnailGenerator;
	}

	@Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
	public void startWaitingTasks() {
		List<VideoProcessingTask> waitingTasks = taskRepo.findAllByStatusOrderByCreatedAtDesc(VideoProcessingTask.Status.WAITING);
		for (var task : waitingTasks) {
			log.info("Queueing processing of task {}.", task.getId());
			updateTask(task, VideoProcessingTask.Status.IN_PROGRESS);
			videoProcessingExecutor.execute(() -> processVideo(task));
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

		Path uploadFile = fileStorageService.getStoragePathForFile(task.getUploadFileId());
		if (Files.notExists(uploadFile) || !Files.isReadable(uploadFile)) {
			log.error("Uploaded video file {} doesn't exist or isn't readable.", uploadFile);
			updateTask(task, VideoProcessingTask.Status.FAILED);
			return;
		}

		Path videoFile = uploadFile.resolveSibling(task.getUploadFileId() + "-vid-out");
		Path thumbnailFile = uploadFile.resolveSibling(task.getUploadFileId() + "-thm-out");
		try {
			log.info("Processing video for uploaded video file {}.", uploadFile.getFileName());
			videoProcessor.processVideo(uploadFile, videoFile);
			log.info("Generating thumbnail for uploaded video file {}.", uploadFile.getFileName());
			thumbnailGenerator.generateThumbnailImage(uploadFile, thumbnailFile);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("""
					Video processing failed for task {}:
					  Input file:        {}
					  Output file:       {}
					  Exception message: {}""",
					task.getId(),
					uploadFile,
					videoFile,
					e.getMessage()
			);
			updateTask(task, VideoProcessingTask.Status.FAILED);
			return;
		}

		// And finally, copy the output to the final location.
		try (
				var videoIn = Files.newInputStream(videoFile);
				var thumbnailIn = Files.newInputStream(thumbnailFile)
		) {
			// Save the video to a final file location.
			var originalMetadata = fileStorageService.getMetadata(task.getUploadFileId());
			FileMetadata metadata = new FileMetadata(
					originalMetadata.filename(),
					originalMetadata.mimeType(),
					true
			);
			fileStorageService.save(ULID.parseULID(task.getVideoFileId()), videoIn, metadata, Files.size(videoFile));
			// Save the thumbnail too.
			FileMetadata thumbnailMetadata = new FileMetadata(
					"thumbnail.jpeg",
					"image/jpeg",
					true
			);
			fileStorageService.save(thumbnailIn, thumbnailMetadata, Files.size(thumbnailFile));
			updateTask(task, VideoProcessingTask.Status.COMPLETED);
			log.info("Finished processing task {}.", task.getId());

			// TODO: Send HTTP POST to API, with video id and thumbnail id.
		} catch (IOException e) {
			log.error("Failed to copy processed video to final storage location.", e);
			updateTask(task, VideoProcessingTask.Status.FAILED);
		} finally {
			try {
				fileStorageService.delete(task.getUploadFileId());
				Files.deleteIfExists(videoFile);
				Files.deleteIfExists(thumbnailFile);
			} catch (IOException e) {
				log.error("Couldn't delete temporary output files for uploaded video {}", uploadFile);
				e.printStackTrace();
			}
		}
	}

	private void updateTask(VideoProcessingTask task, VideoProcessingTask.Status status) {
		task.setStatus(status);
		taskRepo.saveAndFlush(task);
	}
}
