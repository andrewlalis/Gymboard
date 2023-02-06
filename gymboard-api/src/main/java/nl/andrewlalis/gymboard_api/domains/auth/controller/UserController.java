package nl.andrewlalis.gymboard_api.domains.auth.controller;

import nl.andrewlalis.gymboard_api.domains.auth.dto.*;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.domains.auth.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
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

	@PostMapping(path = "/auth/users/{userId}/followers")
	public ResponseEntity<Void> followUser(@AuthenticationPrincipal User myUser, @PathVariable String userId) {
		userService.followUser(myUser.getId(), userId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping(path = "/auth/users/{userId}/followers")
	public ResponseEntity<Void> unfollowUser(@AuthenticationPrincipal User myUser, @PathVariable String userId) {
		userService.unfollowUser(myUser.getId(), userId);
		return ResponseEntity.ok().build();
	}

	@GetMapping(path = "/auth/me/followers")
	public Page<UserResponse> getFollowers(@AuthenticationPrincipal User user, Pageable pageable) {
		return userService.getFollowers(user.getId(), pageable);
	}

	@GetMapping(path = "/auth/me/following")
	public Page<UserResponse> getFollowing(@AuthenticationPrincipal User user, Pageable pageable) {
		return userService.getFollowing(user.getId(), pageable);
	}
}
