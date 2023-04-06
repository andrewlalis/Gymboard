package nl.andrewlalis.gymboard_api.domains.submission.model;

import jakarta.persistence.*;
import nl.andrewlalis.gymboard_api.domains.api.model.Exercise;
import nl.andrewlalis.gymboard_api.domains.api.model.WeightUnit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Basic user-specified properties about a Submission.
 */
@Embeddable
public class SubmissionProperties {
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Exercise exercise;

	@Column(nullable = false)
	private LocalDateTime performedAt;

	@Column(nullable = false, precision = 7, scale = 2)
	private BigDecimal rawWeight;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private WeightUnit weightUnit;

	@Column(nullable = false, precision = 7, scale = 2)
	private BigDecimal metricWeight;

	@Column(nullable = false)
	private int reps;

	public SubmissionProperties() {}

	public SubmissionProperties(
			Exercise exercise,
			LocalDateTime performedAt,
			BigDecimal rawWeight,
			WeightUnit weightUnit,
			int reps
	) {
		this.exercise = exercise;
		this.performedAt = performedAt;
		this.rawWeight = rawWeight;
		this.weightUnit = weightUnit;
		this.metricWeight = WeightUnit.toKilograms(rawWeight, weightUnit);
		this.reps = reps;
	}

	public Exercise getExercise() {
		return exercise;
	}

	public LocalDateTime getPerformedAt() {
		return performedAt;
	}

	public BigDecimal getRawWeight() {
		return rawWeight;
	}

	public WeightUnit getWeightUnit() {
		return weightUnit;
	}

	public BigDecimal getMetricWeight() {
		return metricWeight;
	}

	public int getReps() {
		return reps;
	}
}
