package nl.andrewlalis.gymboard_api.model;

import nl.andrewlalis.gymboard_api.dao.CityRepository;
import nl.andrewlalis.gymboard_api.dao.CountryRepository;
import nl.andrewlalis.gymboard_api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseRepository;
import nl.andrewlalis.gymboard_api.model.exercise.Exercise;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Simple component that loads sample data that's useful when testing the application.
 */
@Component
public class SampleDataLoader implements ApplicationListener<ContextRefreshedEvent> {
	private static final Logger log = LoggerFactory.getLogger(SampleDataLoader.class);
	private final CountryRepository countryRepository;
	private final CityRepository cityRepository;
	private final GymRepository gymRepository;
	private final ExerciseRepository exerciseRepository;

	public SampleDataLoader(
		CountryRepository countryRepository,
		CityRepository cityRepository,
		GymRepository gymRepository,
		ExerciseRepository exerciseRepository
	) {
		this.countryRepository = countryRepository;
		this.cityRepository = cityRepository;
		this.gymRepository = gymRepository;
		this.exerciseRepository = exerciseRepository;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		Path markerFile = Path.of(".sample_data");
		if (Files.exists(markerFile)) return;

		log.info("Generating sample data.");
		try {
			generateSampleData();
			Files.writeString(markerFile, "Yes");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Transactional
	protected void generateSampleData() throws IOException {
		loadCsv("exercises", record -> {
			exerciseRepository.save(new Exercise(record.get(0), record.get(1)));
		});
		loadCsv("countries", record -> {
			countryRepository.save(new Country(record.get(0), record.get(1)));
		});
		loadCsv("cities", record -> {
			var country = countryRepository.findById(record.get(0)).orElseThrow();
			cityRepository.save(new City(record.get(1), record.get(2), country));
		});
		loadCsv("gyms", record -> {
			var city = cityRepository.findByShortNameAndCountryCode(record.get(1), record.get(0)).orElseThrow();
			gymRepository.save(new Gym(
					city,
					record.get(2),
					record.get(3),
					record.get(4),
					new GeoPoint(
							new BigDecimal(record.get(5)),
							new BigDecimal(record.get(6))
					),
					record.get(7)
			));
		});
	}

	private void loadCsv(String csvName, Consumer<CSVRecord> recordConsumer) throws IOException {
		var reader = new FileReader("sample_data/" + csvName + ".csv");
		for (var record : CSVFormat.DEFAULT.parse(reader)) {
			recordConsumer.accept(record);
		}
	}
}
