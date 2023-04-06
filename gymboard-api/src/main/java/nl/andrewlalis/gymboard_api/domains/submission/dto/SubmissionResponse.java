package nl.andrewlalis.gymboard_api.domains.submission.dto;

import nl.andrewlalis.gymboard_api.domains.api.dto.ExerciseResponse;
import nl.andrewlalis.gymboard_api.domains.api.dto.GymSimpleResponse;
import nl.andrewlalis.gymboard_api.domains.submission.model.Submission;
import nl.andrewlalis.gymboard_api.domains.auth.dto.UserResponse;
import nl.andrewlalis.gymboard_api.util.StandardDateFormatter;

public record SubmissionResponse(
		String id,
		String createdAt,
		GymSimpleResponse gym,
		UserResponse user,
		long videoProcessingTaskId,
		String videoFileId,
		String thumbnailFileId,
		boolean processing,
		boolean verified,

		// From SubmissionProperties
		ExerciseResponse exercise,
		String performedAt,
		double rawWeight,
		String weightUnit,
		double metricWeight,
		int reps
) {
	public SubmissionResponse(Submission submission) {
		this(
				submission.getId(),
				StandardDateFormatter.format(submission.getCreatedAt()),
				new GymSimpleResponse(submission.getGym()),
				new UserResponse(submission.getUser()),
				submission.getVideoProcessingTaskId(),
				submission.getVideoFileId(),
				submission.getThumbnailFileId(),
				submission.isProcessing(),
				submission.isVerified(),

				new ExerciseResponse(submission.getProperties().getExercise()),
				StandardDateFormatter.format(submission.getCreatedAt()),
				submission.getProperties().getRawWeight().doubleValue(),
				submission.getProperties().getWeightUnit().name(),
				submission.getProperties().getMetricWeight().doubleValue(),
				submission.getProperties().getReps()
		);
	}
}
