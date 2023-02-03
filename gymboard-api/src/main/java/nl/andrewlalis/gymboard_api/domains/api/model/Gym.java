package nl.andrewlalis.gymboard_api.domains.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a single gym location, at which local leaderboards are held.
 */
@Entity
@Table(name = "gym")
public class Gym {
	@EmbeddedId
	private GymId id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@Column(nullable = false, length = 127)
	private String displayName;

	@Column(length = 1024)
	private String websiteUrl;

	@Embedded
	private GeoPoint location;

	@Column(nullable = false, length = 1024)
	private String streetAddress;

	public Gym() {}

	public Gym(City city, String shortName, String displayName, String websiteUrl, GeoPoint location, String streetAddress) {
		this.id = new GymId(shortName, city);
		this.displayName = displayName;
		this.websiteUrl = websiteUrl;
		this.location = location;
		this.streetAddress = streetAddress;
	}

	public GymId getId() {
		return id;
	}

	public City getCity() {
		return id.getCity();
	}

	public String getShortName() {
		return id.getShortName();
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public GeoPoint getLocation() {
		return location;
	}

	public String getStreetAddress() {
		return streetAddress;
	}
}
