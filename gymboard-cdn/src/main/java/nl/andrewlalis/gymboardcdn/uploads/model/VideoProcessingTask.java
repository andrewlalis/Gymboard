package nl.andrewlalis.gymboardcdn.uploads.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * An entity to keep track of a task for processing a raw video into a better
 * format for Gymboard to serve. Generally, tasks are processed like so:
 * <ol>
 *     <li>A video is uploaded, and a new task is created with the NOT_STARTED status.</li>
 *     <li>Once the Gymboard API verifies the associated submission, it'll
 *     request to start the task, bringing it to the WAITING status.</li>
 *     <li>When a task executor picks up the waiting task, its status changes to IN_PROGRESS.</li>
 *     <li>If the video is processed successfully, then the task is COMPLETED, otherwise FAILED.</li>
 * </ol>
 */
@Entity
@Table(name = "task_video_processing")
public class VideoProcessingTask {
	public enum Status {
		NOT_STARTED,
		WAITING,
		IN_PROGRESS,
		COMPLETED,
		FAILED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;

	/**
	 * The file id for the original, raw user-uploaded video file that needs to
	 * be processed.
	 */
	@Column(nullable = false, updatable = false, length = 26)
	private String uploadFileId;

	@Column(length = 26)
	private String videoFileId;

	@Column(length = 26)
	private String thumbnailFileId;

	public VideoProcessingTask() {}

	public VideoProcessingTask(Status status, String uploadFileId) {
		this.status = status;
		this.uploadFileId = uploadFileId;
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getUploadFileId() {
		return uploadFileId;
	}

	public String getVideoFileId() {
		return videoFileId;
	}

	public void setVideoFileId(String videoFileId) {
		this.videoFileId = videoFileId;
	}

	public String getThumbnailFileId() {
		return thumbnailFileId;
	}

	public void setThumbnailFileId(String thumbnailFileId) {
		this.thumbnailFileId = thumbnailFileId;
	}
}
