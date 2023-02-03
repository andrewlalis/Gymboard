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

	/**
	 * The original filename.
	 */
	@Column(nullable = false)
	private String filename;

	/**
	 * The path to the temporary file that we'll use as input.
	 */
	@Column(nullable = false)
	private String tempFilePath;

	/**
	 * The identifier that will be used to identify the final video, if it
	 * is processed successfully.
	 */
	@Column(nullable = false, updatable = false, length = 26)
	private String videoIdentifier;

	public VideoProcessingTask() {}

	public VideoProcessingTask(Status status, String filename, String tempFilePath, String videoIdentifier) {
		this.status = status;
		this.filename = filename;
		this.tempFilePath = tempFilePath;
		this.videoIdentifier = videoIdentifier;
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public String getFilename() {
		return filename;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getTempFilePath() {
		return tempFilePath;
	}

	public String getVideoIdentifier() {
		return videoIdentifier;
	}
}
