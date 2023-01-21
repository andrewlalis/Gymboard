package nl.andrewlalis.gymboard_api.controller.dto;

import nl.andrewlalis.gymboard_api.model.GeoPoint;

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
