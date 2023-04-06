package nl.andrewlalis.gymboard_api.domains.api.controller;

import nl.andrewlalis.gymboard_api.domains.submission.dto.SubmissionResponse;
import nl.andrewlalis.gymboard_api.domains.api.service.submission.UserSubmissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserSubmissionsController {
	private final UserSubmissionService submissionService;

	public UserSubmissionsController(UserSubmissionService submissionService) {
		this.submissionService = submissionService;
	}

	@GetMapping(path = "/users/{userId}/recent-submissions")
	public List<SubmissionResponse> getRecentSubmissions(@PathVariable String userId) {
		return submissionService.getRecentSubmissions(userId);
	}

	@GetMapping(path = "/users/{userId}/submissions")
	public Page<SubmissionResponse> getSubmissions(@PathVariable String userId, Pageable pageable) {
		return submissionService.getSubmissions(userId, pageable);
	}
}
