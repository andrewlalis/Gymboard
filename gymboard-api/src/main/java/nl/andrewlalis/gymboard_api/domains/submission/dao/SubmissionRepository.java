package nl.andrewlalis.gymboard_api.domains.submission.dao;

import nl.andrewlalis.gymboard_api.domains.submission.model.Submission;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, String>, JpaSpecificationExecutor<Submission> {
	@Modifying
	void deleteAllByUser(User user);

	@Query("SELECT s FROM Submission s " +
			"WHERE s.videoProcessingTaskId = :taskId AND " +
			"s.processing = TRUE")
	List<Submission> findUnprocessedByTaskId(long taskId);

	List<Submission> findAllByProcessingTrue();
}