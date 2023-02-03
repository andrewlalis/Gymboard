package nl.andrewlalis.gymboard_api.util;

import nl.andrewlalis.gymboard_api.controller.dto.CompoundGymId;
import nl.andrewlalis.gymboard_api.controller.dto.ExerciseSubmissionPayload;
import nl.andrewlalis.gymboard_api.controller.dto.UserCreationPayload;
import nl.andrewlalis.gymboard_api.dao.CityRepository;
import nl.andrewlalis.gymboard_api.dao.CountryRepository;
import nl.andrewlalis.gymboard_api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.dao.auth.RoleRepository;
import nl.andrewlalis.gymboard_api.dao.auth.UserRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseRepository;
import nl.andrewlalis.gymboard_api.model.City;
import nl.andrewlalis.gymboard_api.model.Country;
import nl.andrewlalis.gymboard_api.model.GeoPoint;
import nl.andrewlalis.gymboard_api.model.Gym;
import nl.andrewlalis.gymboard_api.model.auth.Role;
import nl.andrewlalis.gymboard_api.model.auth.User;
import nl.andrewlalis.gymboard_api.model.exercise.Exercise;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmission;
import nl.andrewlalis.gymboard_api.service.auth.UserService;
import nl.andrewlalis.gymboard_api.service.cdn_client.CdnClient;
import nl.andrewlalis.gymboard_api.service.submission.ExerciseSubmissionService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

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
	private final ExerciseSubmissionService submissionService;
	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final UserService userService;

	@Value("${app.cdn-origin}")
	private String cdnOrigin;

	public SampleDataLoader(
			CountryRepository countryRepository,
			CityRepository cityRepository,
			GymRepository gymRepository,
			ExerciseRepository exerciseRepository,
			ExerciseSubmissionService submissionService,
			RoleRepository roleRepository, UserRepository userRepository, UserService userService) {
		this.countryRepository = countryRepository;
		this.cityRepository = cityRepository;
		this.gymRepository = gymRepository;
		this.exerciseRepository = exerciseRepository;
		this.submissionService = submissionService;
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.userService = userService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		Path markerFile = Path.of(".sample_data");
		if (Files.exists(markerFile)) return;

		log.info("Generating sample data.");
		try {
			generateSampleData();
			Files.writeString(markerFile, "Yes");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Transactional
	protected void generateSampleData() throws Exception {
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

		// Loading sample submissions involves sending content to the Gymboard CDN service.
		// We upload a video for each submission, and wait until all uploads are processed before continuing.

		final CdnClient cdnClient = new CdnClient(cdnOrigin);
		final Set<String> videoIds = new HashSet<>();

		loadCsv("submissions", record -> {
			var exercise = exerciseRepository.findById(record.get(0)).orElseThrow();
			BigDecimal weight = new BigDecimal(record.get(1));
			ExerciseSubmission.WeightUnit unit = ExerciseSubmission.WeightUnit.valueOf(record.get(2).toUpperCase());
			int reps = Integer.parseInt(record.get(3));
			String name = record.get(4);
			CompoundGymId gymId = CompoundGymId.parse(record.get(5));
			String videoFilename = record.get(6);

			// Upload the video to the CDN, and wait until it's done processing.
			log.info("Uploading video {} to CDN...", videoFilename);
			var video = cdnClient.uploads.uploadVideo(Path.of("sample_data", videoFilename), "video/mp4");
			submissionService.createSubmission(gymId, new ExerciseSubmissionPayload(
					name,
					exercise.getShortName(),
					weight.floatValue(),
					unit.name(),
					reps,
					video.id()
			));
			videoIds.add(video.id());
		});

		int count = videoIds.size();
		while (!videoIds.isEmpty()) {
			log.info("Waiting for {} / {} videos to finish processing...", videoIds.size(), count);
			Set<String> removalSet = new HashSet<>();
			for (var videoId : videoIds) {
				String status = cdnClient.uploads.getVideoProcessingStatus(videoId).status();
				if (status.equalsIgnoreCase("COMPLETED") || status.equalsIgnoreCase("FAILED")) {
					removalSet.add(videoId);
				}
			}
			videoIds.removeAll(removalSet);
			Thread.sleep(1000);
		}

		loadCsv("users", record -> {
			String email = record.get(0);
			String password = record.get(1);
			String name = record.get(2);
			String[] roleNames = record.get(3).split("\\s*\\|\\s*");

			UserCreationPayload payload = new UserCreationPayload(email, password, name);
			var resp = userService.createUser(payload, false);
			User user = userRepository.findByIdWithRoles(resp.id()).orElseThrow();
			for (var roleName : roleNames) {
				if (roleName.isBlank()) continue;
				Role role = roleRepository.findById(roleName.strip().toLowerCase())
						.orElseGet(() -> roleRepository.save(new Role(roleName.strip().toLowerCase())));
				user.getRoles().add(role);
			}
			userRepository.save(user);
		});
	}

	@FunctionalInterface
	interface ThrowableConsumer<T> {
		void accept(T item) throws Exception;
	}

	private void loadCsv(String csvName, ThrowableConsumer<CSVRecord> recordConsumer) throws IOException {
		String path = "sample_data/" + csvName + ".csv";
		log.info("Loading data from {}...", path);
		var reader = new FileReader(path);
		for (var record : CSVFormat.DEFAULT.parse(reader)) {
			try {
				recordConsumer.accept(record);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
