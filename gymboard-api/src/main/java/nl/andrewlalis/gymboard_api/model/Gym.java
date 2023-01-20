package nl.andrewlalis.gymboard_api.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "gym")
public class Gym {
	@EmbeddedId
	private GymId id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	public GymId getId() {
		return id;
	}

	public String getName() {
		return id.getName();
	}

	public String getCityId() {
		return id.getCityId();
	}

	public String getCountryId() {
		return id.getCountryId();
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
