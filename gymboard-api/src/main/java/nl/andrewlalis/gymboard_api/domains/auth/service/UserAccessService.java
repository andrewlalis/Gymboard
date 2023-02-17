package nl.andrewlalis.gymboard_api.domains.auth.service;

import nl.andrewlalis.gymboard_api.domains.auth.dao.UserFollowingRepository;
import nl.andrewlalis.gymboard_api.domains.auth.dao.UserRepository;
import nl.andrewlalis.gymboard_api.domains.auth.model.TokenAuthentication;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * A simple service that provides methods to determine whether a user has access
 * to another user's data.
 */
@Service
public class UserAccessService {
	private final UserRepository userRepository;
	private final UserFollowingRepository followingRepository;

	public UserAccessService(UserRepository userRepository, UserFollowingRepository followingRepository) {
		this.userRepository = userRepository;
		this.followingRepository = followingRepository;
	}

	/**
	 * Determines if the given user is allowed to access a user. This method is
	 * meant to be used during a persistence session!
	 * @param user The user who's trying to access.
	 * @param targetUser The target user.
	 * @return True if the user may access them, or false otherwise.
	 */
	public boolean userHasAccess(User user, User targetUser) {
		if (targetUser != null && !targetUser.getPreferences().isAccountPrivate()) {
			return true;
		}
		if (user != null && targetUser != null && user.getId().equals(targetUser.getId())) {
			return true;
		}
		return user != null && followingRepository.existsByFollowedUserAndFollowingUser(targetUser, user);
	}

	public boolean currentUserHasAccess(User targetUser) {
		User currentUser = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth instanceof TokenAuthentication tokenAuth) {
			currentUser = tokenAuth.user();
		}
		return userHasAccess(currentUser, targetUser);
	}

	public void enforceUserAccess(User targetUser) {
		if (!currentUserHasAccess(targetUser)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to access that user.");
		}
	}

	/**
	 * Determines if the given user is allowed to access the user with the given
	 * id. Generally, a user can access another user if any of the following are
	 * true:
	 * - The target user's account is set as public via their preferences.
	 * - The accessing user is a follower of the target user.
	 * @param user The user who's trying to access.
	 * @param userId The id of the target user.
	 * @return True if the user may access them, or false otherwise.
	 */
	@Transactional(readOnly = true)
	public boolean userHasAccess(User user, String userId) {
		User targetUser = userRepository.findById(userId).orElse(null);
		return userHasAccess(user, targetUser);
	}

	/**
	 * Determines if the currently authenticated user is allowed to access the
	 * user with the given id.
	 * @param userId The id of the target user.
	 * @return True if the user may access them, or false otherwise.
	 */
	@Transactional(readOnly = true)
	public boolean currentUserHasAccess(String userId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if (auth instanceof TokenAuthentication tokenAuth) {
			user = tokenAuth.user();
		}
		return userHasAccess(user, userId);
	}
}
