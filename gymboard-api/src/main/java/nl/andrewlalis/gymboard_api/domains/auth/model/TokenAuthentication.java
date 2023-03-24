package nl.andrewlalis.gymboard_api.domains.auth.model;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * The authentication instance that's used to represent a user who has
 * authenticated with an API token (so most users).
 * @param user The user who authenticated. The user entity has its roles eagerly
 *             loaded.
 * @param token The token that was used.
 */
public record TokenAuthentication(
		User user,
		String token
) implements Authentication {
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.emptyList();
	}

	@Override
	public Object getCredentials() {
		return token;
	}

	@Override
	public Object getDetails() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return user;
	}

	@Override
	public boolean isAuthenticated() {
		return true;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		// Not allowed.
	}

	@Override
	public String getName() {
		return user.getEmail();
	}
}
