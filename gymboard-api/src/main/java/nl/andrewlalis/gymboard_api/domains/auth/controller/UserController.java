package nl.andrewlalis.gymboard_api.domains.auth.controller;

import nl.andrewlalis.gymboard_api.domains.auth.dto.UserPersonalDetailsPayload;
import nl.andrewlalis.gymboard_api.domains.auth.dto.UserPersonalDetailsResponse;
import nl.andrewlalis.gymboard_api.domains.auth.dto.UserResponse;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.domains.auth.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping(path = "/auth/me/personal-details")
	public UserPersonalDetailsResponse getMyPersonalDetails(@AuthenticationPrincipal User user) {
		return userService.getPersonalDetails(user.getId());
	}

	@PostMapping(path = "/auth/me/personal-details")
	public UserResponse updateMyPersonalDetails(
			@AuthenticationPrincipal User user,
			@RequestBody UserPersonalDetailsPayload payload
	) {
		return userService.updatePersonalDetails(user.getId(), payload);
	}
}
