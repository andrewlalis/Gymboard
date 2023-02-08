package nl.andrewlalis.gymboard_api.domains.api.dao;

import nl.andrewlalis.gymboard_api.domains.api.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, String> {
}
