package nl.andrewlalis.gymboard_api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "gym_exercise_submission")
public class ExerciseSubmission {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@Column(nullable = false, updatable = false, length = 63)
	private String submitterName;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Gym gym;

	
}
