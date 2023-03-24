package nl.andrewlalis.gymboard_api.domains.auth.dao;

import nl.andrewlalis.gymboard_api.domains.auth.model.EmailResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface EmailResetCodeRepository extends JpaRepository<EmailResetCode, String> {
	@Modifying
	void deleteAllByCreatedAtBefore(LocalDateTime cutoff);
}
