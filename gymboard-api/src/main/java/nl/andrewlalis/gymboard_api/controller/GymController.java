package nl.andrewlalis.gymboard_api.controller;

import nl.andrewlalis.gymboard_api.controller.dto.GymResponse;
import nl.andrewlalis.gymboard_api.service.GymService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for accessing a particular gym.
 */
@RestController
@RequestMapping(path = "/gyms/{countryCode}/{cityCode}/{gymName}")
public class GymController {
	private final GymService gymService;

	public GymController(GymService gymService) {
		this.gymService = gymService;
	}

	@GetMapping
	public GymResponse getGym(
			@PathVariable String countryCode,
			@PathVariable String cityCode,
			@PathVariable String gymName
	) {
		return gymService.getGym(countryCode, cityCode, gymName);
	}
}
