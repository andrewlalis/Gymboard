package nl.andrewlalis.gymboard_api.dao.exercise;

import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmission;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmissionVideoFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExerciseSubmissionVideoFileRepository extends JpaRepository<ExerciseSubmissionVideoFile, Long> {
	Optional<ExerciseSubmissionVideoFile> findBySubmission(ExerciseSubmission submission);

	@Query("SELECT f FROM ExerciseSubmissionVideoFile f WHERE " +
			"f.submission.id = :submissionId AND f.submission.complete = true")
	Optional<ExerciseSubmissionVideoFile> findByCompletedSubmissionId(String submissionId);
}
