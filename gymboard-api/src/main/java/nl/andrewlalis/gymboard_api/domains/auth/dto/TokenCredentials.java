package nl.andrewlalis.gymboard_api.domains.auth.dto;

public record TokenCredentials(
		String email,
		String password
) {}
