package nl.andrewlalis.gymboard_api.service.auth;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import nl.andrewlalis.gymboard_api.controller.dto.UserActivationPayload;
import nl.andrewlalis.gymboard_api.controller.dto.UserCreationPayload;
import nl.andrewlalis.gymboard_api.controller.dto.UserResponse;
import nl.andrewlalis.gymboard_api.dao.auth.UserActivationCodeRepository;
import nl.andrewlalis.gymboard_api.dao.auth.UserRepository;
import nl.andrewlalis.gymboard_api.model.auth.User;
import nl.andrewlalis.gymboard_api.model.auth.UserActivationCode;
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
	private final ULID ulid;
	private final PasswordEncoder passwordEncoder;
	private final JavaMailSender mailSender;

	@Value("${app.web-origin}")
	private String webOrigin;

	public UserService(
			UserRepository userRepository,
			UserActivationCodeRepository activationCodeRepository, ULID ulid,
			PasswordEncoder passwordEncoder,
			JavaMailSender mailSender
	) {
		this.userRepository = userRepository;
		this.activationCodeRepository = activationCodeRepository;
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
		LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
		if (activationCode.getCreatedAt().isBefore(cutoff)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code is expired.");
		}
		User user = activationCode.getUser();
		user.setActivated(true);
		userRepository.save(user);
		return new UserResponse(user);
	}
}
