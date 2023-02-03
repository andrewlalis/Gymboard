package nl.andrewlalis.gymboard_api.domains.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class GeoPoint implements Serializable {
	@Column(nullable = false, precision = 9, scale = 6)
	private BigDecimal latitude;
	@Column(nullable = false, precision = 9, scale = 6)
	private BigDecimal longitude;

	public GeoPoint() {}

	public GeoPoint(BigDecimal latitude, BigDecimal longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GeoPoint geoPoint = (GeoPoint) o;
		return geoPoint.latitude.equals(this.latitude) &&
				geoPoint.longitude.equals(this.longitude);
	}

	@Override
	public int hashCode() {
		return Objects.hash(latitude, longitude);
	}
}
