package nl.andrewlalis.gymboard_api.domains.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import nl.andrewlalis.gymboard_api.domains.auth.dto.TokenCredentials;
import nl.andrewlalis.gymboard_api.domains.auth.dto.TokenResponse;
import nl.andrewlalis.gymboard_api.domains.auth.dao.UserRepository;
import nl.andrewlalis.gymboard_api.domains.auth.model.Role;
import nl.andrewlalis.gymboard_api.domains.auth.model.TokenAuthentication;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * This service is responsible for generating, verifying, and generally managing
 * authentication tokens.
 */
@Service
public class TokenService {
	private static final Logger log = LoggerFactory.getLogger(TokenService.class);
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String ISSUER = "Gymboard";
	private PrivateKey privateKey = null;

	@Value("${app.auth.private-key-location}")
	private String privateKeyLocation;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public TokenService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Generates a new short-lived access token for the given user.
	 * @param user The user to generate an access token for.
	 * @return The access token string.
	 */
	private String generateAccessToken(User user) {
		Instant expiration = Instant.now().plus(30, ChronoUnit.MINUTES);
		return Jwts.builder()
				.setSubject(user.getId())
				.setIssuer(ISSUER)
				.setAudience("Gymboard App")
				.setExpiration(Date.from(expiration))
				.claim("email", user.getEmail())
				.claim("name", user.getName())
				.claim("roles", user.getRoles().stream()
						.map(Role::getShortName)
						.collect(Collectors.joining(",")))
				.signWith(getPrivateKey())
				.compact();
	}

	/**
	 * Generates a new access token for a given set of credentials.
	 * @param credentials The credentials to use for authentication.
	 * @return A token response.
	 */
	@Transactional(readOnly = true)
	public TokenResponse generateAccessToken(TokenCredentials credentials) {
		User user = userRepository.findByEmailWithRoles(credentials.email())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
		if (!passwordEncoder.matches(credentials.password(), user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		if (!user.isActivated()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		String token = generateAccessToken(user);
		return new TokenResponse(token);
	}

	public TokenResponse regenerateAccessToken(Authentication auth) {
		if (!(auth instanceof TokenAuthentication tokenAuth)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		User user = userRepository.findByIdWithRoles(tokenAuth.user().getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
		if (!user.isActivated()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		String token = generateAccessToken(user);
		return new TokenResponse(token);
	}

	public String extractBearerToken(HttpServletRequest request) {
		String authorizationHeader = request.getHeader("Authorization");
		if (authorizationHeader == null || authorizationHeader.isBlank()) return null;
		if (authorizationHeader.startsWith(BEARER_PREFIX)) {
			return authorizationHeader.substring(BEARER_PREFIX.length());
		}
		return null;
	}

	public Jws<Claims> getToken(String token) {
		if (token == null) return null;
		try {
			var builder = Jwts.parserBuilder()
					.setSigningKey(this.getPrivateKey())
					.requireIssuer(ISSUER);
			return builder.build().parseClaimsJws(token);
		} catch (Exception e) {
			log.warn("Error parsing JWT.", e);
			return null;
		}
	}

	private PrivateKey getPrivateKey() {
		if (privateKey == null) {
			try {
				byte[] keyBytes = Files.readAllBytes(Path.of(this.privateKeyLocation));
				PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
				KeyFactory kf = KeyFactory.getInstance("RSA");
				privateKey = kf.generatePrivate(spec);
			} catch (Exception e) {
				log.error("Could not obtain private key.", e);
				throw new RuntimeException("Cannot obtain private key.", e);
			}
		}
		return privateKey;
	}
}
