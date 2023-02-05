package nl.andrewlalis.gymboardsearch;

import nl.andrewlalis.gymboardsearch.index.GymIndexGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class GymboardSearchApplication implements CommandLineRunner {
	private final GymIndexGenerator gymIndexGenerator;

	public GymboardSearchApplication(GymIndexGenerator gymIndexGenerator) {
		this.gymIndexGenerator = gymIndexGenerator;
	}

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(GymboardSearchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		gymIndexGenerator.generateIndex();
	}
}
