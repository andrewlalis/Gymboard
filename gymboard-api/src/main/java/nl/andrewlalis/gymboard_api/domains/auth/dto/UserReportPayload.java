package nl.andrewlalis.gymboard_api.domains.auth.dto;

public record UserReportPayload(
		String reason,
		String description
) {}
