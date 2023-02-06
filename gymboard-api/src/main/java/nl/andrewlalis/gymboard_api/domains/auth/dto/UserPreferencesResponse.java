package nl.andrewlalis.gymboard_api.domains.auth.dto;

import nl.andrewlalis.gymboard_api.domains.auth.model.UserPreferences;

public record UserPreferencesResponse(
		String userId,
		boolean accountPrivate,
		String locale
) {
	public UserPreferencesResponse(UserPreferences p) {
		this(
				p.getUserId(),
				p.isAccountPrivate(),
				p.getLocale()
		);
	}
}
