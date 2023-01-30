package nl.andrewlalis.gymboard_api.controller;

import nl.andrewlalis.gymboard_api.controller.dto.TokenCredentials;
import nl.andrewlalis.gymboard_api.controller.dto.TokenResponse;
import nl.andrewlalis.gymboard_api.controller.dto.UserResponse;
import nl.andrewlalis.gymboard_api.model.auth.User;
import nl.andrewlalis.gymboard_api.service.auth.TokenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
	private final TokenService tokenService;

	public AuthController(TokenService tokenService) {
		this.tokenService = tokenService;
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
}
