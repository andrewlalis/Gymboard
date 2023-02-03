package nl.andrewlalis.gymboard_api.domains.api.dto;

import nl.andrewlalis.gymboard_api.domains.api.model.exercise.Exercise;

public record ExerciseResponse(
		String shortName,
		String displayName
) {
	public ExerciseResponse(Exercise exercise) {
		this(exercise.getShortName(), exercise.getDisplayName());
	}
}
