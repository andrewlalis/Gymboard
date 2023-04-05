package nl.andrewlalis.gymboard_api.util.sample_data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple component that loads sample data that's useful when testing the application.
 */
@Component
@Profile("development")
public class SampleDataLoader implements ApplicationListener<ContextRefreshedEvent> {
	private static final Logger log = LoggerFactory.getLogger(SampleDataLoader.class);

	/**
	 * The list of all sample data generators that the application has loaded.
	 */
	private final List<SampleDataGenerator> generators;

	public SampleDataLoader(List<SampleDataGenerator> generators) {
		this.generators = generators;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		Path markerFile = Path.of(".sample_data");
		if (Files.exists(markerFile)) return;
		log.info("Generating sample data.");
		try {
			Set<SampleDataGenerator> completedGenerators = new HashSet<>();
			for (var gen : generators) {
				runGenerator(gen, completedGenerators);
			}
			Files.writeString(markerFile, "Yes");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void runGenerator(SampleDataGenerator gen, Set<SampleDataGenerator> completed) {
		for (var dep : gen.dependencies()) {
			runGenerator(getGeneratorByClass(dep), completed);
		}
		if (!completed.contains(gen)) {
			try {
				log.info("Running sample data generator: {}", gen.getClass().getSimpleName());
				gen.generate();
				completed.add(gen);
			} catch (Exception e) {
				throw new RuntimeException("Generator failed: " + gen.getClass().getSimpleName(), e);
			}
		}
	}

	private SampleDataGenerator getGeneratorByClass(Class<? extends SampleDataGenerator> c) {
		for (var gen : generators) {
			if (gen.getClass().equals(c)) return gen;
		}
		throw new RuntimeException("Missing generator: " + c.getSimpleName());
	}
}
