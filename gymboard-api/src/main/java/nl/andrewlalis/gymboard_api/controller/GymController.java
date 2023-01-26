package nl.andrewlalis.gymboard_api.controller;

import nl.andrewlalis.gymboard_api.controller.dto.*;
import nl.andrewlalis.gymboard_api.service.ExerciseSubmissionService;
import nl.andrewlalis.gymboard_api.service.GymService;
import nl.andrewlalis.gymboard_api.service.UploadService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for accessing a particular gym.
 */
@RestController
@RequestMapping(path = "/gyms/{compoundId}")
public class GymController {
	private final GymService gymService;
	private final UploadService uploadService;
	private final ExerciseSubmissionService submissionService;

	public GymController(GymService gymService, UploadService uploadService, ExerciseSubmissionService submissionService) {
		this.gymService = gymService;
		this.uploadService = uploadService;
		this.submissionService = submissionService;
	}

	@GetMapping
	public GymResponse getGym(@PathVariable String compoundId) {
		return gymService.getGym(CompoundGymId.parse(compoundId));
	}

	@PostMapping(path = "/submissions")
	public ExerciseSubmissionResponse createSubmission(
			@PathVariable String compoundId,
			@RequestBody ExerciseSubmissionPayload payload
	) {
		return submissionService.createSubmission(CompoundGymId.parse(compoundId), payload);
	}

	@GetMapping(path = "/submissions/{submissionId}")
	public ExerciseSubmissionResponse getSubmission(
			@PathVariable String compoundId,
			@PathVariable long submissionId
	) {
		return submissionService.getSubmission(CompoundGymId.parse(compoundId), submissionId);
	}

	@PostMapping(path = "/submissions/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public UploadedFileResponse uploadVideo(
			@PathVariable String compoundId,
			@RequestParam MultipartFile file
	) {
		return uploadService.handleSubmissionUpload(CompoundGymId.parse(compoundId), file);
	}
}
