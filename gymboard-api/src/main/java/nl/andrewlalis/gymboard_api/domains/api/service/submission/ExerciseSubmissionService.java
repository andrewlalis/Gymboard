package nl.andrewlalis.gymboard_api.domains.api.service.submission;

import nl.andrewlalis.gymboard_api.domains.api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.ExerciseRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.submission.SubmissionRepository;
import nl.andrewlalis.gymboard_api.domains.api.dto.*;
import nl.andrewlalis.gymboard_api.domains.api.model.Gym;
import nl.andrewlalis.gymboard_api.domains.api.model.WeightUnit;
import nl.andrewlalis.gymboard_api.domains.api.model.Exercise;
import nl.andrewlalis.gymboard_api.domains.api.model.submission.Submission;
import nl.andrewlalis.gymboard_api.domains.api.service.cdn_client.CdnClient;
import nl.andrewlalis.gymboard_api.domains.api.service.cdn_client.UploadsClient;
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
	private final CdnClient cdnClient;

	public ExerciseSubmissionService(GymRepository gymRepository,
									 UserRepository userRepository, ExerciseRepository exerciseRepository,
									 SubmissionRepository submissionRepository,
									 ULID ulid, CdnClient cdnClient) {
		this.gymRepository = gymRepository;
		this.userRepository = userRepository;
		this.exerciseRepository = exerciseRepository;
		this.submissionRepository = submissionRepository;
		this.ulid = ulid;
		this.cdnClient = cdnClient;
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
		Gym gym = gymRepository.findByCompoundId(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
		if (!user.isActivated()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		Exercise exercise = exerciseRepository.findById(payload.exerciseShortName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid exercise."));

		var validationResponse = validateSubmissionData(gym, user, exercise, payload);
		if (!validationResponse.isValid()) {
			throw new ApiValidationException(validationResponse);
		}

		// Create the submission.
		LocalDateTime performedAt = payload.performedAt();
		if (performedAt == null) performedAt = LocalDateTime.now();
		BigDecimal rawWeight = BigDecimal.valueOf(payload.weight());
		WeightUnit weightUnit = WeightUnit.parse(payload.weightUnit());
		BigDecimal metricWeight = BigDecimal.valueOf(payload.weight());
		if (weightUnit == WeightUnit.POUNDS) {
			metricWeight = WeightUnit.toKilograms(rawWeight);
		}
		Submission submission = submissionRepository.saveAndFlush(new Submission(
				ulid.nextULID(), gym, exercise, user,
				performedAt,
				payload.taskId(),
				rawWeight, weightUnit, metricWeight, payload.reps()
		));
		try {
			cdnClient.uploads.startTask(submission.getVideoProcessingTaskId());
		} catch (Exception e) {
			log.error("Failed to start video processing task for submission " + submission.getId(), e);
		}
		return new SubmissionResponse(submission);
	}

	private ValidationResponse validateSubmissionData(Gym gym, User user, Exercise exercise, SubmissionPayload data) {
		ValidationResponse response = new ValidationResponse();
		LocalDateTime cutoff = LocalDateTime.now().minusDays(3);
		if (data.performedAt() != null && data.performedAt().isAfter(LocalDateTime.now())) {
			response.addMessage("Cannot submit an exercise from the future.");
		}
		if (data.performedAt() != null && data.performedAt().isBefore(cutoff)) {
			response.addMessage("Cannot submit an exercise too far in the past.");
		}
		if (data.reps() < 1 || data.reps() > 500) {
			response.addMessage("Invalid rep count.");
		}
		BigDecimal rawWeight = BigDecimal.valueOf(data.weight());
		WeightUnit weightUnit = WeightUnit.parse(data.weightUnit());
		BigDecimal metricWeight = WeightUnit.toKilograms(rawWeight, weightUnit);

		if (metricWeight.compareTo(BigDecimal.ZERO) <= 0 || metricWeight.compareTo(BigDecimal.valueOf(1000.0)) > 0) {
			response.addMessage("Invalid weight.");
		}

		try {
			var status = cdnClient.uploads.getVideoProcessingTaskStatus(data.taskId());
			if (!status.status().equalsIgnoreCase("NOT_STARTED")) {
				response.addMessage("Invalid video processing task.");
			}
		} catch (Exception e) {
			log.error("Error fetching task status.", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching uploaded video task status.");
		}
		return response;
	}

	@Transactional
	public void deleteSubmission(String submissionId, User user) {
		Submission submission = submissionRepository.findById(submissionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!submission.getUser().getId().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete other user's submission.");
		}
		// TODO: Find a secure way to delete the associated video.
		submissionRepository.delete(submission);
	}
}
