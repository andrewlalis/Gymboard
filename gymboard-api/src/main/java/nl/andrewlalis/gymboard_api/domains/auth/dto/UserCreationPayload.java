package nl.andrewlalis.gymboard_api.domains.auth.dto;

public record UserCreationPayload(
		String email,
		String password,
		String name
) {}
