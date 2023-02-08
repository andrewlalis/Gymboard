package nl.andrewlalis.gymboard_api.domains.api.dao.submission;

import nl.andrewlalis.gymboard_api.domains.api.model.submission.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, String>, JpaSpecificationExecutor<Submission> {
}
