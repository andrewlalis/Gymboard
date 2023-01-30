package nl.andrewlalis.gymboard_api.controller.dto;

public record UserCreationPayload(
		String email,
		String password,
		String name
) {}
