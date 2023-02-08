package nl.andrewlalis.gymboard_api.domains.api.model.submission;

import jakarta.persistence.*;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "submission_report")
public class SubmissionReport {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Submission submission;

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@Column(nullable = false)
	private String reason;

	@Column(length = 1024)
	private String description;

	public SubmissionReport() {}

	public SubmissionReport(Submission submission, User user, String reason, String description) {
		this.submission = submission;
		this.user = user;
		this.reason = reason;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public Submission getSubmission() {
		return submission;
	}

	public User getUser() {
		return user;
	}

	public String getReason() {
		return reason;
	}

	public String getDescription() {
		return description;
	}
}
