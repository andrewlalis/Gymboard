package nl.andrewlalis.gymboard_api.domains.auth.model;

import jakarta.persistence.*;
import nl.andrewlalis.gymboard_api.domains.api.model.WeightUnit;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Personal details that belong to a user.
 */
@Entity
@Table(name = "auth_user_personal_details")
public class UserPersonalDetails {
	public enum PersonSex {
		MALE,
		FEMALE,
		UNKNOWN;

		public static PersonSex parse(String s) {
			if (s != null && !s.isBlank()) {
				s = s.strip().toUpperCase();
				if (s.equals("M") || s.equals("MALE") || s.equals("MAN")) {
					return MALE;
				} else if (s.equals("F") || s.equals("FEMALE") || s.equals("WOMAN")) {
					return FEMALE;
				}
			}
			return UNKNOWN;
		}
	}

	@Id
	@Column(name = "user_id", length = 26)
	private String userId;

	@OneToOne(optional = false, fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	@Column
	private LocalDate birthDate;

	@Column(precision = 7, scale = 2)
	private BigDecimal currentWeight;

	@Column
	@Enumerated(EnumType.STRING)
	private WeightUnit currentWeightUnit;

	@Column(precision = 7, scale = 2)
	private BigDecimal currentMetricWeight;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private PersonSex sex = PersonSex.UNKNOWN;

	public UserPersonalDetails() {}

	public UserPersonalDetails(User user) {
		this.user = user;
		this.userId = user.getId();
	}

	public String getUserId() {
		return userId;
	}

	public User getUser() {
		return user;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public BigDecimal getCurrentWeight() {
		return currentWeight;
	}

	public WeightUnit getCurrentWeightUnit() {
		return currentWeightUnit;
	}

	public BigDecimal getCurrentMetricWeight() {
		return currentMetricWeight;
	}

	public PersonSex getSex() {
		return sex;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public void setCurrentWeight(BigDecimal currentWeight) {
		this.currentWeight = currentWeight;
	}

	public void setCurrentWeightUnit(WeightUnit currentWeightUnit) {
		this.currentWeightUnit = currentWeightUnit;
	}

	public void setCurrentMetricWeight(BigDecimal currentMetricWeight) {
		this.currentMetricWeight = currentMetricWeight;
	}

	public void setSex(PersonSex sex) {
		this.sex = sex;
	}
}
