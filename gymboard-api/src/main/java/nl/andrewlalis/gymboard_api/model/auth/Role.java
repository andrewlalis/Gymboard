package nl.andrewlalis.gymboard_api.model.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auth_role")
public class Role {
	@Id
	@Column(nullable = false)
	private String shortName;

	public Role() {}

	public Role(String shortName) {
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}
}
