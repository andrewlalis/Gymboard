package nl.andrewlalis.gymboard_api.domains.auth.dto;

public record UserPreferencesPayload(
		boolean accountPrivate,
		String locale
) {}
