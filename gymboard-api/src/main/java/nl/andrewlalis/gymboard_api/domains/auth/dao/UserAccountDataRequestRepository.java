package nl.andrewlalis.gymboard_api.domains.auth.dao;

import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.domains.auth.model.UserAccountDataRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountDataRequestRepository extends JpaRepository<UserAccountDataRequest, Long> {
	boolean existsByUserIdAndFulfilledFalse(String userId);

	@Modifying
	void deleteAllByUser(User user);
}
