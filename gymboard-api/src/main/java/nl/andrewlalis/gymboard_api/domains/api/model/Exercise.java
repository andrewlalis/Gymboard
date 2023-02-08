package nl.andrewlalis.gymboard_api.domains.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * An exercise for which attempts can be submitted, and lifts are tracked.
 */
@Entity
@Table(name = "exercise")
public class Exercise {
	@Id
	@Column(nullable = false, length = 127)
	private String shortName;

	@Column(nullable = false, unique = true)
	private String displayName;

	public Exercise() {}

	public Exercise(String shortName, String displayName) {
		this.shortName = shortName;
		this.displayName = displayName;
	}

	public String getShortName() {
		return shortName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
