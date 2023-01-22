package nl.andrewlalis.gymboard_api.controller.dto;

import nl.andrewlalis.gymboard_api.model.exercise.Exercise;

public record ExerciseResponse(
		String shortName,
		String displayName
) {
	public ExerciseResponse(Exercise exercise) {
		this(exercise.getShortName(), exercise.getDisplayName());
	}
}
