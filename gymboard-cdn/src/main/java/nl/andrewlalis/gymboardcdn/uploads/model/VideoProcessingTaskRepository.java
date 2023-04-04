package nl.andrewlalis.gymboardcdn.uploads.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VideoProcessingTaskRepository extends JpaRepository<VideoProcessingTask, Long> {
	List<VideoProcessingTask> findAllByStatusOrderByCreatedAtDesc(VideoProcessingTask.Status status);

	List<VideoProcessingTask> findAllByCreatedAtBefore(LocalDateTime cutoff);
}
