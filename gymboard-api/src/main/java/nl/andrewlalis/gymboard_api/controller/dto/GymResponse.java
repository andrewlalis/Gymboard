package nl.andrewlalis.gymboard_api.controller.dto;

import nl.andrewlalis.gymboard_api.model.Gym;

import java.time.format.DateTimeFormatter;

public record GymResponse (
		String countryCode,
		String countryName,
		String cityShortName,
		String cityName,
		String createdAt,
		String displayName,
		String websiteUrl,
		double locationLatitude,
		double locationLongitude,
		String streetAddress
) {
	public GymResponse(Gym gym) {
		this(
				gym.getCity().getCountry().getCode(),
				gym.getCity().getCountry().getName(),
				gym.getCity().getShortName(),
				gym.getCity().getName(),
				gym.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
				gym.getDisplayName(),
				gym.getWebsiteUrl(),
				gym.getLocation().getLatitude().doubleValue(),
				gym.getLocation().getLongitude().doubleValue(),
				gym.getStreetAddress()
		);
	}
}
