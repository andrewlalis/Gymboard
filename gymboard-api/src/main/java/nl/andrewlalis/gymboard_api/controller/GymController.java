package nl.andrewlalis.gymboard_api.controller;

import nl.andrewlalis.gymboard_api.controller.dto.GymResponse;
import nl.andrewlalis.gymboard_api.service.GymService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for accessing a particular gym.
 */
@RestController
public class GymController {
	private final GymService gymService;

	public GymController(GymService gymService) {
		this.gymService = gymService;
	}

	@GetMapping(path = "/gyms/{countryCode}/{cityCode}/{gymName}")
	public GymResponse getGym(
			@PathVariable String countryCode,
			@PathVariable String cityCode,
			@PathVariable String gymName
	) {
		return gymService.getGym(countryCode, cityCode, gymName);
	}

	@GetMapping(path = "/gyms/search")
	public List<GymResponse> searchGyms(@RequestParam(name = "query", required = false) String query) {
		return gymService.searchGyms(query);
	}
}
