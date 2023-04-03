package nl.andrewlalis.gymboardcdn;

import nl.andrewlalis.gymboardcdn.service.FileStorageService;
import nl.andrewlalis.gymboardcdn.util.ULID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
@EnableScheduling
public class Config {
	@Value("${app.web-origin}")
	private String webOrigin;
	@Value("${app.api-origin}")
	private String apiOrigin;

	@Bean
	public CorsFilter corsFilter() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOriginPattern(webOrigin);
		config.addAllowedOriginPattern(apiOrigin);
		config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept"));
		config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH"));
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

	@Bean
	public ULID ulid() {
		return new ULID();
	}

	@Bean
	public FileStorageService fileStorageService() {
		return new FileStorageService(ulid(), "cdn-files");
	}
}
