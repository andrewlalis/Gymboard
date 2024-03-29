package nl.andrewlalis.gymboard_api.domains.auth.dao;

import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.domains.auth.model.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {
	@Modifying
	void deleteAllByUserOrReportedBy(User user, User reportedBy);
}
