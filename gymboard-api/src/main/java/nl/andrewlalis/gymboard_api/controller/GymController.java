package nl.andrewlalis.gymboard_api.controller;

import nl.andrewlalis.gymboard_api.controller.dto.*;
import nl.andrewlalis.gymboard_api.service.GymService;
import nl.andrewlalis.gymboard_api.service.UploadService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controller for accessing a particular gym.
 */
@RestController
public class GymController {
	private final GymService gymService;
	private final UploadService uploadService;

	public GymController(GymService gymService, UploadService uploadService) {
		this.gymService = gymService;
		this.uploadService = uploadService;
	}

	@GetMapping(path = "/gyms/{countryCode}/{cityCode}/{gymName}")
	public GymResponse getGym(
			@PathVariable String countryCode,
			@PathVariable String cityCode,
			@PathVariable String gymName
	) {
		return gymService.getGym(new RawGymId(countryCode, cityCode, gymName));
	}

	@PostMapping(path = "/gyms/{countryCode}/{cityCode}/{gymName}/submissions")
	public ExerciseSubmissionResponse createSubmission(
			@PathVariable String countryCode,
			@PathVariable String cityCode,
			@PathVariable String gymName,
			@RequestBody ExerciseSubmissionPayload payload
	) throws IOException {
		return gymService.createSubmission(new RawGymId(countryCode, cityCode, gymName), payload);
	}

	@PostMapping(
			path = "/gyms/{countryCode}/{cityCode}/{gymName}/submissions/upload",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	)
	public UploadedFileResponse uploadVideo(
			@PathVariable String countryCode,
			@PathVariable String cityCode,
			@PathVariable String gymName,
			@RequestParam MultipartFile file
	) {
		return uploadService.handleSubmissionUpload(new RawGymId(countryCode, cityCode, gymName), file);
	}
}
