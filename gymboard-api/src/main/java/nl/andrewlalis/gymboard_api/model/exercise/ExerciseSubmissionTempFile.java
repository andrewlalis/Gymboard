package nl.andrewlalis.gymboard_api.model.exercise;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Tracks the temporary file on disk that's stored while a user is preparing
 * their submission. This file will be removed after the submission is
 * processed.
 */
@Entity
@Table(name = "exercise_submission_temp_file")
public class ExerciseSubmissionTempFile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@Column(nullable = false, updatable = false, length = 1024)
	private String path;

	/**
	 * The submission that this temporary file is for. This will initially be
	 * null, but will be set as soon as the submission is finalized.
	 */
	@OneToOne(fetch = FetchType.LAZY)
	private ExerciseSubmission submission;

	public ExerciseSubmissionTempFile() {}

	public ExerciseSubmissionTempFile(String path) {
		this.path = path;
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public String getPath() {
		return path;
	}

	public ExerciseSubmission getSubmission() {
		return submission;
	}

	public void setSubmission(ExerciseSubmission submission) {
		this.submission = submission;
	}
}
