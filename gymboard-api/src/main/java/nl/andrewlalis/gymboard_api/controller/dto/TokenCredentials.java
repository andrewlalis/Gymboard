package nl.andrewlalis.gymboard_api.controller.dto;

public record TokenCredentials(
		String email,
		String password
) {}
