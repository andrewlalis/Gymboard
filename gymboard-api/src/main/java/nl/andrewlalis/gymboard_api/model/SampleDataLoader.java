package nl.andrewlalis.gymboard_api.model;

import nl.andrewlalis.gymboard_api.dao.CityRepository;
import nl.andrewlalis.gymboard_api.dao.CountryRepository;
import nl.andrewlalis.gymboard_api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseRepository;
import nl.andrewlalis.gymboard_api.model.exercise.Exercise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

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
		generateSampleData();
		try {
			Files.writeString(markerFile, "Yes");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Transactional
	protected void generateSampleData() {
		Exercise benchPress = exerciseRepository.save(new Exercise("barbell-bench-press", "Barbell Bench Press"));
		Exercise squat = exerciseRepository.save(new Exercise("barbell-squat", "Barbell Squat"));
		Exercise deadlift = exerciseRepository.save(new Exercise("deadlift", "Deadlift"));

		Country nl = countryRepository.save(new Country("nl", "Netherlands"));
		City groningen = cityRepository.save(new City("groningen", "Groningen", nl));
		Gym g1 = gymRepository.save(new Gym(
				groningen,
				"trainmore-munnekeholm",
				"Trainmore Munnekeholm",
				"https://trainmore.nl/clubs/munnekeholm/",
				new GeoPoint(new BigDecimal("53.215939"), new BigDecimal("6.561549")),
				"Munnekeholm 1, 9711 JA Groningen"
		));
		Gym g2 = gymRepository.save(new Gym(
				groningen,
				"trainmore-oude-ebbinge",
				"Trainmore Oude Ebbinge Non-Stop",
				"https://trainmore.nl/clubs/oude-ebbinge/",
				new GeoPoint(new BigDecimal("53.220900"), new BigDecimal("6.565976")),
				"Oude Ebbingestraat 54-58, 9712 HL Groningen"
		));


		Country us = countryRepository.save(new Country("us", "United States"));
		City tampa = cityRepository.save(new City("tampa", "Tampa", us));
		Gym g3 = gymRepository.save(new Gym(
				tampa,
				"powerhouse-gym",
				"Powerhouse Gym Athletic Club",
				"http://www.pgathleticclub.com/",
				new GeoPoint(new BigDecimal("27.997223"), new BigDecimal("-82.496237")),
				"3251-A W Hillsborough Ave, Tampa, FL 33614, United States"
		));
	}
}
