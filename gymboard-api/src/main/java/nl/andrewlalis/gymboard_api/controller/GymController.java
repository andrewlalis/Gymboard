package nl.andrewlalis.gymboard_api.controller;

import nl.andrewlalis.gymboard_api.controller.dto.CompoundGymId;
import nl.andrewlalis.gymboard_api.controller.dto.ExerciseSubmissionPayload;
import nl.andrewlalis.gymboard_api.controller.dto.ExerciseSubmissionResponse;
import nl.andrewlalis.gymboard_api.controller.dto.GymResponse;
import nl.andrewlalis.gymboard_api.service.GymService;
import nl.andrewlalis.gymboard_api.service.submission.ExerciseSubmissionService;
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
	public List<ExerciseSubmissionResponse> getRecentSubmissions(@PathVariable String compoundId) {
		return gymService.getRecentSubmissions(CompoundGymId.parse(compoundId));
	}

	@PostMapping(path = "/submissions")
	public ExerciseSubmissionResponse createSubmission(
			@PathVariable String compoundId,
			@RequestBody ExerciseSubmissionPayload payload
	) {
		return submissionService.createSubmission(CompoundGymId.parse(compoundId), payload);
	}
}
