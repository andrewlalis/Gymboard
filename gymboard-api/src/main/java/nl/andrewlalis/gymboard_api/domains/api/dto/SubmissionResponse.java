package nl.andrewlalis.gymboard_api.domains.api.dto;

import nl.andrewlalis.gymboard_api.domains.api.model.submission.Submission;
import nl.andrewlalis.gymboard_api.domains.auth.dto.UserResponse;
import nl.andrewlalis.gymboard_api.util.StandardDateFormatter;

public record SubmissionResponse(
		String id,
		String createdAt,
		GymSimpleResponse gym,
		ExerciseResponse exercise,
		UserResponse user,
		String performedAt,
		String videoFileId,
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
				new ExerciseResponse(submission.getExercise()),
				new UserResponse(submission.getUser()),
				StandardDateFormatter.format(submission.getPerformedAt()),
				submission.getVideoFileId(),
				submission.getRawWeight().doubleValue(),
				submission.getWeightUnit().name(),
				submission.getMetricWeight().doubleValue(),
				submission.getReps()
		);
	}
}
