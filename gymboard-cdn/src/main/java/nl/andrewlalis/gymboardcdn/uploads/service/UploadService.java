package nl.andrewlalis.gymboardcdn.uploads.service;

import jakarta.servlet.http.HttpServletRequest;
import nl.andrewlalis.gymboardcdn.uploads.api.VideoUploadResponse;
import nl.andrewlalis.gymboardcdn.uploads.api.VideoProcessingTaskStatusResponse;
import nl.andrewlalis.gymboardcdn.files.FileMetadata;
import nl.andrewlalis.gymboardcdn.files.FileStorageService;
import nl.andrewlalis.gymboardcdn.uploads.model.VideoProcessingTask;
import nl.andrewlalis.gymboardcdn.uploads.model.VideoProcessingTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Service
public class UploadService {
	private static final Logger log = LoggerFactory.getLogger(UploadService.class);

	private static final long MAX_UPLOAD_SIZE_BYTES = (1024 * 1024 * 1024); // 1 Gb

	private final VideoProcessingTaskRepository videoTaskRepository;
	private final FileStorageService fileStorageService;

	public UploadService(VideoProcessingTaskRepository videoTaskRepository, FileStorageService fileStorageService) {
		this.videoTaskRepository = videoTaskRepository;
		this.fileStorageService = fileStorageService;
	}

	/**
	 * Handles uploading of a processable video file that will be processed
	 * before being stored in the system.
	 * @param request The request from which we can read the file.
	 * @return A response containing the id of the video processing task, to be
	 * given to the Gymboard API so that it can further manage processing after
	 * a submission is completed.
	 */
	@Transactional
	public VideoUploadResponse processableVideoUpload(HttpServletRequest request) {
		String contentLengthStr = request.getHeader("Content-Length");
		if (contentLengthStr == null || !contentLengthStr.matches("\\d+")) {
			throw new ResponseStatusException(HttpStatus.LENGTH_REQUIRED);
		}
		long contentLength = Long.parseUnsignedLong(contentLengthStr);
		if (contentLength > MAX_UPLOAD_SIZE_BYTES) {
			throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE);
		}
		String filename = request.getHeader("X-Gymboard-Filename");
		if (filename == null) filename = "unnamed.mp4";
		FileMetadata metadata = new FileMetadata(
				filename,
				request.getContentType(),
				false
		);
		String uploadFileId;
		try {
			uploadFileId = fileStorageService.save(request.getInputStream(), metadata, contentLength);
		} catch (IOException e) {
			log.error("Failed to save video upload to temp file.", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		var task = videoTaskRepository.save(new VideoProcessingTask(
				VideoProcessingTask.Status.NOT_STARTED,
				uploadFileId
		));
		return new VideoUploadResponse(task.getId());
	}

	/**
	 * Gets the status of a video processing task.
	 * @param id The task id.
	 * @return The status of the video processing task.
	 */
	@Transactional(readOnly = true)
	public VideoProcessingTaskStatusResponse getVideoProcessingStatus(long id) {
		VideoProcessingTask task = videoTaskRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new VideoProcessingTaskStatusResponse(
				task.getStatus().name(),
				task.getVideoFileId(),
				task.getThumbnailFileId()
		);
	}

	/**
	 * Marks this task as waiting to be picked up for processing. The Gymboard
	 * API should send a message itself to start processing of an uploaded video
	 * once it validates a submission.
	 * @param taskId The task id.
	 */
	@Transactional
	public void startVideoProcessing(long taskId) {
		VideoProcessingTask task = videoTaskRepository.findById(taskId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (task.getStatus() == VideoProcessingTask.Status.NOT_STARTED) {
			task.setStatus(VideoProcessingTask.Status.WAITING);
			videoTaskRepository.save(task);
		}
	}
}
