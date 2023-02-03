package nl.andrewlalis.gymboard_api.domains.api.dto;

import nl.andrewlalis.gymboard_api.domains.api.model.exercise.ExerciseSubmission;

import java.time.format.DateTimeFormatter;

public record ExerciseSubmissionResponse(
		String id,
		String createdAt,
		GymSimpleResponse gym,
		ExerciseResponse exercise,
		String videoFileId,
		String submitterName,
		double rawWeight,
		String weightUnit,
		double metricWeight,
		int reps
) {
	public ExerciseSubmissionResponse(ExerciseSubmission submission) {
		this(
				submission.getId(),
				submission.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
				new GymSimpleResponse(submission.getGym()),
				new ExerciseResponse(submission.getExercise()),
				submission.getVideoFileId(),
				submission.getSubmitterName(),
				submission.getRawWeight().doubleValue(),
				submission.getWeightUnit().name(),
				submission.getMetricWeight().doubleValue(),
				submission.getReps()
		);
	}
}