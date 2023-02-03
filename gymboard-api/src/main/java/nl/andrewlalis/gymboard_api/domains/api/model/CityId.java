package nl.andrewlalis.gymboard_api.domains.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CityId implements Serializable {
	@Column(nullable = false, length = 127)
	private String shortName;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Country country;

	public CityId() {}

	public CityId(String shortName, Country country) {
		this.shortName = shortName;
		this.country = country;
	}

	public String getShortName() {
		return shortName;
	}

	public Country getCountry() {
		return country;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CityId cityId = (CityId) o;
		return shortName.equals(cityId.shortName) && country.equals(cityId.country);
	}

	@Override
	public int hashCode() {
		return Objects.hash(shortName, country);
	}
}
