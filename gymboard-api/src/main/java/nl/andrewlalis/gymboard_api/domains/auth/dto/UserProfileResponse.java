package nl.andrewlalis.gymboard_api.domains.auth.dto;

/**
 * Response that contains all the information needed to show a user's profile
 * page.
 */
public record UserProfileResponse(
		String id,
		String name,
		String followerCount,
		String followingCount,
		boolean followingThisUser,
		boolean accountPrivate,
		boolean canAccessThisUser
) {}
