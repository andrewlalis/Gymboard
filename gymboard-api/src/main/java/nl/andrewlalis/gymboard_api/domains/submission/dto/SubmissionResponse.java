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
		ExerciseResponse exercise,
		UserResponse user,
		String performedAt,
		long videoProcessingTaskId,
		String videoFileId,
		String thumbnailFileId,
		double rawWeight,
		String weightUnit,
		double metricWeight,
		int reps,
		boolean verified
) {
	public SubmissionResponse(Submission submission) {
		this(
				submission.getId(),
				StandardDateFormatter.format(submission.getCreatedAt()),
				new GymSimpleResponse(submission.getGym()),
				new ExerciseResponse(submission.getExercise()),
				new UserResponse(submission.getUser()),
				StandardDateFormatter.format(submission.getPerformedAt()),
				submission.getVideoProcessingTaskId(),
				submission.getVideoFileId(),
				submission.getThumbnailFileId(),
				submission.getRawWeight().doubleValue(),
				submission.getWeightUnit().name(),
				submission.getMetricWeight().doubleValue(),
				submission.getReps(),
				submission.isVerified()
		);
	}
}
