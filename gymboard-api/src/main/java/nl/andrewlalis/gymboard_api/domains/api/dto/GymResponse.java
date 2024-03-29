package nl.andrewlalis.gymboard_api.domains.api.dto;

import nl.andrewlalis.gymboard_api.domains.api.model.Gym;
import nl.andrewlalis.gymboard_api.util.StandardDateFormatter;

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
				StandardDateFormatter.format(gym.getCreatedAt()),
				gym.getShortName(),
				gym.getDisplayName(),
				gym.getWebsiteUrl(),
				new GeoPointResponse(gym.getLocation()),
				gym.getStreetAddress()
		);
	}
}
