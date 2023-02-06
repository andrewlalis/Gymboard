package nl.andrewlalis.gymboard_api.domains.auth.dto;

import nl.andrewlalis.gymboard_api.domains.auth.model.UserPersonalDetails;

import java.time.format.DateTimeFormatter;

public record UserPersonalDetailsResponse(
		String userId,
		String birthDate,
		float currentWeight,
		String currentWeightUnit,
		float currentMetricWeight,
		String sex
) {
	public UserPersonalDetailsResponse(UserPersonalDetails pd) {
		this(
				pd.getUserId(),
				pd.getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
				pd.getCurrentWeight().floatValue(),
				pd.getCurrentWeightUnit().name(),
				pd.getCurrentMetricWeight().floatValue(),
				pd.getSex().name()
		);
	}
}
