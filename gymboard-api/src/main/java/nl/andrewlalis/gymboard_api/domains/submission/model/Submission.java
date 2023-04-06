package nl.andrewlalis.gymboard_api.domains.submission.model;

import jakarta.persistence.*;
import nl.andrewlalis.gymboard_api.domains.api.model.Gym;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * The Submission entity represents a user's posted video of a lift they did at
 * a gym.
 * <p>
 *     A submission is created in the front-end using the following flow:
 * </p>
 * <ol>
 *     <li>User uploads a raw video of their lift.</li>
 *     <li>User enters some basic information about the lift.</li>
 *     <li>User submits the lift.</li>
 *     <li>API validates the information.</li>
 *     <li>API creates a new Submission, and tells the CDN service to process
 *     the uploaded video.</li>
 *     <li>Once processing completes successfully, the CDN sends the final video
 *     and thumbnail file ids to the API and the Submission's "processing" flag
 *     is removed.</li>
 *     <li>If for whatever reason the CDN's video processing fails or never
 *     completes, the Submission is deleted and the user is notified of the
 *     issue.</li>
 * </ol>
 */
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
	private User user;

	/**
	 * The id of the video processing task that a user gives to us when they
	 * create the submission, so that when the task finishes processing, we can
	 * route its data to the right submission.
	 */
	@Column
	private Long videoProcessingTaskId;

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

	/**
	 * The user-specified properties of the submission.
	 */
	@Embedded
	private SubmissionProperties properties;

	/**
	 * A flag that indicates whether this submission is currently processing.
	 * A submission is processing until its associated processing task completes
	 * either successfully or unsuccessfully.
	 */
	@Column(nullable = false)
	private boolean processing;

	@Column(nullable = false)
	private boolean verified;

	public Submission() {}

	public Submission(
		String id,
		Gym gym,
		User user,
		long videoProcessingTaskId,
		SubmissionProperties properties
	) {
		this.id = id;
		this.gym = gym;
		this.user = user;
		this.videoProcessingTaskId = videoProcessingTaskId;
		this.properties = properties;
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

	public Long getVideoProcessingTaskId() {
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

	public SubmissionProperties getProperties() {
		return properties;
	}

	public boolean isProcessing() {
		return processing;
	}

	public void setProcessing(boolean processing) {
		this.processing = processing;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}
}
