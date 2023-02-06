package nl.andrewlalis.gymboardsearch;

import nl.andrewlalis.gymboardsearch.index.JdbcIndexGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class GymboardSearchApplication implements CommandLineRunner {

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

	@Override
	public void run(String... args) {
		gymIndexGenerator.generate();
		userIndexGenerator.generate();
	}
}
