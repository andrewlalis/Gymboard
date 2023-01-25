package nl.andrewlalis.gymboard_api.model.exercise;

import jakarta.persistence.*;
import nl.andrewlalis.gymboard_api.model.Gym;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_submission")
public class ExerciseSubmission {
	public enum Status {
		WAITING,
		PROCESSING,
		FAILED,
		COMPLETED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Gym gym;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Exercise exercise;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;

	@Column(nullable = false, updatable = false, length = 63)
	private String submitterName;

	@Column(nullable = false, precision = 7, scale = 2)
	private BigDecimal weight;

	@Column(nullable = false)
	private int reps;

	public ExerciseSubmission() {}

	public ExerciseSubmission(Gym gym, Exercise exercise, String submitterName, BigDecimal weight, int reps) {
		this.gym = gym;
		this.exercise = exercise;
		this.submitterName = submitterName;
		this.weight = weight;
		this.reps = reps;
		this.status = Status.WAITING;
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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getSubmitterName() {
		return submitterName;
	}

	public BigDecimal getWeight() {
		return weight;
	}

	public int getReps() {
		return reps;
	}
}
