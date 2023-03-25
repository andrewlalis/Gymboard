package nl.andrewlalis.gymboard_api.domains.auth.controller;

import nl.andrewlalis.gymboard_api.domains.auth.dto.*;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.domains.auth.model.UserPreferences;
import nl.andrewlalis.gymboard_api.domains.auth.service.UserAccessService;
import nl.andrewlalis.gymboard_api.domains.auth.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
	private final UserService userService;
	private final UserAccessService userAccessService;

	public UserController(UserService userService, UserAccessService userAccessService) {
		this.userService = userService;
		this.userAccessService = userAccessService;
	}

	/**
	 * Gets information about the user, as determined by the provided access
	 * token.
	 * @param user The user that requested information.
	 * @return The user data.
	 */
	@GetMapping(path = "/auth/me")
	public UserResponse getMyUser(@AuthenticationPrincipal User user) {
		return new UserResponse(user);
	}

	@GetMapping(path = "/auth/users/{userId}")
	public UserResponse getUser(@PathVariable String userId) {
		return userService.getUser(userId);
	}

	/**
	 * Gets all the information that the app will typically need to display a
	 * user's profile page. Some information may be omitted if the user has
	 * set their {@link UserPreferences#isAccountPrivate()}
	 * to true, and the requesting user isn't following them.
	 * @param userId The id of the user to fetch profile information for.
	 * @return The user's profile information.
	 */
	@GetMapping(path = "/auth/users/{userId}/profile")
	public UserProfileResponse getUserProfile(@PathVariable String userId) {
		return userService.getProfile(userId);
	}

	@GetMapping(path = "/auth/users/{userId}/access")
	public UserAccessResponse getUserAccess(@PathVariable String userId) {
		return new UserAccessResponse(userAccessService.currentUserHasAccess(userId));
	}

	/**
	 * Endpoint for updating one's own password.
	 * @param user The user that's updating their password.
	 * @param payload The payload with the new password.
	 * @return An empty 200 OK response.
	 */
	@PostMapping(path = "/auth/me/password")
	public ResponseEntity<Void> updateMyPassword(@AuthenticationPrincipal User user, @RequestBody PasswordUpdatePayload payload) {
		userService.updatePassword(user.getId(), payload);
		return ResponseEntity.ok().build();
	}

	@PostMapping(path = "/auth/me/email-reset-code")
	public ResponseEntity<Void> generateEmailResetCode(@AuthenticationPrincipal User user, @RequestBody EmailUpdatePayload payload) {
		userService.generateEmailResetCode(user.getId(), payload);
		return ResponseEntity.ok().build();
	}

	@PostMapping(path = "/auth/me/email")
	public ResponseEntity<Void> updateMyEmail(@AuthenticationPrincipal User user, @RequestParam String code) {
		userService.updateEmail(user.getId(), code);
		return ResponseEntity.ok().build();
	}

	@GetMapping(path = "/auth/me/personal-details")
	public UserPersonalDetailsResponse getMyPersonalDetails(@AuthenticationPrincipal User user) {
		return userService.getPersonalDetails(user.getId());
	}

	@PostMapping(path = "/auth/me/personal-details")
	public UserPersonalDetailsResponse updateMyPersonalDetails(
			@AuthenticationPrincipal User user,
			@RequestBody UserPersonalDetailsPayload payload
	) {
		return userService.updatePersonalDetails(user.getId(), payload);
	}

	@GetMapping(path = "/auth/me/preferences")
	public UserPreferencesResponse getMyPreferences(@AuthenticationPrincipal User user) {
		return userService.getPreferences(user.getId());
	}

	@PostMapping(path = "/auth/me/preferences")
	public UserPreferencesResponse updateMyPreferences(
			@AuthenticationPrincipal User user,
			@RequestBody UserPreferencesPayload payload
	) {
		return userService.updatePreferences(user.getId(), payload);
	}

	@GetMapping(path = "/auth/users/{user1Id}/relationship-to/{user2Id}")
	public UserRelationshipResponse getRelationship(@PathVariable String user1Id, @PathVariable String user2Id) {
		return userService.getRelationship(user1Id, user2Id);
	}

	@PostMapping(path = "/auth/users/{userId}/followers")
	public UserFollowResponse followUser(@AuthenticationPrincipal User myUser, @PathVariable String userId) {
		return userService.followUser(myUser.getId(), userId);
	}

	@DeleteMapping(path = "/auth/users/{userId}/followers")
	public ResponseEntity<Void> unfollowUser(@AuthenticationPrincipal User myUser, @PathVariable String userId) {
		userService.unfollowUser(myUser.getId(), userId);
		return ResponseEntity.ok().build();
	}

	@PostMapping(path = "/auth/me/follow-requests/{followRequestId}")
	public ResponseEntity<Void> respondToFollowRequest(
			@AuthenticationPrincipal User myUser,
			@PathVariable long followRequestId,
			@RequestBody UserFollowRequestApproval payload
	) {
		userService.respondToFollowRequest(myUser.getId(), followRequestId, payload.approve());
		return ResponseEntity.ok().build();
	}

	@GetMapping(path = "/auth/me/followers")
	public Page<UserResponse> getMyFollowers(@AuthenticationPrincipal User user, Pageable pageable) {
		return userService.getFollowers(user.getId(), pageable);
	}

	@GetMapping(path = "/auth/me/following")
	public Page<UserResponse> getMyFollowing(@AuthenticationPrincipal User user, Pageable pageable) {
		return userService.getFollowing(user.getId(), pageable);
	}

	@GetMapping(path = "/auth/users/{userId}/followers")
	public Page<UserResponse> getUserFollowers(@PathVariable String userId, Pageable pageable) {
		return userService.getFollowers(userId, pageable);
	}

	@GetMapping(path = "/auth/users/{userId}/following")
	public Page<UserResponse> getUserFollowing(@PathVariable String userId, Pageable pageable) {
		return userService.getFollowing(userId, pageable);
	}

	@PostMapping(path = "/auth/users/{userId}/reports")
	public ResponseEntity<Void> reportUser(@PathVariable String userId, @RequestBody UserReportPayload payload) {
		userService.reportUser(userId, payload);
		return ResponseEntity.ok().build();
	}
}
