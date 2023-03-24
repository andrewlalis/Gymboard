package nl.andrewlalis.gymboard_api.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.andrewlalis.gymboard_api.domains.auth.dao.UserRepository;
import nl.andrewlalis.gymboard_api.domains.auth.model.TokenAuthentication;
import nl.andrewlalis.gymboard_api.domains.auth.service.TokenService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * A filter that performs token authentication on incoming requests, and if a
 * user's token is valid, sets the security context's authentication to a new
 * instance of {@link TokenAuthentication}.
 */
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {
	private final TokenService tokenService;
	private final UserRepository userRepository;

	public TokenAuthenticationFilter(TokenService tokenService, UserRepository userRepository) {
		this.tokenService = tokenService;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		String token = tokenService.extractBearerToken(request);
		Jws<Claims> jws = tokenService.getToken(token);
		if (jws != null) {
			userRepository.findByIdWithRoles(jws.getBody().getSubject())
					.ifPresent(user -> SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication(user, token)));
		}
		filterChain.doFilter(request, response);
	}
}
