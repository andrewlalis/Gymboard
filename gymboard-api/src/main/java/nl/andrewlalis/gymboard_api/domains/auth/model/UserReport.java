package nl.andrewlalis.gymboard_api.domains.auth.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A user report is submitted by one user to report inappropriate actions of
 * another user.
 */
@Entity
@Table(name = "auth_user_report")
public class UserReport {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	private User reportedBy;

	@Column(nullable = false)
	private String reason;

	@Column(length = 1024)
	private String description;

	public UserReport() {}

	public UserReport(User user, User reportedBy, String reason, String description) {
		this.user = user;
		this.reportedBy = reportedBy;
		this.reason = reason;
		this.description = description;
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

	public User getReportedBy() {
		return reportedBy;
	}

	public String getReason() {
		return reason;
	}

	public String getDescription() {
		return description;
	}
}
