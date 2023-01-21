package nl.andrewlalis.gymboard_api.controller.dto;

import nl.andrewlalis.gymboard_api.model.Gym;

import java.time.format.DateTimeFormatter;

public record GymResponse (
		String countryCode,
		String countryName,
		String cityShortName,
		String cityName,
		String createdAt,
		String shortName,
		String displayName,
		String websiteUrl,
		GeoPointResponse location,
		String streetAddress
) {
	public GymResponse(Gym gym) {
		this(
				gym.getCity().getCountry().getCode(),
				gym.getCity().getCountry().getName(),
				gym.getCity().getShortName(),
				gym.getCity().getName(),
				gym.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
				gym.getShortName(),
				gym.getDisplayName(),
				gym.getWebsiteUrl(),
				new GeoPointResponse(gym.getLocation()),
				gym.getStreetAddress()
		);
	}
}
