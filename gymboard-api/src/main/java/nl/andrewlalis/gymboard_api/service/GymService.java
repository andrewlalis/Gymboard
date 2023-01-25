package nl.andrewlalis.gymboard_api.service;

import nl.andrewlalis.gymboard_api.controller.dto.ExerciseSubmissionPayload;
import nl.andrewlalis.gymboard_api.controller.dto.ExerciseSubmissionResponse;
import nl.andrewlalis.gymboard_api.controller.dto.GymResponse;
import nl.andrewlalis.gymboard_api.controller.dto.RawGymId;
import nl.andrewlalis.gymboard_api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.dao.StoredFileRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseSubmissionRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseSubmissionTempFileRepository;
import nl.andrewlalis.gymboard_api.model.Gym;
import nl.andrewlalis.gymboard_api.model.StoredFile;
import nl.andrewlalis.gymboard_api.model.exercise.Exercise;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmission;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmissionTempFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class GymService {
	private static final Logger log = LoggerFactory.getLogger(GymService.class);

	private final GymRepository gymRepository;
	private final StoredFileRepository fileRepository;
	private final ExerciseRepository exerciseRepository;
	private final ExerciseSubmissionRepository exerciseSubmissionRepository;
	private final ExerciseSubmissionTempFileRepository tempFileRepository;

	public GymService(GymRepository gymRepository,
					  StoredFileRepository fileRepository,
					  ExerciseRepository exerciseRepository,
					  ExerciseSubmissionRepository exerciseSubmissionRepository,
					  ExerciseSubmissionTempFileRepository tempFileRepository) {
		this.gymRepository = gymRepository;
		this.fileRepository = fileRepository;
		this.exerciseRepository = exerciseRepository;
		this.exerciseSubmissionRepository = exerciseSubmissionRepository;
		this.tempFileRepository = tempFileRepository;
	}

	@Transactional(readOnly = true)
	public GymResponse getGym(RawGymId id) {
		Gym gym = gymRepository.findByRawId(id.gymName(), id.cityCode(), id.countryCode())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new GymResponse(gym);
	}

	/**
	 * Handles the creation of a new exercise submission. This involves a few steps:
	 * <ol>
	 *     <li>Pre-fetch all of the referenced data, like exercise and video file.</li>
	 *     <li>Check that the submission is legitimate.</li>
	 *     <li>Begin video processing.</li>
	 *     <li>Save the submission with the PROCESSING status.</li>
	 * </ol>
	 * Once the asynchronous submission processing is complete, the submission
	 * status will change to COMPLETE.
	 * @param id The gym id.
	 * @param payload The submission data.
	 * @return The saved submission, which will be in the PROCESSING state at first.
	 */
	@Transactional
	public ExerciseSubmissionResponse createSubmission(RawGymId id, ExerciseSubmissionPayload payload) throws IOException {
		Gym gym = gymRepository.findByRawId(id.gymName(), id.cityCode(), id.countryCode())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Exercise exercise = exerciseRepository.findById(payload.exerciseShortName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid exercise."));
		ExerciseSubmissionTempFile tempFile = tempFileRepository.findById(payload.videoId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid video id."));

		// TODO: Validate the submission data.


		// Create the submission.
		ExerciseSubmission submission = exerciseSubmissionRepository.save(new ExerciseSubmission(
				gym,
				exercise,
				payload.name(),
				BigDecimal.valueOf(payload.weight()),
				payload.reps()
		));
		// Then link it to the temporary video file so the async task can find it.
		tempFile.setSubmission(submission);
		tempFileRepository.save(tempFile);

		return new ExerciseSubmissionResponse(submission);
	}

	/**
	 * Asynchronous task that's started after a submission is submitted, which
	 * handles video processing and anything else that might need to be done
	 * before the submission can be marked as COMPLETED.
	 * @param submissionId The submission's id.
	 */
	@Async @Transactional
	public void processSubmission(long submissionId) {
		Optional<ExerciseSubmission> optionalSubmission = exerciseSubmissionRepository.findById(submissionId);
		if (optionalSubmission.isEmpty()) {
			log.warn("Submission id {} is not associated with a submission.", submissionId);
			return;
		}
		ExerciseSubmission submission = optionalSubmission.get();
		Optional<ExerciseSubmissionTempFile> optionalTempFile = tempFileRepository.findBySubmission(submission);
		if (optionalTempFile.isEmpty()) {
			log.warn("Submission {} failed because the temporary video file couldn't be found.", submission.getId());
			submission.setStatus("FAILED");
			return;
		}
		ExerciseSubmissionTempFile tempFile = optionalTempFile.get();

		// TODO: Finish this!
	}
}
