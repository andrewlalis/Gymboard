package nl.andrewlalis.gymboard_api.domains.auth.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * A code that's sent to a user's email address to grant them access to change
 * their password without needing to log in.
 */
@Entity
@Table(name = "auth_user_password_reset_code")
public class PasswordResetCode {
	public static final Duration VALID_FOR = Duration.ofMinutes(30);

	@Id
	@Column(nullable = false, updatable = false, length = 127)
	private String code;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private User user;

	public PasswordResetCode() {}

	public PasswordResetCode(String code, User user) {
		this.code = code;
		this.user = user;
	}

	public String getCode() {
		return code;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public User getUser() {
		return user;
	}
}
