package nl.andrewlalis.gymboard_api.domains.api.dao.exercise;

import nl.andrewlalis.gymboard_api.domains.api.model.exercise.ExerciseSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseSubmissionRepository extends JpaRepository<ExerciseSubmission, String>, JpaSpecificationExecutor<ExerciseSubmission> {
}