package nl.andrewlalis.gymboard_api.domains.api.dao.exercise;

import nl.andrewlalis.gymboard_api.domains.api.model.exercise.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, String> {
}
