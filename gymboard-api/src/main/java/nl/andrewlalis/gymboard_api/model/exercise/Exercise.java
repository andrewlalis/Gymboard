package nl.andrewlalis.gymboard_api.model.exercise;

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
	private String shortname;

	@Column(nullable = false, unique = true)
	private String name;

	public Exercise() {}

	public Exercise(String shortname, String name) {
		this.shortname = shortname;
		this.name = name;
	}

	public String getShortname() {
		return shortname;
	}

	public String getName() {
		return name;
	}
}
