package nl.andrewlalis.gymboard_api.domains.auth.service;

import nl.andrewlalis.gymboard_api.domains.auth.dao.*;
import nl.andrewlalis.gymboard_api.domains.auth.model.EmailResetCode;
import nl.andrewlalis.gymboard_api.domains.auth.model.PasswordResetCode;
import nl.andrewlalis.gymboard_api.domains.auth.model.UserActivationCode;
import nl.andrewlalis.gymboard_api.domains.auth.model.UserFollowRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class CleanupService {
	private final PasswordResetCodeRepository passwordResetCodeRepository;
	private final UserActivationCodeRepository activationCodeRepository;
	private final UserFollowRequestRepository followRequestRepository;
	private final EmailResetCodeRepository emailResetCodeRepository;
	private final UserRepository userRepository;

	public CleanupService(
			PasswordResetCodeRepository passwordResetCodeRepository,
			UserActivationCodeRepository activationCodeRepository,
			UserFollowRequestRepository followRequestRepository,
			EmailResetCodeRepository emailResetCodeRepository,
			UserRepository userRepository
	) {
		this.passwordResetCodeRepository = passwordResetCodeRepository;
		this.activationCodeRepository = activationCodeRepository;
		this.followRequestRepository = followRequestRepository;
		this.emailResetCodeRepository = emailResetCodeRepository;
		this.userRepository = userRepository;
	}

	/**
	 * Scheduled task that periodically removes all old authentication entities
	 * so that they don't clutter up the system.
	 */
	@Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
	@Transactional
	public void removeOldAuthEntities() {
		LocalDateTime passwordResetCodeCutoff = LocalDateTime.now().minus(PasswordResetCode.VALID_FOR);
		passwordResetCodeRepository.deleteAllByCreatedAtBefore(passwordResetCodeCutoff);
		LocalDateTime activationCodeCutoff = LocalDateTime.now().minus(UserActivationCode.VALID_FOR);
		activationCodeRepository.deleteAllByCreatedAtBefore(activationCodeCutoff);
		LocalDateTime followRequestCutoff = LocalDateTime.now().minus(UserFollowRequest.VALID_FOR);
		followRequestRepository.deleteAllByCreatedAtBefore(followRequestCutoff);
		LocalDateTime emailResetCodeCutoff = LocalDateTime.now().minus(EmailResetCode.VALID_FOR);
		emailResetCodeRepository.deleteAllByCreatedAtBefore(emailResetCodeCutoff);
		LocalDateTime inactiveUserCutoff = LocalDateTime.now().minusDays(7);
		userRepository.deleteAllByActivatedFalseAndCreatedAtBefore(inactiveUserCutoff);
	}
}
