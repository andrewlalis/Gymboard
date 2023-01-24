package nl.andrewlalis.gymboardsearch;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GymboardSearchApplication implements CommandLineRunner {
	private final GymIndexGenerator gymIndexGenerator;

	public GymboardSearchApplication(GymIndexGenerator gymIndexGenerator) {
		this.gymIndexGenerator = gymIndexGenerator;
	}

	public static void main(String[] args) {
		SpringApplication.run(GymboardSearchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		gymIndexGenerator.generateIndex();
	}
}
