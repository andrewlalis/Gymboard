package nl.andrewlalis.gymboard_api.domains.auth.dao;

import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
	boolean existsByEmail(String email);
	Optional<User> findByEmail(String email);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
	Optional<User> findByIdWithRoles(String id);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
	Optional<User> findByEmailWithRoles(String email);

	@Modifying
	void deleteAllByActivatedFalseAndCreatedAtBefore(LocalDateTime cutoff);
}
