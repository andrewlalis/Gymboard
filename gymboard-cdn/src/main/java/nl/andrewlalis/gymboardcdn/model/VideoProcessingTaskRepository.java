package nl.andrewlalis.gymboardcdn.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoProcessingTaskRepository extends JpaRepository<VideoProcessingTask, Long> {
	Optional<VideoProcessingTask> findByVideoIdentifier(String identifier);

	List<VideoProcessingTask> findAllByStatusOrderByCreatedAtDesc(VideoProcessingTask.Status status);

	List<VideoProcessingTask> findAllByCreatedAtBefore(LocalDateTime cutoff);
}
