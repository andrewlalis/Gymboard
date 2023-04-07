package nl.andrewlalis.gymboardcdn.uploads.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.andrewlalis.gymboardcdn.files.FileMetadata;
import nl.andrewlalis.gymboardcdn.files.FileStorageService;
import nl.andrewlalis.gymboardcdn.uploads.model.VideoProcessingTask;
import nl.andrewlalis.gymboardcdn.uploads.model.VideoProcessingTaskRepository;
import nl.andrewlalis.gymboardcdn.uploads.model.VideoProcessingTaskStatusUpdate;
import nl.andrewlalis.gymboardcdn.uploads.service.process.ThumbnailGenerator;
import nl.andrewlalis.gymboardcdn.uploads.service.process.VideoProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
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
	private final ObjectMapper objectMapper;

	@Value("${app.api-origin}")
	private String apiOrigin;

	@Value("${app.api-secret}")
	private String apiSecret;

	public VideoProcessingService(Executor videoProcessingExecutor,
								  VideoProcessingTaskRepository taskRepo,
								  FileStorageService fileStorageService,
								  VideoProcessor videoProcessor,
								  ThumbnailGenerator thumbnailGenerator, ObjectMapper objectMapper) {
		this.videoProcessingExecutor = videoProcessingExecutor;
		this.taskRepo = taskRepo;
		this.fileStorageService = fileStorageService;
		this.videoProcessor = videoProcessor;
		this.thumbnailGenerator = thumbnailGenerator;
		this.objectMapper = objectMapper;
	}

	private void updateTask(VideoProcessingTask task, VideoProcessingTask.Status status) {
		task.setStatus(status);
		taskRepo.saveAndFlush(task);
		if (status == VideoProcessingTask.Status.COMPLETED || status == VideoProcessingTask.Status.FAILED) {
			sendTaskCompleteToApi(task);
		}
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
				deleteAllTaskFiles(task);
				taskRepo.delete(task);
			} else if (task.getStatus() == VideoProcessingTask.Status.FAILED) {
				log.info("Deleting failed task {}.", task.getId());
				taskRepo.delete(task);
			} else if (task.getStatus() == VideoProcessingTask.Status.IN_PROGRESS) {
				log.info("Task {} was in progress for too long; deleting.", task.getId());
				deleteAllTaskFiles(task);
				taskRepo.delete(task);
			} else if (task.getStatus() == VideoProcessingTask.Status.WAITING) {
				log.info("Task {} was waiting for too long; deleting.", task.getId());
				deleteAllTaskFiles(task);
				taskRepo.delete(task);
			}
		}
	}

	private void processVideo(VideoProcessingTask task) {
		log.info("Started processing task {}.", task.getId());

		Path uploadFile = fileStorageService.getStoragePathForFile(task.getUploadFileId());
		Path rawUploadFile = uploadFile.resolveSibling(task.getUploadFileId() + "-vid-in");
		if (Files.notExists(uploadFile) || !Files.isReadable(uploadFile)) {
			log.error("Uploaded video file {} doesn't exist or isn't readable.", uploadFile);
			updateTask(task, VideoProcessingTask.Status.FAILED);
			return;
		}
		try {
			fileStorageService.copyTo(task.getUploadFileId(), rawUploadFile);
		} catch (IOException e) {
			log.error("Failed to copy raw video file {} to {}.", uploadFile, rawUploadFile);
			e.printStackTrace();
			updateTask(task, VideoProcessingTask.Status.FAILED);
			return;
		}

		// Run the actual processing here.
		Path videoFile = uploadFile.resolveSibling(task.getUploadFileId() + "-vid-out.mp4");
		Path thumbnailFile = uploadFile.resolveSibling(task.getUploadFileId() + "-thm-out.jpeg");
		try {
			log.info("Processing video for uploaded video file {}.", uploadFile.getFileName());
			videoProcessor.processVideo(rawUploadFile, videoFile);
			log.info("Generating thumbnail for uploaded video file {}.", uploadFile.getFileName());
			thumbnailGenerator.generateThumbnailImage(videoFile, thumbnailFile);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("""
					Video processing failed for task {}:
					  Input file:        {}
					  Output file:       {}
					  Exception message: {}""",
					task.getId(),
					rawUploadFile,
					videoFile,
					e.getMessage()
			);
			updateTask(task, VideoProcessingTask.Status.FAILED);
			return;
		}

		// And finally, copy the output to the final location.
		try {
			// Save the video to a final file location.
			var originalMetadata = fileStorageService.getMetadata(task.getUploadFileId());
			FileMetadata metadata = new FileMetadata(originalMetadata.filename(), originalMetadata.mimeType(), true);
			String videoFileId = fileStorageService.save(videoFile, metadata);

			// Save the thumbnail too.
			FileMetadata thumbnailMetadata = new FileMetadata("thumbnail.jpeg", "image/jpeg", true);
			String thumbnailFileId = fileStorageService.save(thumbnailFile, thumbnailMetadata);

			task.setVideoFileId(videoFileId);
			task.setThumbnailFileId(thumbnailFileId);
			updateTask(task, VideoProcessingTask.Status.COMPLETED);
			log.info("Finished processing task {}.", task.getId());
		} catch (IOException e) {
			log.error("Failed to copy processed video to final storage location.", e);
			updateTask(task, VideoProcessingTask.Status.FAILED);
		} finally {
			deleteAllTaskFiles(task);
		}
	}

	/**
	 * Sends an update message to the Gymboard API when a task finishes its
	 * processing. Note that Gymboard API will also eventually poll the CDN's
	 * own API to get task status if we fail to send it, so there's some
	 * redundancy built-in.
	 * @param task The task to send.
	 */
	private void sendTaskCompleteToApi(VideoProcessingTask task) {
		String json;
		try {
			json = objectMapper.writeValueAsString(new VideoProcessingTaskStatusUpdate(task));
		} catch (JsonProcessingException e) {
			log.error("JSON error while sending task data to API for task " + task.getId(), e);
			return;
		}
		HttpClient httpClient = HttpClient.newBuilder().build();
		HttpRequest request = HttpRequest.newBuilder(URI.create(apiOrigin + "/submissions/video-processing-complete"))
				.header("Content-Type", "application/json")
				.header("X-Gymboard-Service-Secret", apiSecret)
				.timeout(Duration.ofSeconds(3))
				.POST(HttpRequest.BodyPublishers.ofString(json))
				.build();
		try {
			HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
			if (response.statusCode() >= 400) {
				log.error("API returned not-ok response {}", response.statusCode());
			}
		} catch (Exception e) {
			log.error("Failed to send HTTP request to API.", e);
		}
	}

	/**
	 * Helper function to delete all temporary files related to a task's
	 * processing operations. If the task is FAILED, then files are kept for
	 * debugging purposes.
	 * @param task The task to delete files for.
	 */
	private void deleteAllTaskFiles(VideoProcessingTask task) {
		if (task.getStatus() == VideoProcessingTask.Status.FAILED) {
			log.warn("Retaining files for failed task {}, upload id {}.", task.getId(), task.getUploadFileId());
			return;
		}
		Path dir = fileStorageService.getStoragePathForFile(task.getUploadFileId()).getParent();
		try (var s = Files.list(dir)) {
			var files = s.toList();
			for (var file : files) {
				String filename = file.getFileName().toString().strip();
				if (Files.isRegularFile(file) && filename.startsWith(task.getUploadFileId())) {
					try {
						Files.delete(file);
					} catch (IOException e) {
						log.error("Failed to delete file " + file + " related to task " + task.getId(), e);
					}
				}
			}
		} catch (IOException e) {
			log.error("Failed to list files in " + dir + " when deleting files for task " + task.getId(), e);
		}
	}
}
