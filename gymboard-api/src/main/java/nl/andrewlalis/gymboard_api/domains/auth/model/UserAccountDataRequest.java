package nl.andrewlalis.gymboard_api.domains.auth.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A request issued by a user for a download of their entire account data set.
 * This entity is created when a user sends a request, and will get picked up
 * and processed eventually by a scheduled task, and ultimately the user will be
 * sent an email with a link to download their data.
 */
@Entity
@Table(name = "auth_user_account_data_request")
public class UserAccountDataRequest {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private User user;

	@Column(nullable = false)
	private boolean fulfilled = false;

	public UserAccountDataRequest() {}

	public UserAccountDataRequest(User user) {
		this.user = user;
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public User getUser() {
		return user;
	}

	public boolean isFulfilled() {
		return fulfilled;
	}
}
