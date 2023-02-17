package nl.andrewlalis.gymboard_api.domains.auth.dto;

public record UserRelationshipResponse(
		boolean following,
		boolean followedBy
) {}
