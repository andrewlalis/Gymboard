package nl.andrewlalis.gymboard_api.controller.dto;

import nl.andrewlalis.gymboard_api.model.auth.User;

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
