package nl.andrewlalis.gymboard_api.domains.auth.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * A code that's sent to a user's new email address to confirm that they own
 * it. Once confirmed, the user's email address will be updated.
 */
@Table(name = "auth_user_email_reset_code")
@Entity
public class EmailResetCode {
	public static final Duration VALID_FOR = Duration.ofMinutes(30);

	@Id
	@Column(nullable = false, updatable = false, length = 127)
	private String code;

	@Column(nullable = false, updatable = false, unique = true)
	private String newEmail;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private User user;

	public EmailResetCode() {}

	public EmailResetCode(String code, String newEmail, User user) {
		this.code = code;
		this.newEmail = newEmail;
		this.user = user;
	}

	public String getCode() {
		return code;
	}

	public String getNewEmail() {
		return newEmail;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public User getUser() {
		return user;
	}
}
