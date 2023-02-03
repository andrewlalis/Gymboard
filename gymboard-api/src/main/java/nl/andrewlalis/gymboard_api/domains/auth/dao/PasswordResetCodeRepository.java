package nl.andrewlalis.gymboard_api.domains.auth.dao;

import nl.andrewlalis.gymboard_api.domains.auth.model.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, String> {
	@Modifying
	void deleteAllByCreatedAtBefore(LocalDateTime cutoff);
}
