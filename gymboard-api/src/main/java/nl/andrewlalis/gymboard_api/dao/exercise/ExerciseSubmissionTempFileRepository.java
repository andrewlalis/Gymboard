package nl.andrewlalis.gymboard_api.dao.exercise;

import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmission;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmissionTempFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseSubmissionTempFileRepository extends JpaRepository<ExerciseSubmissionTempFile, Long> {
	List<ExerciseSubmissionTempFile> findAllByCreatedAtBefore(LocalDateTime timestamp);
	Optional<ExerciseSubmissionTempFile> findBySubmission(ExerciseSubmission submission);
}
