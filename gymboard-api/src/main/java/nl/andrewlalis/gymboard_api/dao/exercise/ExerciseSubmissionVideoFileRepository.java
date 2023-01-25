package nl.andrewlalis.gymboard_api.dao.exercise;

import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmission;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmissionVideoFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExerciseSubmissionVideoFileRepository extends JpaRepository<ExerciseSubmissionVideoFile, Long> {
	Optional<ExerciseSubmissionVideoFile> findBySubmission(ExerciseSubmission submission);
}
