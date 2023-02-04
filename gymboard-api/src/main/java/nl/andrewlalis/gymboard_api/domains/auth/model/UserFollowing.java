package nl.andrewlalis.gymboard_api.domains.auth.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Stores information about a "following" relationship between users, which can
 * have impacts on the notifications that a user can receive.
 */
@Entity
@Table(name = "auth_user_following")
public class UserFollowing {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private User followedUser;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private User followingUser;

	public UserFollowing() {}

	public UserFollowing(User followedUser, User followingUser) {
		this.followedUser = followedUser;
		this.followingUser = followingUser;
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public User getFollowedUser() {
		return followedUser;
	}

	public User getFollowingUser() {
		return followingUser;
	}
}
