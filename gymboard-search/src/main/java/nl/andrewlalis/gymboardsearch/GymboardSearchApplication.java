package nl.andrewlalis.gymboardsearch;

import nl.andrewlalis.gymboardsearch.index.JdbcIndexGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableScheduling
public class GymboardSearchApplication {

	public GymboardSearchApplication(JdbcIndexGenerator gymIndexGenerator, JdbcIndexGenerator userIndexGenerator) {
		this.gymIndexGenerator = gymIndexGenerator;
		this.userIndexGenerator = userIndexGenerator;
	}

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(GymboardSearchApplication.class, args);
	}

	private final JdbcIndexGenerator gymIndexGenerator;
	private final JdbcIndexGenerator userIndexGenerator;

	@Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
	public void reIndex() {
		gymIndexGenerator.generate();
		userIndexGenerator.generate();
	}
}
