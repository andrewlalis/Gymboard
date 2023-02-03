package nl.andrewlalis.gymboard_api.domains.auth.dto;

import nl.andrewlalis.gymboard_api.domains.auth.model.User;

public record UserResponse(
		String id,
		boolean activated,
		String email,
		String name
) {
	public UserResponse(User user) {
		this(
				user.getId(),
				user.isActivated(),
				user.getEmail(),
				user.getName()
		);
	}
}
