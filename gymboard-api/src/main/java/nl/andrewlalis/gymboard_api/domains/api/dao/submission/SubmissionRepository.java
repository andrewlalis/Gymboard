package nl.andrewlalis.gymboard_api.domains.api.dao.submission;

import nl.andrewlalis.gymboard_api.domains.api.model.submission.Submission;
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
			"(s.videoFileId IS NULL OR s.thumbnailFileId IS NULL)")
	List<Submission> findUnprocessedByTaskId(long taskId);
}
