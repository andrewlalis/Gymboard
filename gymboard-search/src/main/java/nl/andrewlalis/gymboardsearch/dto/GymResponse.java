package nl.andrewlalis.gymboardsearch.dto;

import org.apache.lucene.document.Document;

public record GymResponse(
		String compoundId,
		String shortName,
		String displayName,
		String cityShortName,
		String cityName,
		String countryCode,
		String countryName,
		String streetAddress,
		double latitude,
		double longitude
) {
	public GymResponse(Document doc) {
		this(
				doc.get("compound_id"),
				doc.get("short_name"),
				doc.get("display_name"),
				doc.get("city_short_name"),
				doc.get("city_name"),
				doc.get("country_code"),
				doc.get("country_name"),
				doc.get("street_address"),
				doc.getField("latitude").numericValue().doubleValue(),
				doc.getField("longitude").numericValue().doubleValue()
		);
	}
}
