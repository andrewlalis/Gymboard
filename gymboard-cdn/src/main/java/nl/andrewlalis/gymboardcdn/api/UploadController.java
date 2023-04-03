package nl.andrewlalis.gymboardcdn.api;

import jakarta.servlet.http.HttpServletRequest;
import nl.andrewlalis.gymboardcdn.service.UploadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UploadController {
	private final UploadService uploadService;

	public UploadController(UploadService uploadService) {
		this.uploadService = uploadService;
	}

	@PostMapping(path = "/uploads/video", consumes = {"video/mp4"})
	public FileUploadResponse uploadVideo(HttpServletRequest request) {
		return uploadService.processableVideoUpload(request);
	}

	@GetMapping(path = "/uploads/video/{id}/status")
	public VideoProcessingTaskStatusResponse getVideoProcessingStatus(@PathVariable String id) {
		return uploadService.getVideoProcessingStatus(id);
	}
}
