package nl.andrewlalis.gymboard_api.dao.exercise;

import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseSubmissionRepository extends JpaRepository<ExerciseSubmission, Long> {
}
