package nl.andrewlalis.gymboard_api.model.exercise;

import jakarta.persistence.*;
import nl.andrewlalis.gymboard_api.model.Gym;
import nl.andrewlalis.gymboard_api.model.StoredFile;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_submission")
public class ExerciseSubmission {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Gym gym;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Exercise exercise;

	@Column(nullable = false, updatable = false, length = 63)
	private String submitterName;

	@Column(nullable = false)
	private boolean verified;

	@Column(nullable = false, precision = 7, scale = 2)
	private BigDecimal weight;

	@OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private StoredFile videoFile;

	public ExerciseSubmission() {}

	public ExerciseSubmission(Gym gym, Exercise exercise, String submitterName, BigDecimal weight, StoredFile videoFile) {
		this.gym = gym;
		this.exercise = exercise;
		this.submitterName = submitterName;
		this.weight = weight;
		this.videoFile = videoFile;
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public Gym getGym() {
		return gym;
	}

	public Exercise getExercise() {
		return exercise;
	}

	public String getSubmitterName() {
		return submitterName;
	}

	public boolean isVerified() {
		return verified;
	}

	public BigDecimal getWeight() {
		return weight;
	}

	public StoredFile getVideoFile() {
		return videoFile;
	}
}
