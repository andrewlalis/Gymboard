package nl.andrewlalis.gymboard_api.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public enum LeaderboardTimeframe {
	DAY(Duration.ofDays(1)),
	WEEK(Duration.ofDays(7)),
	MONTH(Duration.ofDays(30)),
	YEAR(Duration.ofDays(365)),
	ALL(Duration.ZERO);

	private final Duration duration;

	LeaderboardTimeframe(Duration duration) {
		this.duration = duration;
	}

	public Optional<LocalDateTime> getCutoffTime(LocalDateTime now) {
		if (this.duration.isZero()) return Optional.empty();
		return Optional.of(now.minus(this.duration));
	}

	public static LeaderboardTimeframe parse(String s, LeaderboardTimeframe defaultValue) {
		if (s == null || s.isBlank()) return defaultValue;
		try {
			return LeaderboardTimeframe.valueOf(s.toUpperCase());
		} catch (IllegalArgumentException e) {
			return defaultValue;
		}
	}
}
