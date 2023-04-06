package nl.andrewlalis.gymboard_api.domains.auth.service;

import nl.andrewlalis.gymboard_api.domains.submission.dao.SubmissionRepository;
import nl.andrewlalis.gymboard_api.domains.auth.dao.*;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountDeletionService {
	private static final Logger logger = LoggerFactory.getLogger(UserAccountDeletionService.class);

	private final UserRepository userRepository;
	private final UserReportRepository userReportRepository;
	private final UserFollowingRepository userFollowingRepository;
	private final UserActivationCodeRepository userActivationCodeRepository;
	private final EmailResetCodeRepository emailResetCodeRepository;
	private final PasswordResetCodeRepository passwordResetCodeRepository;
	private final SubmissionRepository submissionRepository;

	public UserAccountDeletionService(UserRepository userRepository,
									  UserReportRepository userReportRepository,
									  UserFollowingRepository userFollowingRepository,
									  UserActivationCodeRepository userActivationCodeRepository,
									  EmailResetCodeRepository emailResetCodeRepository,
									  PasswordResetCodeRepository passwordResetCodeRepository,
									  SubmissionRepository submissionRepository) {
		this.userRepository = userRepository;
		this.userReportRepository = userReportRepository;
		this.userFollowingRepository = userFollowingRepository;
		this.userActivationCodeRepository = userActivationCodeRepository;
		this.emailResetCodeRepository = emailResetCodeRepository;
		this.passwordResetCodeRepository = passwordResetCodeRepository;
		this.submissionRepository = submissionRepository;
	}

	@Transactional
	public void deleteAccount(User user) {
		logger.info("Deleting user account {}", user.getEmail());

		passwordResetCodeRepository.deleteAllByUser(user);
		emailResetCodeRepository.deleteAllByUser(user);
		userActivationCodeRepository.deleteAllByUser(user);
		userReportRepository.deleteAllByUserOrReportedBy(user, user);
		userFollowingRepository.deleteAllByFollowedUserOrFollowingUser(user, user);
		submissionRepository.deleteAllByUser(user);
		userRepository.deleteById(user.getId());
	}
}
