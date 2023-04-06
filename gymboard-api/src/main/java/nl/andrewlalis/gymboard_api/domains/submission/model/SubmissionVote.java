package nl.andrewlalis.gymboard_api.domains.submission.model;

import jakarta.persistence.*;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.domains.submission.model.Submission;

@Entity
@Table(
		name = "submission_vote",
		uniqueConstraints = @UniqueConstraint(columnNames = {"submission_id", "user_id"})
)
public class SubmissionVote {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Submission submission;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private User user;

	public SubmissionVote() {}

	public SubmissionVote(Submission submission, User user) {
		this.submission = submission;
		this.user = user;
	}

	public Long getId() {
		return id;
	}

	public Submission getSubmission() {
		return submission;
	}

	public User getUser() {
		return user;
	}
}
