package nl.andrewlalis.gymboardcdn.service;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;

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

	@Transactional
	public FileUploadResponse processableVideoUpload(MultipartFile file) {
		Path tempFile;
		try {
			tempFile = fileService.saveToTempFile(file);
		} catch (IOException e) {
			log.error("Failed to save video upload to temp file.", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		String identifier = fileService.createNewFileIdentifier();
		videoTaskRepository.save(new VideoProcessingTask(
				VideoProcessingTask.Status.WAITING,
				file.getOriginalFilename(),
				tempFile.toString(),
				identifier
		));
		return new FileUploadResponse(identifier);
	}

	@Transactional(readOnly = true)
	public VideoProcessingTaskStatusResponse getVideoProcessingStatus(String identifier) {
		VideoProcessingTask task = videoTaskRepository.findByVideoIdentifier(identifier)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new VideoProcessingTaskStatusResponse(task.getStatus().name());
	}
}
