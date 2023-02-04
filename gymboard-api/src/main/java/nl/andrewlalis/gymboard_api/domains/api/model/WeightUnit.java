package nl.andrewlalis.gymboard_api.domains.api.model;

import java.math.BigDecimal;

public enum WeightUnit {
	KILOGRAMS,
	POUNDS;

	public static WeightUnit parse(String s) {
		if (s == null || s.isBlank()) return KILOGRAMS;
		s = s.strip().toUpperCase();
		if (s.equals("LB") || s.equals("LBS") || s.equals("POUND") || s.equals("POUNDS")) {
			return POUNDS;
		}
		return KILOGRAMS;
	}

	public static BigDecimal toKilograms(BigDecimal pounds) {
		BigDecimal metric = new BigDecimal("0.45359237");
		return metric.multiply(pounds);
	}
}
