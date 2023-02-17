package nl.andrewlalis.gymboard_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	private final TokenAuthenticationFilter tokenAuthenticationFilter;

	public SecurityConfig(TokenAuthenticationFilter tokenAuthenticationFilter) {
		this.tokenAuthenticationFilter = tokenAuthenticationFilter;
	}

	/**
	 * Defines the security configuration we'll use for this API.
	 * @param http The security configurable.
	 * @return The filter chain to apply.
	 * @throws Exception If an error occurs while configuring.
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.httpBasic().disable()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
			.csrf().disable()
			.cors().and()
			.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.authorizeHttpRequests()
				.requestMatchers(// Allow the following GET endpoints to be public.
						HttpMethod.GET,
						"/exercises",
						"/leaderboards",
						"/gyms/**",
						"/submissions/**",
						"/auth/reset-password",
						"/auth/users/*",
						"/auth/users/*/access",
						"/auth/users/*/followers",
						"/auth/users/*/following",
						"/users/*/recent-submissions"
				).permitAll()
				.requestMatchers(// Allow the following POST endpoints to be public.
						HttpMethod.POST,
						"/auth/token",
						"/auth/register",
						"/auth/activate",
						"/auth/reset-password"
				).permitAll()
				// Everything else must be authenticated, just to be safe.
				.anyRequest().authenticated();
		return http.build();
	}

	@Value("${app.web-origin}")
	private String webOrigin;

	/**
	 * Defines the CORS configuration for this API, which is to say that we
	 * allow cross-origin requests ONLY from the web app for the vast majority
	 * of endpoints.
	 * @return The CORS configuration source.
	 */
	@Bean
	@Order(1)
	public CorsConfigurationSource corsConfigurationSource() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		// Don't do this in production, use a proper list  of allowed origins
		config.addAllowedOriginPattern(webOrigin);
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		return null;// Disable the standard spring authentication manager.
	}
}
