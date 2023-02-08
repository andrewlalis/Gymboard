package nl.andrewlalis.gymboard_api.domains.api.service.submission;

import nl.andrewlalis.gymboard_api.domains.api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.ExerciseRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.submission.SubmissionRepository;
import nl.andrewlalis.gymboard_api.domains.api.dto.CompoundGymId;
import nl.andrewlalis.gymboard_api.domains.api.dto.SubmissionPayload;
import nl.andrewlalis.gymboard_api.domains.api.dto.SubmissionResponse;
import nl.andrewlalis.gymboard_api.domains.api.model.Gym;
import nl.andrewlalis.gymboard_api.domains.api.model.WeightUnit;
import nl.andrewlalis.gymboard_api.domains.api.model.Exercise;
import nl.andrewlalis.gymboard_api.domains.api.model.submission.Submission;
import nl.andrewlalis.gymboard_api.domains.auth.dao.UserRepository;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.util.ULID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service which handles the rather mundane tasks associated with exercise
 * submissions, like CRUD and fetching video data.
 */
@Service
public class ExerciseSubmissionService {
	private static final Logger log = LoggerFactory.getLogger(ExerciseSubmissionService.class);

	private final GymRepository gymRepository;
	private final UserRepository userRepository;
	private final ExerciseRepository exerciseRepository;
	private final SubmissionRepository submissionRepository;
	private final ULID ulid;

	public ExerciseSubmissionService(GymRepository gymRepository,
									 UserRepository userRepository, ExerciseRepository exerciseRepository,
									 SubmissionRepository submissionRepository,
									 ULID ulid) {
		this.gymRepository = gymRepository;
		this.userRepository = userRepository;
		this.exerciseRepository = exerciseRepository;
		this.submissionRepository = submissionRepository;
		this.ulid = ulid;
	}

	@Transactional(readOnly = true)
	public SubmissionResponse getSubmission(String submissionId) {
		Submission submission = submissionRepository.findById(submissionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new SubmissionResponse(submission);
	}

	/**
	 * Handles the creation of a new exercise submission.
	 * @param id The gym id.
	 * @param userId The user's id.
	 * @param payload The submission data.
	 * @return The saved submission.
	 */
	@Transactional
	public SubmissionResponse createSubmission(CompoundGymId id, String userId, SubmissionPayload payload) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
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
		Submission submission = submissionRepository.saveAndFlush(new Submission(
				ulid.nextULID(),
				gym,
				exercise,
				user,
				LocalDateTime.now(),
				payload.videoFileId(),
				rawWeight,
				weightUnit,
				metricWeight,
				payload.reps()
		));
		return new SubmissionResponse(submission);
	}
}
