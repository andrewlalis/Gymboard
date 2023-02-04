package nl.andrewlalis.gymboard_api.domains.api.service.submission;

import nl.andrewlalis.gymboard_api.domains.api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.exercise.ExerciseRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.exercise.ExerciseSubmissionRepository;
import nl.andrewlalis.gymboard_api.domains.api.dto.CompoundGymId;
import nl.andrewlalis.gymboard_api.domains.api.dto.ExerciseSubmissionPayload;
import nl.andrewlalis.gymboard_api.domains.api.dto.ExerciseSubmissionResponse;
import nl.andrewlalis.gymboard_api.domains.api.model.Gym;
import nl.andrewlalis.gymboard_api.domains.api.model.WeightUnit;
import nl.andrewlalis.gymboard_api.domains.api.model.exercise.Exercise;
import nl.andrewlalis.gymboard_api.domains.api.model.exercise.ExerciseSubmission;
import nl.andrewlalis.gymboard_api.util.ULID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

/**
 * Service which handles the rather mundane tasks associated with exercise
 * submissions, like CRUD and fetching video data.
 */
@Service
public class ExerciseSubmissionService {
	private static final Logger log = LoggerFactory.getLogger(ExerciseSubmissionService.class);

	private final GymRepository gymRepository;
	private final ExerciseRepository exerciseRepository;
	private final ExerciseSubmissionRepository exerciseSubmissionRepository;
	private final ULID ulid;

	public ExerciseSubmissionService(GymRepository gymRepository,
									 ExerciseRepository exerciseRepository,
									 ExerciseSubmissionRepository exerciseSubmissionRepository,
									 ULID ulid) {
		this.gymRepository = gymRepository;
		this.exerciseRepository = exerciseRepository;
		this.exerciseSubmissionRepository = exerciseSubmissionRepository;
		this.ulid = ulid;
	}

	@Transactional(readOnly = true)
	public ExerciseSubmissionResponse getSubmission(String submissionId) {
		ExerciseSubmission submission = exerciseSubmissionRepository.findById(submissionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new ExerciseSubmissionResponse(submission);
	}

	/**
	 * Handles the creation of a new exercise submission.
	 * @param id The gym id.
	 * @param payload The submission data.
	 * @return The saved submission.
	 */
	@Transactional
	public ExerciseSubmissionResponse createSubmission(CompoundGymId id, ExerciseSubmissionPayload payload) {
		Gym gym = gymRepository.findByCompoundId(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Exercise exercise = exerciseRepository.findById(payload.exerciseShortName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid exercise."));

		// TODO: Validate the submission data.

		// Create the submission.
		BigDecimal rawWeight = BigDecimal.valueOf(payload.weight());
		WeightUnit weightUnit = WeightUnit.parse(payload.weightUnit());
		BigDecimal metricWeight = BigDecimal.valueOf(payload.weight());
		if (weightUnit == WeightUnit.POUNDS) {
			metricWeight = WeightUnit.toKilograms(rawWeight);
		}
		ExerciseSubmission submission = exerciseSubmissionRepository.saveAndFlush(new ExerciseSubmission(
				ulid.nextULID(),
				gym,
				exercise,
				payload.videoFileId(),
				payload.name(),
				rawWeight,
				weightUnit,
				metricWeight,
				payload.reps()
		));
		return new ExerciseSubmissionResponse(submission);
	}
}
