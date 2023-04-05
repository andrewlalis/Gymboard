package nl.andrewlalis.gymboard_api.domains.api.controller;

import nl.andrewlalis.gymboard_api.domains.api.dto.SubmissionResponse;
import nl.andrewlalis.gymboard_api.domains.api.dto.VideoProcessingCompletePayload;
import nl.andrewlalis.gymboard_api.domains.api.service.cdn_client.UploadsClient;
import nl.andrewlalis.gymboard_api.domains.api.service.submission.ExerciseSubmissionService;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/submissions")
public class SubmissionController {
	private final ExerciseSubmissionService submissionService;

	public SubmissionController(ExerciseSubmissionService submissionService) {
		this.submissionService = submissionService;
	}

	@GetMapping(path = "/{submissionId}")
	public SubmissionResponse getSubmission(@PathVariable String submissionId) {
		return submissionService.getSubmission(submissionId);
	}

	@DeleteMapping(path = "/{submissionId}")
	public ResponseEntity<Void> deleteSubmission(@PathVariable String submissionId, @AuthenticationPrincipal User user) {
		submissionService.deleteSubmission(submissionId, user);
		return ResponseEntity.noContent().build();
	}

	@PostMapping(path = "/video-processing-complete")
	public ResponseEntity<Void> handleVideoProcessingComplete(@RequestBody VideoProcessingCompletePayload taskStatus) {
		// TODO: Validate that the request came ONLY from the CDN service.
		submissionService.handleVideoProcessingComplete(taskStatus);
		return ResponseEntity.noContent().build();
	}
}
