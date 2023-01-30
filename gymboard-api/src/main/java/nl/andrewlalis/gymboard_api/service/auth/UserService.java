package nl.andrewlalis.gymboard_api.service.auth;

import nl.andrewlalis.gymboard_api.controller.dto.UserCreationPayload;
import nl.andrewlalis.gymboard_api.controller.dto.UserResponse;
import nl.andrewlalis.gymboard_api.dao.auth.UserRepository;
import nl.andrewlalis.gymboard_api.model.auth.User;
import nl.andrewlalis.gymboard_api.util.ULID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final ULID ulid;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, ULID ulid, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.ulid = ulid;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public UserResponse getUser(String id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new UserResponse(user);
	}

	@Transactional
	public UserResponse createUser(UserCreationPayload payload) {
		// TODO: Validate user payload.
		User user = userRepository.save(new User(
				ulid.nextULID(),
				true, // TODO: Change this to false once email activation is in.
				payload.email(),
				passwordEncoder.encode(payload.password()),
				payload.name()
		));
		return new UserResponse(user);
	}
}
