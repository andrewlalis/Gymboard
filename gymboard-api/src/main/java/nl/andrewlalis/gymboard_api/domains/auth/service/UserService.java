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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static nl.andrewlalis.gymboard_api.util.DataUtils.findByIdOrThrow;

@Service
public class UserService {
	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;
	private final UserPersonalDetailsRepository userPersonalDetailsRepository;
	private final UserPreferencesRepository userPreferencesRepository;
	private final UserActivationCodeRepository activationCodeRepository;
	private final PasswordResetCodeRepository passwordResetCodeRepository;
	private final EmailResetCodeRepository emailResetCodeRepository;
	private final UserFollowingRepository userFollowingRepository;
	private final UserFollowRequestRepository followRequestRepository;
	private final UserAccessService userAccessService;
	private final UserReportRepository userReportRepository;
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
			EmailResetCodeRepository emailResetCodeRepository, UserFollowingRepository userFollowingRepository,
			UserFollowRequestRepository followRequestRepository,
			UserAccessService userAccessService,
			UserReportRepository userReportRepository, ULID ulid,
			PasswordEncoder passwordEncoder,
			JavaMailSender mailSender
	) {
		this.userRepository = userRepository;
		this.userPersonalDetailsRepository = userPersonalDetailsRepository;
		this.userPreferencesRepository = userPreferencesRepository;
		this.activationCodeRepository = activationCodeRepository;
		this.passwordResetCodeRepository = passwordResetCodeRepository;
		this.emailResetCodeRepository = emailResetCodeRepository;
		this.userFollowingRepository = userFollowingRepository;
		this.followRequestRepository = followRequestRepository;
		this.userAccessService = userAccessService;
		this.userReportRepository = userReportRepository;
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

	@Transactional
	public void generateEmailResetCode(String id, EmailUpdatePayload payload) {
		User user = findByIdOrThrow(id, userRepository);
		if (userRepository.existsByEmail(payload.newEmail())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is taken.");
		}
		EmailResetCode emailResetCode = emailResetCodeRepository.save(new EmailResetCode(
				StringGenerator.randomString(127, StringGenerator.Alphabet.ALPHANUMERIC),
				payload.newEmail(),
				user
		));
		String emailContent = String.format(
				"""
				<p>Hello %s,</p>
	
				<p>
					You've just requested to change your email from %s to this email address.
				</p>
	
				<p>
					Please click enter this code to reset your email: %s
				</p>
				""",
				user.getName(),
				user.getEmail(),
				emailResetCode.getCode()
		);
		MimeMessage msg = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
			helper.setFrom("Gymboard <noreply@gymboard.io>");
			helper.setSubject("Gymboard Account Email Update");
			helper.setTo(emailResetCode.getNewEmail());
			helper.setText(emailContent, true);
			mailSender.send(msg);
		} catch (MessagingException e) {
			log.error("Error sending user email update email.", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public void updateEmail(String userId, String code) {
		User user = findByIdOrThrow(userId, userRepository);
		EmailResetCode emailResetCode = findByIdOrThrow(code, emailResetCodeRepository);
		if (!emailResetCode.getUser().getId().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		user.setEmail(emailResetCode.getNewEmail());
		userRepository.save(user);
		emailResetCodeRepository.delete(emailResetCode);
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

	/**
	 * When a user indicates that they'd like to follow another, this method is
	 * invoked. If the person they want to follow is private, we create a new
	 * {@link UserFollowRequest} that the person must approve. Otherwise, the
	 * user just starts following the person. A 400 bad request is thrown if the
	 * user tries to follow themselves.
	 * @param followerId The id of the user that's trying to follow a user.
	 * @param followedId The id of the user that's being followed.
	 * @return A response that indicates the outcome.
	 */
	@Transactional
	public UserFollowResponse followUser(String followerId, String followedId) {
		if (followerId.equals(followedId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't follow yourself.");
		User follower = findByIdOrThrow(followerId, userRepository);
		User followed = findByIdOrThrow(followedId, userRepository);

		if (!userFollowingRepository.existsByFollowedUserAndFollowingUser(followed, follower)) {
			if (followed.getPreferences().isAccountPrivate()) {
				userFollowingRepository.save(new UserFollowing(followed, follower));
				return UserFollowResponse.requested();
			} else {
				followRequestRepository.save(new UserFollowRequest(follower, followed));
				return UserFollowResponse.followed();
			}
		}
		return UserFollowResponse.alreadyFollowed();
	}

	@Transactional
	public void unfollowUser(String followerId, String followedId) {
		if (followerId.equals(followedId)) return;
		User follower = findByIdOrThrow(followerId, userRepository);
		User followed = findByIdOrThrow(followedId, userRepository);

		userFollowingRepository.deleteByFollowedUserAndFollowingUser(followed, follower);
	}

	@Transactional
	public void respondToFollowRequest(String userId, long followRequestId, boolean approved) {
		User followedUser = findByIdOrThrow(userId, userRepository);
		UserFollowRequest followRequest = findByIdOrThrow(followRequestId, followRequestRepository);
		if (!followRequest.getUserToFollow().getId().equals(followedUser.getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		if (followRequest.getApproved() != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request already decided.");
		}
		followRequest.setApproved(approved);
		followRequestRepository.save(followRequest);
		if (approved) {
			userFollowingRepository.save(new UserFollowing(followedUser, followRequest.getRequestingUser()));
			// TODO: Send notification to the user who requested to follow.
		}
	}

	@Transactional(readOnly = true)
	public Page<UserResponse> getFollowers(String userId, Pageable pageable) {
		User user = findByIdOrThrow(userId, userRepository);
		userAccessService.enforceUserAccess(user);
		return userFollowingRepository.findAllByFollowedUserOrderByCreatedAtDesc(user, pageable)
				.map(UserFollowing::getFollowingUser)
				.map(UserResponse::new);
	}

	@Transactional(readOnly = true)
	public Page<UserResponse> getFollowing(String userId, Pageable pageable) {
		User user = findByIdOrThrow(userId, userRepository);
		userAccessService.enforceUserAccess(user);
		return userFollowingRepository.findAllByFollowingUserOrderByCreatedAtDesc(user, pageable)
				.map(UserFollowing::getFollowedUser)
				.map(UserResponse::new);
	}

	public long getFollowerCount(String userId) {
		return userFollowingRepository.countByFollowedUser(findByIdOrThrow(userId, userRepository));
	}

	public long getFollowingCount(String userId) {
		return userFollowingRepository.countByFollowingUser(findByIdOrThrow(userId, userRepository));
	}

	@Transactional(readOnly = true)
	public UserRelationshipResponse getRelationship(String user1Id, String user2Id) {
		User user1 = findByIdOrThrow(user1Id, userRepository);
		User user2 = findByIdOrThrow(user2Id, userRepository);
		userAccessService.enforceUserAccess(user1);
		boolean user1FollowingUser2 = userFollowingRepository.existsByFollowedUserAndFollowingUser(user2, user1);
		boolean user1FollowedByUser2 = userFollowingRepository.existsByFollowedUserAndFollowingUser(user1, user2);
		return new UserRelationshipResponse(
				user1FollowingUser2,
				user1FollowedByUser2
		);
	}

	@Transactional
	public void reportUser(String userId, UserReportPayload payload) {
		User user = findByIdOrThrow(userId, userRepository);
		User reporter = null;
		if (SecurityContextHolder.getContext().getAuthentication() instanceof TokenAuthentication t) {
			reporter = findByIdOrThrow(t.user().getId(), userRepository);
		}
		userReportRepository.save(new UserReport(
				user,
				reporter,
				payload.reason(),
				payload.description()
		));
	}
}
