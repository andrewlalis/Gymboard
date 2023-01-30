package nl.andrewlalis.gymboard_api.controller;

import nl.andrewlalis.gymboard_api.controller.dto.TokenCredentials;
import nl.andrewlalis.gymboard_api.controller.dto.TokenResponse;
import nl.andrewlalis.gymboard_api.controller.dto.UserResponse;
import nl.andrewlalis.gymboard_api.model.auth.User;
import nl.andrewlalis.gymboard_api.service.auth.TokenService;
import org.springframework.security.access.prepost.PreAuthorize;
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

	@PostMapping(path = "/auth/token")
	public TokenResponse getToken(@RequestBody TokenCredentials credentials) {
		return tokenService.generateAccessToken(credentials);
	}

	@GetMapping(path = "/auth/me")
	@PreAuthorize("isAuthenticated()")
	public UserResponse getMyUser(@AuthenticationPrincipal User user) {
		return new UserResponse(user);
	}
}
