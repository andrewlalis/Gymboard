package nl.andrewlalis.gymboard_api.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * The API-standard formatter for date-time objects that are sent as responses
 * where we need to enforce a specific format.
 */
public class StandardDateFormatter {
	public static String format(LocalDateTime utcTimestamp) {
		return utcTimestamp.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}
}
