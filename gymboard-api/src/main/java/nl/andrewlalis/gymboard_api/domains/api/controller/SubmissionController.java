package nl.andrewlalis.gymboard_api.domains.api.controller;

import nl.andrewlalis.gymboard_api.domains.api.dto.SubmissionResponse;
import nl.andrewlalis.gymboard_api.domains.api.service.submission.ExerciseSubmissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
