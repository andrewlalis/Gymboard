package nl.andrewlalis.gymboardcdn.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoProcessingTaskRepository extends JpaRepository<VideoProcessingTask, Long> {
	Optional<VideoProcessingTask> findByVideoIdentifier(String identifier);

	boolean existsByVideoIdentifier(String identifier);

	List<VideoProcessingTask> findAllByStatusOrderByCreatedAtDesc(VideoProcessingTask.Status status);
}
