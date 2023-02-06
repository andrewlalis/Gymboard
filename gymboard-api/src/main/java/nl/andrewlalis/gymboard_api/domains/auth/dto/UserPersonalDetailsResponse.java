package nl.andrewlalis.gymboard_api.domains.auth.dto;

import nl.andrewlalis.gymboard_api.domains.auth.model.UserPersonalDetails;

import java.time.format.DateTimeFormatter;

public record UserPersonalDetailsResponse(
		String userId,
		String birthDate,
		Float currentWeight,
		String currentWeightUnit,
		Float currentMetricWeight,
		String sex
) {
	public UserPersonalDetailsResponse(UserPersonalDetails pd) {
		this(
				pd.getUserId(),
				pd.getBirthDate() == null ? null : pd.getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
				pd.getCurrentWeight() == null ? null : pd.getCurrentWeight().floatValue(),
				pd.getCurrentWeightUnit() == null ? null : pd.getCurrentWeightUnit().name(),
				pd.getCurrentMetricWeight() == null ? null : pd.getCurrentMetricWeight().floatValue(),
				pd.getSex().name()
		);
	}
}
