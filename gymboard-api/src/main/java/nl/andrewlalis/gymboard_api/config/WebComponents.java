package nl.andrewlalis.gymboard_api.config;

import nl.andrewlalis.gymboard_api.domains.api.service.cdn_client.CdnClient;
import nl.andrewlalis.gymboard_api.util.ULID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class WebComponents {
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(10);
	}

	@Bean
	public ULID ulid() {
		return new ULID();
	}

	@Value("${app.cdn-origin}")
	private String cdnOrigin;
	@Value("${app.cdn-secret}")
	private String cdnSecret;

	@Bean
	public CdnClient cdnClient() {
		return new CdnClient(cdnOrigin, cdnSecret);
	}
}
