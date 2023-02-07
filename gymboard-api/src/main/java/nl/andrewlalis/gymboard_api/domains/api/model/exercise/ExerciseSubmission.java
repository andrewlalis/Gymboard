package nl.andrewlalis.gymboard_api.domains.api.model.exercise;

import jakarta.persistence.*;
import nl.andrewlalis.gymboard_api.domains.api.model.Gym;
import nl.andrewlalis.gymboard_api.domains.api.model.WeightUnit;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_submission")
public class ExerciseSubmission {
	@Id
	@Column(nullable = false, updatable = false, length = 26)
	private String id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Gym gym;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Exercise exercise;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private User user;

	@Column(nullable = false)
	private LocalDateTime performedAt;

	/**
	 * The id of the video file that was submitted for this submission. It lives
	 * on the <em>gymboard-cdn</em> service as a stored file, which can be
	 * accessed via <code>GET https://CDN-HOST/files/{videoFileId}</code>.
	 */
	@Column(nullable = false, updatable = false, length = 26)
	private String videoFileId;

	@Column(nullable = false, precision = 7, scale = 2)
	private BigDecimal rawWeight;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private WeightUnit weightUnit;

	@Column(nullable = false, precision = 7, scale = 2)
	private BigDecimal metricWeight;

	@Column(nullable = false)
	private int reps;

	public ExerciseSubmission() {}

	public ExerciseSubmission(String id, Gym gym, Exercise exercise, User user, LocalDateTime performedAt, String videoFileId, BigDecimal rawWeight, WeightUnit unit, BigDecimal metricWeight, int reps) {
		this.id = id;
		this.gym = gym;
		this.exercise = exercise;
		this.videoFileId = videoFileId;
		this.user = user;
		this.performedAt = performedAt;
		this.rawWeight = rawWeight;
		this.weightUnit = unit;
		this.metricWeight = metricWeight;
		this.reps = reps;
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

	public String getVideoFileId() {
		return videoFileId;
	}

	public User getUser() {
		return user;
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
