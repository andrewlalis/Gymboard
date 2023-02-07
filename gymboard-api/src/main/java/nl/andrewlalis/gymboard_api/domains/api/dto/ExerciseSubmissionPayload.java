package nl.andrewlalis.gymboard_api.domains.api.dto;

public record ExerciseSubmissionPayload(
		String exerciseShortName,
		float weight,
		String weightUnit,
		int reps,
		String videoFileId
) {}
