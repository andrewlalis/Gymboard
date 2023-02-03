package nl.andrewlalis.gymboard_api.domains.api.dto;

import nl.andrewlalis.gymboard_api.domains.api.model.GeoPoint;

public record GeoPointResponse(
		double latitude,
		double longitude
) {
	public GeoPointResponse(GeoPoint p) {
		this(
				p.getLatitude().doubleValue(),
				p.getLongitude().doubleValue()
		);
	}
}
