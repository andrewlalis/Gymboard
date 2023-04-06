package nl.andrewlalis.gymboard_api.domains.submission.model;

import jakarta.persistence.*;
import nl.andrewlalis.gymboard_api.domains.api.model.Exercise;
import nl.andrewlalis.gymboard_api.domains.api.model.Gym;
import nl.andrewlalis.gymboard_api.domains.api.model.WeightUnit;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A submission draft is a temporary entity that exists while a user is
 * preparing their submission. It includes all the data needed to make a
 * submission, so when the user has finished editing, they can "submit" their
 * draft and video processing will then begin, and once done, their submission
 * will be published.
 * <p>
 *     <strong>This is not yet implemented!</strong>
 * </p>
 */
@Entity
@Table(name = "submission_draft")
public class SubmissionDraft {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private User user;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Gym gym;

	// All of the following properties are editable while this draft has not yet
	// been submitted. They will be validated upon submission.

	@ManyToOne(fetch = FetchType.LAZY)
	private Exercise exercise;

	@Column
	private LocalDateTime performedAt;

	@Column(precision = 7, scale = 2)
	private BigDecimal rawWeight;

	@Column @Enumerated(EnumType.STRING)
	private WeightUnit weightUnit;

	@Column
	private int reps;

	@Column
	private long videoProcessingTaskId;

	public SubmissionDraft() {}

	public SubmissionDraft(User user, Gym gym) {
		this.user = user;
		this.gym = gym;
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public User getUser() {
		return user;
	}

	public Gym getGym() {
		return gym;
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

	public int getReps() {
		return reps;
	}

	public long getVideoProcessingTaskId() {
		return videoProcessingTaskId;
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;
	}

	public void setPerformedAt(LocalDateTime performedAt) {
		this.performedAt = performedAt;
	}

	public void setRawWeight(BigDecimal rawWeight) {
		this.rawWeight = rawWeight;
	}

	public void setWeightUnit(WeightUnit weightUnit) {
		this.weightUnit = weightUnit;
	}

	public void setReps(int reps) {
		this.reps = reps;
	}

	public void setVideoProcessingTaskId(long videoProcessingTaskId) {
		this.videoProcessingTaskId = videoProcessingTaskId;
	}
}
