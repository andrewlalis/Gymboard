package nl.andrewlalis.gymboard_api.dao.exercise;

import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseSubmissionRepository extends JpaRepository<ExerciseSubmission, Long>, JpaSpecificationExecutor<ExerciseSubmission> {
	List<ExerciseSubmission> findAllByStatus(ExerciseSubmission.Status status);
}
