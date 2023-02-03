package nl.andrewlalis.gymboard_api.domains.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import nl.andrewlalis.gymboard_api.domains.auth.dao.PasswordResetCodeRepository;
import nl.andrewlalis.gymboard_api.domains.auth.dto.PasswordResetPayload;
import nl.andrewlalis.gymboard_api.domains.auth.dto.UserActivationPayload;
import nl.andrewlalis.gymboard_api.domains.auth.dto.UserCreationPayload;
import nl.andrewlalis.gymboard_api.domains.auth.dto.UserResponse;
import nl.andrewlalis.gymboard_api.domains.auth.dao.UserActivationCodeRepository;
import nl.andrewlalis.gymboard_api.domains.auth.dao.UserRepository;
import nl.andrewlalis.gymboard_api.domains.auth.model.PasswordResetCode;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.domains.auth.model.UserActivationCode;
import nl.andrewlalis.gymboard_api.util.StringGenerator;
import nl.andrewlalis.gymboard_api.util.ULID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UserService {
	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;
	private final UserActivationCodeRepository activationCodeRepository;
	private final PasswordResetCodeRepository passwordResetCodeRepository;
	private final ULID ulid;
	private final PasswordEncoder passwordEncoder;
	private final JavaMailSender mailSender;

	@Value("${app.web-origin}")
	private String webOrigin;

	public UserService(
			UserRepository userRepository,
			UserActivationCodeRepository activationCodeRepository,
			PasswordResetCodeRepository passwordResetCodeRepository,
			ULID ulid,
			PasswordEncoder passwordEncoder,
			JavaMailSender mailSender
	) {
		this.userRepository = userRepository;
		this.activationCodeRepository = activationCodeRepository;
		this.passwordResetCodeRepository = passwordResetCodeRepository;
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
			LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
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
		LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
		if (code.getCreatedAt().isBefore(cutoff)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		// TODO: Validate password.

		code.getUser().setPasswordHash(passwordEncoder.encode(payload.newPassword()));
		passwordResetCodeRepository.delete(code);
	}
}
