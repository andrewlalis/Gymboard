package nl.andrewlalis.gymboard_api.domains.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "country")
public class Country {
	@Id
	@Column(nullable = false, length = 2, unique = true)
	private String code;

	@Column(nullable = false, unique = true)
	private String name;

	public Country() {}

	public Country(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
}
