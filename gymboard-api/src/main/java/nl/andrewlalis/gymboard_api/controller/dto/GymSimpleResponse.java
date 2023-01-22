package nl.andrewlalis.gymboard_api.controller.dto;

import nl.andrewlalis.gymboard_api.model.Gym;

public record GymSimpleResponse(
		String countryCode,
		String cityShortName,
		String shortName,
		String displayName
) {
	public GymSimpleResponse(Gym gym) {
		this(
				gym.getCity().getCountry().getCode(),
				gym.getCity().getShortName(),
				gym.getShortName(),
				gym.getDisplayName()
		);
	}
}
