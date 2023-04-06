package nl.andrewlalis.gymboard_api.domains.submission.model;

import jakarta.persistence.*;
import nl.andrewlalis.gymboard_api.domains.api.model.Exercise;
import nl.andrewlalis.gymboard_api.domains.api.model.Gym;
import nl.andrewlalis.gymboard_api.domains.api.model.WeightUnit;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "submission")
public class Submission {
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
	 * The id of the video processing task that a user gives to us when they
	 * create the submission, so that when the task finishes processing, we can
	 * route its data to the right submission.
	 */
	@Column(nullable = false, updatable = false)
	private long videoProcessingTaskId;

	/**
	 * The id of the video file that was submitted for this submission. It lives
	 * on the <em>gymboard-cdn</em> service as a stored file, which can be
	 * accessed via <code>GET https://CDN-HOST/files/{videoFileId}</code>.
	 */
	@Column(length = 26)
	private String videoFileId = null;

	/**
	 * The id of the thumbnail file that was generated for this submission.
	 * Similarly to the video file id, it refers to a file managed by the CDN.
	 */
	@Column(length = 26)
	private String thumbnailFileId = null;

	@Column(nullable = false, precision = 7, scale = 2)
	private BigDecimal rawWeight;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private WeightUnit weightUnit;

	@Column(nullable = false, precision = 7, scale = 2)
	private BigDecimal metricWeight;

	@Column(nullable = false)
	private int reps;

	@Column(nullable = false)
	private boolean verified;

	public Submission() {}

	public Submission(
		String id,
		Gym gym,
		Exercise exercise,
		User user,
		LocalDateTime performedAt,
		long videoProcessingTaskId,
		BigDecimal rawWeight,
		WeightUnit unit,
		BigDecimal metricWeight,
		int reps
	) {
		this.id = id;
		this.gym = gym;
		this.exercise = exercise;
		this.user = user;
		this.performedAt = performedAt;
		this.videoProcessingTaskId = videoProcessingTaskId;
		this.rawWeight = rawWeight;
		this.weightUnit = unit;
		this.metricWeight = metricWeight;
		this.reps = reps;
		this.verified = false;
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

	public long getVideoProcessingTaskId() {
		return videoProcessingTaskId;
	}

	public String getVideoFileId() {
		return videoFileId;
	}

	public String getThumbnailFileId() {
		return thumbnailFileId;
	}

	public void setVideoFileId(String videoFileId) {
		this.videoFileId = videoFileId;
	}

	public void setThumbnailFileId(String thumbnailFileId) {
		this.thumbnailFileId = thumbnailFileId;
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

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}
}
