package nl.andrewlalis.gymboardcdn.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.andrewlalis.gymboardcdn.api.FileMetadataResponse;
import nl.andrewlalis.gymboardcdn.api.FileUploadResponse;
import nl.andrewlalis.gymboardcdn.api.VideoProcessingTaskStatusResponse;
import nl.andrewlalis.gymboardcdn.model.StoredFile;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

@Service
public class UploadService {
	private static final Logger log = LoggerFactory.getLogger(UploadService.class);

	private final StoredFileRepository storedFileRepository;
	private final VideoProcessingTaskRepository videoTaskRepository;
	private final FileService fileService;

	public UploadService(StoredFileRepository storedFileRepository,
						 VideoProcessingTaskRepository videoTaskRepository,
						 FileService fileService) {
		this.storedFileRepository = storedFileRepository;
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

	/**
	 * Streams the contents of a stored file to a client via the Http response.
	 * @param id The file's unique identifier.
	 * @param response The response to stream the content to.
	 */
	@Transactional(readOnly = true)
	public void streamFile(String id, HttpServletResponse response) {
		StoredFile file = storedFileRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		response.setContentType(file.getMimeType());
		response.setContentLengthLong(file.getSize());
		try {
			Path filePath = fileService.getStoragePathForFile(file);
			try (var in = Files.newInputStream(filePath)) {
				in.transferTo(response.getOutputStream());
			}
		} catch (IOException e) {
			log.error("Failed to write file to response.", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional(readOnly = true)
	public FileMetadataResponse getFileMetadata(String id) {
		StoredFile file = storedFileRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		try {
			Path filePath = fileService.getStoragePathForFile(file);
			boolean exists = Files.exists(filePath);
			return new FileMetadataResponse(
					file.getName(),
					file.getMimeType(),
					file.getSize(),
					file.getUploadedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
					exists
			);
		} catch (IOException e) {
			log.error("Couldn't get path to stored file.", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
