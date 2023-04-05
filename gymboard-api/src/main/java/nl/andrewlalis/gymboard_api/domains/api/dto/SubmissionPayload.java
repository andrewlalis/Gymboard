package nl.andrewlalis.gymboard_api.domains.api.dto;

import java.time.LocalDateTime;

public record SubmissionPayload(
		String exerciseShortName,
		LocalDateTime performedAt,
		float weight,
		String weightUnit,
		int reps,
		long taskId
) {}
