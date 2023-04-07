package nl.andrewlalis.gymboardcdn.uploads.api;

import jakarta.servlet.http.HttpServletRequest;
import nl.andrewlalis.gymboardcdn.ServiceOnly;
import nl.andrewlalis.gymboardcdn.uploads.service.UploadService;
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
	public VideoUploadResponse uploadVideo(HttpServletRequest request) {
		return uploadService.processableVideoUpload(request);
	}

	@PostMapping(path = "/uploads/video/{taskId}/start") @ServiceOnly
	public void startVideoProcessing(@PathVariable long taskId) {
		uploadService.startVideoProcessing(taskId);
	}

	@GetMapping(path = "/uploads/video/{taskId}/status")
	public VideoProcessingTaskStatusResponse getVideoProcessingStatus(@PathVariable long taskId) {
		return uploadService.getVideoProcessingStatus(taskId);
	}
}
