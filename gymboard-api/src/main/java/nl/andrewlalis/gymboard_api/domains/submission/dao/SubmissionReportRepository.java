package nl.andrewlalis.gymboard_api.domains.submission.dao;

import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.domains.submission.model.SubmissionReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionReportRepository extends JpaRepository<SubmissionReport, Long> {
	@Modifying
	void deleteAllByUser(User user);
}
