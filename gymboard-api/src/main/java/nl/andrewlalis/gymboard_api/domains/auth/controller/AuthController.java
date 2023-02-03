package nl.andrewlalis.gymboard_api.domains.auth.controller;

import nl.andrewlalis.gymboard_api.domains.auth.dto.*;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.domains.auth.service.TokenService;
import nl.andrewlalis.gymboard_api.domains.auth.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
	private final TokenService tokenService;
	private final UserService userService;

	public AuthController(TokenService tokenService, UserService userService) {
		this.tokenService = tokenService;
		this.userService = userService;
	}

	/**
	 * Endpoint for registering a new user in the system. <strong>This is a
	 * public endpoint.</strong> If the user is successfully created, an email
	 * will be sent to them with a link for activating the account.
	 * @param payload The payload.
	 * @return The created user.
	 */
	@PostMapping(path = "/auth/register")
	public UserResponse registerNewUser(@RequestBody UserCreationPayload payload) {
		return userService.createUser(payload, true);
	}

	/**
	 * Endpoint for activating a new user via an activation code. <strong>This
	 * is a public endpoint.</strong> If the code is recent (within 24 hours)
	 * and the user exists, then they'll be activated and able to log in.
	 * @param payload The payload containing the activation code.
	 * @return The activated user.
	 */
	@PostMapping(path = "/auth/activate")
	public UserResponse activateUser(@RequestBody UserActivationPayload payload) {
		return userService.activateUser(payload);
	}

	/**
	 * Endpoint for obtaining a new access token for a user to access certain
	 * parts of the application. <strong>This is a public endpoint.</strong> If
	 * a token is successfully obtained, it should be provided to all subsequent
	 * requests as an Authorization "Bearer" token.
	 * @param credentials The credentials.
	 * @return The token the client should use.
	 */
	@PostMapping(path = "/auth/token")
	public TokenResponse getToken(@RequestBody TokenCredentials credentials) {
		return tokenService.generateAccessToken(credentials);
	}

	/**
	 * Endpoint that can be used by an authenticated user to fetch a new access
	 * token using their current one; useful for staying logged in beyond the
	 * duration of the initial token's expiration.
	 * @param auth The current authentication.
	 * @return The new token the client should use.
	 */
	@GetMapping(path = "/auth/token")
	public TokenResponse getUpdatedToken(Authentication auth) {
		return tokenService.regenerateAccessToken(auth);
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

	/**
	 * <strong>Public endpoint</strong> for requesting a reset code to be sent
	 * to an account with the given email address.
	 * @param email The email address.
	 * @return An empty 200 OK response.
	 */
	@GetMapping(path = "/auth/reset-password")
	public ResponseEntity<Void> generatePasswordResetCode(@RequestParam String email) {
		userService.generatePasswordResetCode(email);
		return ResponseEntity.ok().build();
	}

	/**
	 * <strong>Public endpoint</strong> for resetting one's password using a
	 * reset code obtained from an email.
	 * @param payload The payload containing the code and new password.
	 * @return An empty 200 OK response.
	 */
	@PostMapping(path = "/auth/reset-password")
	public ResponseEntity<Void> resetPassword(@RequestBody PasswordResetPayload payload) {
		userService.resetUserPassword(payload);
		return ResponseEntity.ok().build();
	}
}
