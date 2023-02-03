package nl.andrewlalis.gymboard_api.domains.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "city")
public class City {
	@EmbeddedId
	private CityId id;

	@Column(nullable = false)
	private String name;

	public City() {}

	public City(String shortName, String name, Country country) {
		this.id = new CityId(shortName, country);
		this.name = name;
	}

	public String getShortName() {
		return id.getShortName();
	}

	public String getName() {
		return name;
	}

	public Country getCountry() {
		return id.getCountry();
	}
}
