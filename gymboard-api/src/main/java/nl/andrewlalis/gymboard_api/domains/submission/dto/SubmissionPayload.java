package nl.andrewlalis.gymboard_api.domains.submission.dto;

public record SubmissionPayload(
		String exerciseShortName,
		String performedAt,
		float weight,
		String weightUnit,
		int reps,
		long taskId
) {}
