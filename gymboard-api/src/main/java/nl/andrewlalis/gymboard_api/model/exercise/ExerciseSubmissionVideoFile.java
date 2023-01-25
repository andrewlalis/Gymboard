package nl.andrewlalis.gymboard_api.model.exercise;

import jakarta.persistence.*;
import nl.andrewlalis.gymboard_api.model.StoredFile;

/**
 * An entity which links an {@link ExerciseSubmission} to a {@link nl.andrewlalis.gymboard_api.model.StoredFile}
 * containing the video that was submitted along with the submission.
 */
@Entity
@Table(name = "exercise_submission_video_file")
public class ExerciseSubmissionVideoFile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(optional = false, fetch = FetchType.LAZY)
	private ExerciseSubmission submission;

	@OneToOne(optional = false, fetch = FetchType.LAZY, orphanRemoval = true)
	private StoredFile file;

	public ExerciseSubmissionVideoFile() {}

	public ExerciseSubmissionVideoFile(ExerciseSubmission submission, StoredFile file) {
		this.submission = submission;
		this.file = file;
	}

	public Long getId() {
		return id;
	}

	public ExerciseSubmission getSubmission() {
		return submission;
	}

	public StoredFile getFile() {
		return file;
	}
}
