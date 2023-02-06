package nl.andrewlalis.gymboard_api.domains.auth.dao;

import nl.andrewlalis.gymboard_api.domains.auth.model.UserPersonalDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPersonalDetailsRepository extends JpaRepository<UserPersonalDetails, String> {}
