package nl.andrewlalis.gymboardcdn;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableScheduling
public class Config {
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
}
