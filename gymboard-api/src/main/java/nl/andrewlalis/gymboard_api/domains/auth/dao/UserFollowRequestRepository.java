package nl.andrewlalis.gymboard_api.domains.auth.dao;

import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.domains.auth.model.UserFollowRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserFollowRequestRepository extends JpaRepository<UserFollowRequest, Long> {
	@Modifying
	void deleteAllByCreatedAtBefore(LocalDateTime cutoff);

	boolean existsByRequestingUserAndUserToFollowAndApprovedIsNull(User requestingUser, User userToFollow);
}
