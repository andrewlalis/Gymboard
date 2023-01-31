package nl.andrewlalis.gymboard_api.model.auth;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_user_activation_code")
public class UserActivationCode {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private User user;

	@Column(nullable = false, unique = true, updatable = false, length = 127)
	private String code;

	public UserActivationCode() {}

	public UserActivationCode(User user, String code) {
		this.user = user;
		this.code = code;
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

	public String getCode() {
		return code;
	}
}
