package nl.andrewlalis.gymboard_api.domains.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import nl.andrewlalis.gymboard_api.domains.api.model.WeightUnit;
import nl.andrewlalis.gymboard_api.domains.auth.dao.*;
import nl.andrewlalis.gymboard_api.domains.auth.dto.*;
import nl.andrewlalis.gymboard_api.domains.auth.model.*;
import nl.andrewlalis.gymboard_api.util.StringGenerator;
import nl.andrewlalis.gymboard_api.util.ULID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;
	private final UserPersonalDetailsRepository userPersonalDetailsRepository;
	private final UserPreferencesRepository userPreferencesRepository;
	private final UserActivationCodeRepository activationCodeRepository;
	private final PasswordResetCodeRepository passwordResetCodeRepository;
	private final UserFollowingRepository userFollowingRepository;
	private final UserAccessService userAccessService;
	private final ULID ulid;
	private final PasswordEncoder passwordEncoder;
	private final JavaMailSender mailSender;

	@Value("${app.web-origin}")
	private String webOrigin;

	public UserService(
			UserRepository userRepository,
			UserPersonalDetailsRepository userPersonalDetailsRepository,
			UserPreferencesRepository userPreferencesRepository,
			UserActivationCodeRepository activationCodeRepository,
			PasswordResetCodeRepository passwordResetCodeRepository,
			UserFollowingRepository userFollowingRepository, UserAccessService userAccessService, ULID ulid,
			PasswordEncoder passwordEncoder,
			JavaMailSender mailSender
	) {
		this.userRepository = userRepository;
		this.userPersonalDetailsRepository = userPersonalDetailsRepository;
		this.userPreferencesRepository = userPreferencesRepository;
		this.activationCodeRepository = activationCodeRepository;
		this.passwordResetCodeRepository = passwordResetCodeRepository;
		this.userFollowingRepository = userFollowingRepository;
		this.userAccessService = userAccessService;
		this.ulid = ulid;
		this.passwordEncoder = passwordEncoder;
		this.mailSender = mailSender;
	}

	@Transactional(readOnly = true)
	public UserResponse getUser(String id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new UserResponse(user);
	}

	@Transactional
	public UserResponse createUser(UserCreationPayload payload, boolean requireActivation) {
		// TODO: Validate user payload.
		User user = userRepository.save(new User(
				ulid.nextULID(),
				!requireActivation,
				payload.email(),
				passwordEncoder.encode(payload.password()),
				payload.name()
		));
		if (requireActivation) {
			generateAndSendActivationCode(user);
		}
		return new UserResponse(user);
	}

	private void generateAndSendActivationCode(User user) {
		Random random = new SecureRandom();
		StringBuilder sb = new StringBuilder(127);
		final String alphabet = "bcdfghjkmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ23456789";
		for (int i = 0; i < 127; i++) {
			sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
		}
		String rawCode = sb.toString();
		UserActivationCode activationCode = activationCodeRepository.save(new UserActivationCode(user, rawCode));
		// Send email.
		String activationLink = webOrigin + "/activate?code=" + activationCode.getCode();
		String emailContent = String.format(
				"""
				<p>Hello %s,</p>
				
				<p>
					Thank you for registering a new account at Gymboard!
				</p>
				<p>
					Please click <a href="%s">here</a> to activate your account.
				</p>
				""",
				user.getName(),
				activationLink
		);
		MimeMessage msg = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
			helper.setFrom("Gymboard <noreply@gymboard.io>");
			helper.setSubject("Activate Your Gymboard Account");
			helper.setTo(user.getEmail());
			helper.setText(emailContent, true);
			mailSender.send(msg);
		} catch (MessagingException e) {
			log.error("Error sending user activation email.", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public UserResponse activateUser(UserActivationPayload payload) {
		UserActivationCode activationCode = activationCodeRepository.findByCode(payload.code())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
		User user = activationCode.getUser();
		if (!user.isActivated()) {
			LocalDateTime cutoff = LocalDateTime.now().minus(UserActivationCode.VALID_FOR);
			if (activationCode.getCreatedAt().isBefore(cutoff)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code is expired.");
			}
			user.setActivated(true);
			userRepository.save(user);
		}
		activationCodeRepository.delete(activationCode);
		return new UserResponse(user);
	}

	@Transactional
	public void generatePasswordResetCode(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!user.isActivated()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		PasswordResetCode passwordResetCode = passwordResetCodeRepository.save(new PasswordResetCode(
				StringGenerator.randomString(127, StringGenerator.Alphabet.ALPHANUMERIC),
				user
		));

		// Send email.
		String resetLink = webOrigin + "/password-reset?code=" + passwordResetCode.getCode();
		String emailContent = String.format(
				"""
				<p>Hello %s,</p>
				
				<p>
					You've just requested to reset your password.
				</p>
				<p>
					Please click <a href="%s">here</a> to reset your password.
				</p>
				""",
				user.getName(),
				resetLink
		);
		MimeMessage msg = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
			helper.setFrom("Gymboard <noreply@gymboard.io>");
			helper.setSubject("Gymboard Account Password Reset");
			helper.setTo(user.getEmail());
			helper.setText(emailContent, true);
			mailSender.send(msg);
		} catch (MessagingException e) {
			log.error("Error sending user password reset email.", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public void resetUserPassword(PasswordResetPayload payload) {
		PasswordResetCode code = passwordResetCodeRepository.findById(payload.code())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		LocalDateTime cutoff = LocalDateTime.now().minus(PasswordResetCode.VALID_FOR);
		if (code.getCreatedAt().isBefore(cutoff)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		// TODO: Validate password.

		User user = code.getUser();
		user.setPasswordHash(passwordEncoder.encode(payload.newPassword()));
		userRepository.save(user);
		passwordResetCodeRepository.delete(code);
	}

	@Transactional
	public void updatePassword(String id, PasswordUpdatePayload payload) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		// TODO: Validate password.

		user.setPasswordHash(passwordEncoder.encode(payload.newPassword()));
		userRepository.save(user);
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
	}

	@Transactional
	public UserPersonalDetailsResponse updatePersonalDetails(String id, UserPersonalDetailsPayload payload) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		var pd = user.getPersonalDetails();

		pd.setBirthDate(payload.birthDate());
		BigDecimal currentWeight = payload.currentWeight() == null ? null : BigDecimal.valueOf(payload.currentWeight());
		WeightUnit currentWeightUnit = WeightUnit.parse(payload.currentWeightUnit());
		BigDecimal currentMetricWeight = null;
		if (currentWeight != null) {
			if (currentWeightUnit == WeightUnit.POUNDS) {
				currentMetricWeight = WeightUnit.toKilograms(currentWeight);
			} else {
				currentMetricWeight = new BigDecimal(currentWeight.toString());
			}
		}
		pd.setCurrentWeight(currentWeight);
		pd.setCurrentWeightUnit(currentWeightUnit);
		pd.setCurrentMetricWeight(currentMetricWeight);
		pd.setSex(UserPersonalDetails.PersonSex.parse(payload.sex()));
		user = userRepository.save(user);
		return new UserPersonalDetailsResponse(user.getPersonalDetails());
	}

	@Transactional(readOnly = true)
	public UserPersonalDetailsResponse getPersonalDetails(String id) {
		var pd = userPersonalDetailsRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new UserPersonalDetailsResponse(pd);
	}

	@Transactional(readOnly = true)
	public UserPreferencesResponse getPreferences(String id) {
		var p = userPreferencesRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new UserPreferencesResponse(p);
	}

	@Transactional
	public UserPreferencesResponse updatePreferences(String id, UserPreferencesPayload payload) {
		var p = userPreferencesRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		p.setAccountPrivate(payload.accountPrivate());
		p.setLocale(payload.locale());
		p = userPreferencesRepository.save(p);
		return new UserPreferencesResponse(p);
	}

	@Transactional
	public void followUser(String followerId, String followedId) {
		if (followerId.equals(followedId)) return;
		User follower = userRepository.findById(followerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		User followed = userRepository.findById(followedId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		if (!userFollowingRepository.existsByFollowedUserAndFollowingUser(followed, follower)) {
			userFollowingRepository.save(new UserFollowing(followed, follower));
		}
	}

	@Transactional
	public void unfollowUser(String followerId, String followedId) {
		if (followerId.equals(followedId)) return;
		User follower = userRepository.findById(followerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		User followed = userRepository.findById(followedId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		userFollowingRepository.deleteByFollowedUserAndFollowingUser(followed, follower);
	}

	@Transactional(readOnly = true)
	public Page<UserResponse> getFollowers(String userId, Pageable pageable) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		userAccessService.enforceUserAccess(user);
		return userFollowingRepository.findAllByFollowedUserOrderByCreatedAtDesc(user, pageable)
				.map(UserFollowing::getFollowingUser)
				.map(UserResponse::new);
	}

	@Transactional(readOnly = true)
	public Page<UserResponse> getFollowing(String userId, Pageable pageable) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		userAccessService.enforceUserAccess(user);
		return userFollowingRepository.findAllByFollowingUserOrderByCreatedAtDesc(user, pageable)
				.map(UserFollowing::getFollowedUser)
				.map(UserResponse::new);
	}

	@Transactional(readOnly = true)
	public UserRelationshipResponse getRelationship(String user1Id, String user2Id) {
		User user1 = userRepository.findById(user1Id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		User user2 = userRepository.findById(user2Id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		userAccessService.enforceUserAccess(user1);
		boolean user1FollowingUser2 = userFollowingRepository.existsByFollowedUserAndFollowingUser(user2, user1);
		boolean user1FollowedByUser2 = userFollowingRepository.existsByFollowedUserAndFollowingUser(user1, user2);
		return new UserRelationshipResponse(
				user1FollowingUser2,
				user1FollowedByUser2
		);
	}
}
