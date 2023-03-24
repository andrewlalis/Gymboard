package nl.andrewlalis.gymboard_api.domains.auth.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * A request that one user sends to ask to follow another user.
 */
@Table(
		name = "auth_user_follow_request",
		uniqueConstraints = @UniqueConstraint(columnNames = {"requesting_user_id", "user_to_follow_id"})
)
@Entity
public class UserFollowRequest {
	public static final Duration VALID_FOR = Duration.ofDays(7);

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private User requestingUser;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private User userToFollow;

	@Column
	private Boolean approved;

	@Column
	private LocalDateTime decidedAt;

	public UserFollowRequest() {}

	public UserFollowRequest(User requestingUser, User userToFollow) {
		this.requestingUser = requestingUser;
		this.userToFollow = userToFollow;
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public User getRequestingUser() {
		return requestingUser;
	}

	public User getUserToFollow() {
		return userToFollow;
	}

	public Boolean getApproved() {
		return approved;
	}

	public LocalDateTime getDecidedAt() {
		return decidedAt;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
		this.decidedAt = LocalDateTime.now();
	}
}
