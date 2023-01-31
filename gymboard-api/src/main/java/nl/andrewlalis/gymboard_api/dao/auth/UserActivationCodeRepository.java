package nl.andrewlalis.gymboard_api.dao.auth;

import nl.andrewlalis.gymboard_api.model.auth.UserActivationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserActivationCodeRepository extends JpaRepository<UserActivationCode, Long> {
	Optional<UserActivationCode> findByCode(String code);
}
