package nl.andrewlalis.gymboard_api.controller.dto;

public record ExerciseSubmissionPayload(
		String name,
		String exerciseShortName,
		float weight,
		long videoId
) {}
