package nl.andrewlalis.gymboard_api.domains.api.service.submission;

import nl.andrewlalis.gymboard_api.domains.api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.ExerciseRepository;
import nl.andrewlalis.gymboard_api.domains.submission.dao.SubmissionRepository;
import nl.andrewlalis.gymboard_api.domains.api.dto.*;
import nl.andrewlalis.gymboard_api.domains.api.model.Gym;
import nl.andrewlalis.gymboard_api.domains.api.model.WeightUnit;
import nl.andrewlalis.gymboard_api.domains.api.model.Exercise;
import nl.andrewlalis.gymboard_api.domains.submission.dto.SubmissionPayload;
import nl.andrewlalis.gymboard_api.domains.submission.dto.SubmissionResponse;
import nl.andrewlalis.gymboard_api.domains.submission.model.Submission;
import nl.andrewlalis.gymboard_api.domains.api.service.cdn_client.CdnClient;
import nl.andrewlalis.gymboard_api.domains.auth.dao.UserRepository;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.domains.submission.model.SubmissionProperties;
import nl.andrewlalis.gymboard_api.util.ULID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

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
		SubmissionProperties properties = new SubmissionProperties(
				exercise,
				performedAt,
				rawWeight,
				weightUnit,
				payload.reps()
		);

		Submission submission = new Submission(ulid.nextULID(), gym, user, payload.taskId(), properties);
		try {
			cdnClient.uploads.startTask(submission.getVideoProcessingTaskId());
			submission.setProcessing(true);
		} catch (Exception e) {
			log.error("Failed to start video processing task for submission.", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to start video processing.");
		}
		submission = submissionRepository.save(submission);
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
			if (status == null || !status.status().equalsIgnoreCase("NOT_STARTED")) {
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
		try {

			if (submission.getVideoFileId() != null) {
				cdnClient.files.deleteFile(submission.getVideoFileId());
			}
			if (submission.getThumbnailFileId() != null) {
				cdnClient.files.deleteFile(submission.getThumbnailFileId());
			}
		} catch (Exception e) {
			log.error("Couldn't delete CDN content for submission " + submissionId, e);
		}
		submissionRepository.delete(submission);
	}

	/**
	 * This method is invoked when the CDN calls this API's endpoint to notify
	 * us that a video processing task has completed. If the task completed
	 * successfully, we can set any related submissions' video and thumbnail
	 * file ids and remove its "processing" flag. Otherwise, we should delete
	 * the failed submission.
	 * @param payload The information about the task.
	 */
	@Transactional
	public void handleVideoProcessingComplete(VideoProcessingCompletePayload payload) {
		var submissionsToUpdate = submissionRepository.findUnprocessedByTaskId(payload.taskId());
		log.info("Received video processing complete message from CDN: {}, affecting {} submissions.", payload, submissionsToUpdate.size());
		for (var submission : submissionsToUpdate) {
			if (payload.status().equalsIgnoreCase("COMPLETE")) {
				submission.setVideoFileId(payload.videoFileId());
				submission.setThumbnailFileId(payload.thumbnailFileId());
				submission.setProcessing(false);
				submissionRepository.save(submission);
				// TODO: Send notification of successful processing to the user!
			} else if (payload.status().equalsIgnoreCase("FAILED")) {
				submissionRepository.delete(submission);
				// TODO: Send notification of failed video processing to the user!
			}
		}
	}

	/**
	 * A scheduled task that checks and resolves issues with any submission that
	 * stays in the "processing" state for too long.
	 * TODO: Find some way to clean up this mess of logic!
	 */
	@Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
	public void checkProcessingSubmissions() {
		var processingSubmissions = submissionRepository.findAllByProcessingTrue();
		LocalDateTime actionCutoff = LocalDateTime.now().minus(Duration.ofMinutes(10));
		LocalDateTime deleteCutoff = LocalDateTime.now().minus(Duration.ofMinutes(30));
		for (var submission : processingSubmissions) {
			if (submission.getCreatedAt().isBefore(actionCutoff)) {
				// Sanity check to remove any inconsistent submission that doesn't have a task id for whatever reason.
				if (submission.getVideoProcessingTaskId() == null) {
					log.warn(
							"Removing long-processing submission {} for user {} because it doesn't have a task id.",
							submission.getId(), submission.getUser().getEmail()
					);
					submissionRepository.delete(submission);
					// TODO: Send notification to user.
					continue;
				}

				try {
					var status = cdnClient.uploads.getVideoProcessingTaskStatus(submission.getVideoProcessingTaskId());
					if (status == null) {
						// The task no longer exists on the CDN, so remove the submission.
						log.warn(
								"Removing long-processing submission {} for user {} because its task no longer exists on the CDN.",
								submission.getId(), submission.getUser().getEmail()
						);
						submissionRepository.delete(submission);
						// TODO: Send notification to user.
					} else if (status.status().equalsIgnoreCase("FAILED")) {
						// The task failed, so we should remove the submission.
						log.warn(
								"Removing long-processing submission {} for user {} because its task failed.",
								submission.getId(), submission.getUser().getEmail()
						);
						submissionRepository.delete(submission);
						// TODO: Send notification to user.
					} else if (status.status().equalsIgnoreCase("COMPLETED")) {
						// The submission should be marked as complete.
						submission.setVideoFileId(status.videoFileId());
						submission.setThumbnailFileId(status.thumbnailFileId());
						submission.setProcessing(false);
						submissionRepository.save(submission);
						// TODO: Send notification to user.
					} else if (status.status().equalsIgnoreCase("NOT_STARTED")) {
						// If for whatever reason the submission's video processing never started, start now.
						try {
							cdnClient.uploads.startTask(submission.getVideoProcessingTaskId());
						} catch (Exception e) {
							log.error("Failed to start processing task " + submission.getVideoProcessingTaskId(), e);
							if (submission.getCreatedAt().isBefore(deleteCutoff)) {
								log.warn(
										"Removing long-processing submission {} for user {} because it is waiting or processing for too long.",
										submission.getId(), submission.getUser().getEmail()
								);
								submissionRepository.delete(submission);
								// TODO: Send notification to user.
							}
						}
					} else {
						// The task is waiting or processing, so delete the submission if it's been in that state for an unreasonably long time.
						if (submission.getCreatedAt().isBefore(deleteCutoff)) {
							log.warn(
									"Removing long-processing submission {} for user {} because it is waiting or processing for too long.",
									submission.getId(), submission.getUser().getEmail()
							);
							submissionRepository.delete(submission);
							// TODO: Send notification to user.
						}
					}
				} catch (Exception e) {
					log.error("Couldn't fetch status of long-processing submission " + submission.getId() + " for user " + submission.getUser().getEmail(), e);
					// We can't reliably remove this submission yet, so we'll try again on the next pass.
					if (submission.getCreatedAt().isBefore(deleteCutoff)) {
						log.warn(
								"Removing long-processing submission {} for user {} because it is waiting or processing for too long.",
								submission.getId(), submission.getUser().getEmail()
						);
						submissionRepository.delete(submission);
						// TODO: Send notification to user.
					}
				}
			}
		}
	}
}
