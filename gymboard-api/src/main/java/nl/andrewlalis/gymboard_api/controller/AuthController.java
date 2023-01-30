package nl.andrewlalis.gymboard_api.controller;

import nl.andrewlalis.gymboard_api.controller.dto.TokenCredentials;
import nl.andrewlalis.gymboard_api.controller.dto.TokenResponse;
import nl.andrewlalis.gymboard_api.service.auth.TokenService;
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
}
