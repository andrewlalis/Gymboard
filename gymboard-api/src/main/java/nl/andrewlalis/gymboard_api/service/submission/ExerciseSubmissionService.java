package nl.andrewlalis.gymboard_api.service.submission;

import jakarta.servlet.http.HttpServletResponse;
import nl.andrewlalis.gymboard_api.controller.dto.CompoundGymId;
import nl.andrewlalis.gymboard_api.controller.dto.ExerciseSubmissionPayload;
import nl.andrewlalis.gymboard_api.controller.dto.ExerciseSubmissionResponse;
import nl.andrewlalis.gymboard_api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseSubmissionRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseSubmissionTempFileRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseSubmissionVideoFileRepository;
import nl.andrewlalis.gymboard_api.model.Gym;
import nl.andrewlalis.gymboard_api.model.exercise.Exercise;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmission;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmissionTempFile;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmissionVideoFile;
import nl.andrewlalis.gymboard_api.util.ULID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
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
	private final ExerciseSubmissionTempFileRepository tempFileRepository;
	private final ExerciseSubmissionVideoFileRepository submissionVideoFileRepository;
	private final ULID ulid;

	public ExerciseSubmissionService(GymRepository gymRepository,
									 ExerciseRepository exerciseRepository,
									 ExerciseSubmissionRepository exerciseSubmissionRepository,
									 ExerciseSubmissionTempFileRepository tempFileRepository,
									 ExerciseSubmissionVideoFileRepository submissionVideoFileRepository,
									 ULID ulid) {
		this.gymRepository = gymRepository;
		this.exerciseRepository = exerciseRepository;
		this.exerciseSubmissionRepository = exerciseSubmissionRepository;
		this.tempFileRepository = tempFileRepository;
		this.submissionVideoFileRepository = submissionVideoFileRepository;
		this.ulid = ulid;
	}

	@Transactional(readOnly = true)
	public ExerciseSubmissionResponse getSubmission(String submissionId) {
		ExerciseSubmission submission = exerciseSubmissionRepository.findById(submissionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new ExerciseSubmissionResponse(submission);
	}

	@Transactional(readOnly = true)
	public void streamVideo(String submissionId, HttpServletResponse response) {
		ExerciseSubmissionVideoFile videoFile = submissionVideoFileRepository.findByCompletedSubmissionId(submissionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		response.setContentType(videoFile.getFile().getMimeType());
		response.setContentLengthLong(videoFile.getFile().getSize());
		try {
			response.getOutputStream().write(videoFile.getFile().getContent());
		} catch (IOException e) {
			log.error("Failed to write submission video file to response.", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Handles the creation of a new exercise submission. This involves a few steps:
	 * <ol>
	 *     <li>Pre-fetch all of the referenced data, like exercise and video file.</li>
	 *     <li>Check that the submission is legitimate.</li>
	 *     <li>Save the submission. (With the WAITING status initially.)</li>
	 *     <li>Sometime soon, {@link SubmissionProcessingService#processWaitingSubmissions()} will pick up the submission for processing.</li>
	 * </ol>
	 * Once the asynchronous submission processing is complete, the submission
	 * status will change to COMPLETE.
	 * @param id The gym id.
	 * @param payload The submission data.
	 * @return The saved submission, which will be in the PROCESSING state at first.
	 */
	@Transactional
	public ExerciseSubmissionResponse createSubmission(CompoundGymId id, ExerciseSubmissionPayload payload) {
		Gym gym = gymRepository.findByCompoundId(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Exercise exercise = exerciseRepository.findById(payload.exerciseShortName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid exercise."));
		ExerciseSubmissionTempFile tempFile = tempFileRepository.findById(payload.videoId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid video id."));

		validateSubmission(payload, exercise, tempFile);

		// Create the submission.
		BigDecimal rawWeight = BigDecimal.valueOf(payload.weight());
		ExerciseSubmission.WeightUnit unit = ExerciseSubmission.WeightUnit.valueOf(payload.weightUnit().toUpperCase());
		BigDecimal metricWeight = BigDecimal.valueOf(payload.weight());
		if (unit == ExerciseSubmission.WeightUnit.LBS) {
			metricWeight = metricWeight.multiply(new BigDecimal("0.45359237"));
		}

		ExerciseSubmission submission = exerciseSubmissionRepository.saveAndFlush(new ExerciseSubmission(
				ulid.nextULID(),
				gym,
				exercise,
				payload.name(),
				rawWeight,
				unit,
				metricWeight,
				payload.reps()
		));
		// Then link it to the temporary video file so the async task can find it.
		tempFile.setSubmission(submission);
		tempFileRepository.save(tempFile);
		// The submission will be picked up eventually to be processed.

		return new ExerciseSubmissionResponse(submission);
	}

	private void validateSubmission(ExerciseSubmissionPayload payload, Exercise exercise, ExerciseSubmissionTempFile tempFile) {
		// TODO: Implement this validation.
	}
}
