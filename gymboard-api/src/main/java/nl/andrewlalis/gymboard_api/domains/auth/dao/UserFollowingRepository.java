package nl.andrewlalis.gymboard_api.domains.auth.dao;

import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.domains.auth.model.UserFollowing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFollowingRepository extends JpaRepository<UserFollowing, Long> {
	boolean existsByFollowedUserAndFollowingUser(User followedUser, User followingUser);

	@Modifying
	void deleteByFollowedUserAndFollowingUser(User followedUser, User followingUser);

	Page<UserFollowing> findAllByFollowedUserOrderByCreatedAtDesc(User followedUser, Pageable pageable);
	Page<UserFollowing> findAllByFollowingUserOrderByCreatedAtDesc(User followingUser, Pageable pageable);

	long countByFollowedUser(User followedUser);
	long countByFollowingUser(User followingUser);
	long countByFollowedUserId(String id);
	long countByFollowingUserId(String id);
}
