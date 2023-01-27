package nl.andrewlalis.gymboard_api.controller;

import jakarta.servlet.http.HttpServletResponse;
import nl.andrewlalis.gymboard_api.controller.dto.ExerciseSubmissionResponse;
import nl.andrewlalis.gymboard_api.service.ExerciseSubmissionService;
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
	public ExerciseSubmissionResponse getSubmission(@PathVariable String submissionId) {
		return submissionService.getSubmission(submissionId);
	}

	@GetMapping(path = "/{submissionId}/video")
	public void getSubmissionVideo(@PathVariable String submissionId, HttpServletResponse response) {
		submissionService.streamVideo(submissionId, response);
	}
}