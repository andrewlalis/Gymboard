package nl.andrewlalis.gymboardcdn.api;

import nl.andrewlalis.gymboardcdn.service.UploadService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {
	private final UploadService uploadService;

	public UploadController(UploadService uploadService) {
		this.uploadService = uploadService;
	}

	@PostMapping(path = "/uploads/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public FileUploadResponse uploadVideo(@RequestParam MultipartFile file) {
		return uploadService.processableVideoUpload(file);
	}

	@GetMapping(path = "/uploads/video/{identifier}/status")
	public VideoProcessingTaskStatusResponse getVideoProcessingStatus(@PathVariable String identifier) {
		return uploadService.getVideoProcessingStatus(identifier);
	}
}
