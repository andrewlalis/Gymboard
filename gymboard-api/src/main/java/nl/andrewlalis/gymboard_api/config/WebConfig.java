package nl.andrewlalis.gymboard_api.config;

import nl.andrewlalis.gymboard_api.util.ULID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {
	@Bean
	public ULID ulid() {
		return new ULID();
	}
}
