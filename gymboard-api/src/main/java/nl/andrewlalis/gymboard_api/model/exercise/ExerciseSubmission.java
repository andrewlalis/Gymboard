package nl.andrewlalis.gymboard_api.model.exercise;

import jakarta.persistence.*;
import nl.andrewlalis.gymboard_api.model.Gym;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_submission")
public class ExerciseSubmission {
	/**
	 * The status of a submission.
	 * <ul>
	 *     <li>Each submission starts as WAITING.</li>
	 *     <li>The status changes to PROCESSING once it's picked up for processing.</li>
	 *     <li>If processing fails, the status changes to FAILED.</li>
	 *     <li>If processing is successful, the status changes to COMPLETED.</li>
	 *     <li>Once a completed submission is verified either automatically or manually, it's set to VERIFIED.</li>
	 * </ul>
	 */
	public enum Status {
		WAITING,
		PROCESSING,
		FAILED,
		COMPLETED,
		VERIFIED
	}

	public enum WeightUnit {
		KG,
		LBS
	}

	@Id
	@Column(nullable = false, updatable = false, length = 26)
	private String id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Gym gym;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Exercise exercise;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;

	@Column(nullable = false, updatable = false, length = 63)
	private String submitterName;

	@Column(nullable = false, precision = 7, scale = 2)
	private BigDecimal rawWeight;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private WeightUnit weightUnit;

	@Column(nullable = false, precision = 7, scale = 2)
	private BigDecimal metricWeight;

	@Column(nullable = false)
	private int reps;

	/**
	 * Marker that's used to simplify queries where we just want submissions
	 * that are in a status that's not WAITING, PROCESSING, or FAILED, i.e.
	 * a successful submission that's been processed.
	 */
	@Column(nullable = false)
	private boolean complete;

	public ExerciseSubmission() {}

	public ExerciseSubmission(String id, Gym gym, Exercise exercise, String submitterName, BigDecimal rawWeight, WeightUnit unit, BigDecimal metricWeight, int reps) {
		this.id = id;
		this.gym = gym;
		this.exercise = exercise;
		this.submitterName = submitterName;
		this.rawWeight = rawWeight;
		this.weightUnit = unit;
		this.metricWeight = metricWeight;
		this.reps = reps;
		this.status = Status.WAITING;
		this.complete = false;
	}

	public String getId() {
		return id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public Gym getGym() {
		return gym;
	}

	public Exercise getExercise() {
		return exercise;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getSubmitterName() {
		return submitterName;
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

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}
}
