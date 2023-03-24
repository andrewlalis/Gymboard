package nl.andrewlalis.gymboard_api.domains.auth.dto;

/**
 * A response that's sent when a user requests to follow another.
 */
public record UserFollowResponse(
		String result
) {
	private static final String RESULT_FOLLOWED = "FOLLOWED";
	private static final String RESULT_REQUESTED = "REQUESTED";
	private static final String RESULT_ALREADY_FOLLOWED = "ALREADY_FOLLOWED";

	public static UserFollowResponse followed() {
		return new UserFollowResponse(RESULT_FOLLOWED);
	}

	public static UserFollowResponse requested() {
		return new UserFollowResponse(RESULT_REQUESTED);
	}

	public static UserFollowResponse alreadyFollowed() {
		return new UserFollowResponse(RESULT_ALREADY_FOLLOWED);
	}
}
