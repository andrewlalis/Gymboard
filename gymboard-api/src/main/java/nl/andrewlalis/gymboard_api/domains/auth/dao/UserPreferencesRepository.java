package nl.andrewlalis.gymboard_api.domains.auth.dao;

import nl.andrewlalis.gymboard_api.domains.auth.model.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, String> {
}
