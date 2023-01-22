package nl.andrewlalis.gymboard_api.service;

import nl.andrewlalis.gymboard_api.controller.dto.ExerciseSubmissionPayload;
import nl.andrewlalis.gymboard_api.controller.dto.ExerciseSubmissionResponse;
import nl.andrewlalis.gymboard_api.controller.dto.GymResponse;
import nl.andrewlalis.gymboard_api.controller.dto.RawGymId;
import nl.andrewlalis.gymboard_api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.dao.StoredFileRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseSubmissionRepository;
import nl.andrewlalis.gymboard_api.model.Gym;
import nl.andrewlalis.gymboard_api.model.StoredFile;
import nl.andrewlalis.gymboard_api.model.exercise.Exercise;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmission;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class GymService {
	private final GymRepository gymRepository;
	private final StoredFileRepository fileRepository;
	private final ExerciseRepository exerciseRepository;
	private final ExerciseSubmissionRepository exerciseSubmissionRepository;

	public GymService(GymRepository gymRepository, StoredFileRepository fileRepository, ExerciseRepository exerciseRepository, ExerciseSubmissionRepository exerciseSubmissionRepository) {
		this.gymRepository = gymRepository;
		this.fileRepository = fileRepository;
		this.exerciseRepository = exerciseRepository;
		this.exerciseSubmissionRepository = exerciseSubmissionRepository;
	}

	@Transactional(readOnly = true)
	public GymResponse getGym(RawGymId id) {
		Gym gym = gymRepository.findByRawId(id.gymName(), id.cityCode(), id.countryCode())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new GymResponse(gym);
	}

	@Transactional
	public ExerciseSubmissionResponse createSubmission(RawGymId id, ExerciseSubmissionPayload payload) throws IOException {
		Gym gym = gymRepository.findByRawId(id.gymName(), id.cityCode(), id.countryCode())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Exercise exercise = exerciseRepository.findById(payload.exerciseShortName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
		// TODO: Implement legitimate file storage.
		Path path = Path.of("sample_data", "sample_curl_14kg.MP4");
		StoredFile file = fileRepository.save(new StoredFile(
				"sample_curl_14kg.MP4",
				"video/mp4",
				Files.size(path),
				Files.readAllBytes(path)
		));
		ExerciseSubmission submission = exerciseSubmissionRepository.save(new ExerciseSubmission(
				gym,
				exercise,
				payload.name(),
				BigDecimal.valueOf(payload.weight()),
				file
		));
		return new ExerciseSubmissionResponse(submission);
	}
}
