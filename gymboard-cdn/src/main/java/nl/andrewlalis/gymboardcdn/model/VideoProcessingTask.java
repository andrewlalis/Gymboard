package nl.andrewlalis.gymboardcdn.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * An entity to keep track of a task for processing a raw video into a better
 * format for Gymboard to serve.
 */
@Entity
@Table(name = "task_video_processing")
public class VideoProcessingTask {
	public enum Status {
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

	@Column(nullable = false, updatable = false, length = 26)
	private String rawUploadFileId;

	public VideoProcessingTask() {}

	public VideoProcessingTask(Status status, String rawUploadFileId) {
		this.status = status;
		this.rawUploadFileId = rawUploadFileId;
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

	public String getRawUploadFileId() {
		return rawUploadFileId;
	}
}
