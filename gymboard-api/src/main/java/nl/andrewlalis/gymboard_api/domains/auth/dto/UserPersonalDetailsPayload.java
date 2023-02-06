package nl.andrewlalis.gymboard_api.domains.auth.dto;

import java.time.LocalDate;

public record UserPersonalDetailsPayload(
		LocalDate birthDate,
		Float currentWeight,
		String currentWeightUnit,
		String sex
) {}
