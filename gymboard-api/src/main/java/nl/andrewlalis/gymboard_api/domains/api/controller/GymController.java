package nl.andrewlalis.gymboard_api.domains.api.controller;

import nl.andrewlalis.gymboard_api.domains.api.dto.CompoundGymId;
import nl.andrewlalis.gymboard_api.domains.submission.dto.SubmissionPayload;
import nl.andrewlalis.gymboard_api.domains.submission.dto.SubmissionResponse;
import nl.andrewlalis.gymboard_api.domains.api.dto.GymResponse;
import nl.andrewlalis.gymboard_api.domains.api.service.GymService;
import nl.andrewlalis.gymboard_api.domains.api.service.submission.ExerciseSubmissionService;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for accessing a particular gym.
 */
@RestController
@RequestMapping(path = "/gyms/{compoundId}")
public class GymController {
	private final GymService gymService;
	private final ExerciseSubmissionService submissionService;

	public GymController(GymService gymService, ExerciseSubmissionService submissionService) {
		this.gymService = gymService;
		this.submissionService = submissionService;
	}

	@GetMapping
	public GymResponse getGym(@PathVariable String compoundId) {
		return gymService.getGym(CompoundGymId.parse(compoundId));
	}

	@GetMapping(path = "/recent-submissions")
	public List<SubmissionResponse> getRecentSubmissions(@PathVariable String compoundId) {
		return gymService.getRecentSubmissions(CompoundGymId.parse(compoundId));
	}

	@PostMapping(path = "/submissions")
	public SubmissionResponse createSubmission(
			@PathVariable String compoundId,
			@AuthenticationPrincipal User user,
			@RequestBody SubmissionPayload payload
	) {
		return submissionService.createSubmission(CompoundGymId.parse(compoundId), user.getId(), payload);
	}
}
