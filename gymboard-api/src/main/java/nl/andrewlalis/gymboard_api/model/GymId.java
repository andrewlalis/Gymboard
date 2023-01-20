package nl.andrewlalis.gymboard_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Compound primary key used to identify a single {@link Gym}.
 */
@Embeddable
public class GymId implements Serializable {
	@Column(nullable = false, length = 127)
	private String name;
	@Column(nullable = false, length = 127)
	private String cityId;
	@Column(nullable = false, length = 2)
	private String countryId;

	public GymId() {}

	public GymId(String name, String cityId, String countryId) {
		this.name = name;
		this.cityId = cityId;
		this.countryId = countryId;
	}

	public String getName() {
		return name;
	}

	public String getCityId() {
		return cityId;
	}

	public String getCountryId() {
		return countryId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof GymId gymId) {
			return getName().equals(gymId.getName()) &&
					getCityId().equals(gymId.getCityId()) &&
					getCountryId().equals(gymId.getCountryId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), getCityId(), getCountryId());
	}
}
