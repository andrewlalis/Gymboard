package nl.andrewlalis.gymboard_api.domains.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.util.Objects;

/**
 * Compound primary key used to identify a single {@link Gym}.
 */
@Embeddable
public class GymId implements Serializable {
	@Column(nullable = false, length = 127)
	private String shortName;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private City city;

	public GymId() {}

	public GymId(String shortName, City city) {
		this.shortName = shortName;
		this.city = city;
	}

	public String getShortName() {
		return shortName;
	}

	public City getCity() {
		return city;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GymId gymId)) return false;
		return getShortName().equals(gymId.getShortName()) && getCity().equals(gymId.getCity());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getShortName(), getCity());
	}
}
