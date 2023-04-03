package nl.andrewlalis.gymboardcdn.service;

import jakarta.servlet.http.HttpServletRequest;
import nl.andrewlalis.gymboardcdn.api.FileUploadResponse;
import nl.andrewlalis.gymboardcdn.api.VideoProcessingTaskStatusResponse;
import nl.andrewlalis.gymboardcdn.model.StoredFileRepository;
import nl.andrewlalis.gymboardcdn.model.VideoProcessingTask;
import nl.andrewlalis.gymboardcdn.model.VideoProcessingTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class UploadService {
	private static final Logger log = LoggerFactory.getLogger(UploadService.class);

	private static final long MAX_UPLOAD_SIZE_BYTES = (1024 * 1024 * 1024); // 1 Gb

	private final VideoProcessingTaskRepository videoTaskRepository;
	private final FileService fileService;

	public UploadService(VideoProcessingTaskRepository videoTaskRepository, FileService fileService) {
		this.videoTaskRepository = videoTaskRepository;
		this.fileService = fileService;
	}

	/**
	 * Handles uploading of a processable video file that will be processed
	 * before being stored in the system.
	 * @param request The request from which we can read the file.
	 * @return A response that contains an identifier that can be used to check
	 * the status of the video processing, and eventually fetch the video.
	 */
	@Transactional
	public FileUploadResponse processableVideoUpload(HttpServletRequest request) {
		String contentLengthStr = request.getHeader("Content-Length");
		if (contentLengthStr == null || !contentLengthStr.matches("\\d+")) {
			throw new ResponseStatusException(HttpStatus.LENGTH_REQUIRED);
		}
		long contentLength = Long.parseUnsignedLong(contentLengthStr);
		if (contentLength > MAX_UPLOAD_SIZE_BYTES) {
			throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE);
		}
		Path tempFile;
		String filename = request.getHeader("X-Gymboard-Filename");
		if (filename == null) filename = "unnamed.mp4";
		try {
			tempFile = fileService.saveToTempFile(request.getInputStream(), filename);
		} catch (IOException e) {
			log.error("Failed to save video upload to temp file.", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		String identifier = fileService.createNewFileIdentifier();
		videoTaskRepository.save(new VideoProcessingTask(
				VideoProcessingTask.Status.WAITING,
				filename,
				tempFile.toString(),
				identifier
		));
		return new FileUploadResponse(identifier);
	}

	/**
	 * Gets the status of a video processing task.
	 * @param id The video identifier.
	 * @return The status of the video processing task.
	 */
	@Transactional(readOnly = true)
	public VideoProcessingTaskStatusResponse getVideoProcessingStatus(String id) {
		VideoProcessingTask task = videoTaskRepository.findByVideoIdentifier(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new VideoProcessingTaskStatusResponse(task.getStatus().name());
	}
}
