package nl.andrewlalis.gymboard_api.util;

import nl.andrewlalis.gymboard_api.domains.api.dao.CityRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.CountryRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.exercise.ExerciseRepository;
import nl.andrewlalis.gymboard_api.domains.api.dto.CompoundGymId;
import nl.andrewlalis.gymboard_api.domains.api.dto.ExerciseSubmissionPayload;
import nl.andrewlalis.gymboard_api.domains.api.model.*;
import nl.andrewlalis.gymboard_api.domains.api.model.exercise.Exercise;
import nl.andrewlalis.gymboard_api.domains.api.service.cdn_client.CdnClient;
import nl.andrewlalis.gymboard_api.domains.api.service.submission.ExerciseSubmissionService;
import nl.andrewlalis.gymboard_api.domains.auth.dao.RoleRepository;
import nl.andrewlalis.gymboard_api.domains.auth.dao.UserPersonalDetailsRepository;
import nl.andrewlalis.gymboard_api.domains.auth.dao.UserPreferencesRepository;
import nl.andrewlalis.gymboard_api.domains.auth.dao.UserRepository;
import nl.andrewlalis.gymboard_api.domains.auth.dto.UserCreationPayload;
import nl.andrewlalis.gymboard_api.domains.auth.model.Role;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.domains.auth.model.UserPersonalDetails;
import nl.andrewlalis.gymboard_api.domains.auth.service.UserService;
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
import java.time.LocalDate;

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
	private final UserPersonalDetailsRepository personalDetailsRepository;
	private final UserPreferencesRepository preferencesRepository;
	private final UserService userService;

	@Value("${app.cdn-origin}")
	private String cdnOrigin;

	public SampleDataLoader(
			CountryRepository countryRepository,
			CityRepository cityRepository,
			GymRepository gymRepository,
			ExerciseRepository exerciseRepository,
			ExerciseSubmissionService submissionService,
			RoleRepository roleRepository, UserRepository userRepository, UserPersonalDetailsRepository personalDetailsRepository, UserPreferencesRepository preferencesRepository, UserService userService) {
		this.countryRepository = countryRepository;
		this.cityRepository = cityRepository;
		this.gymRepository = gymRepository;
		this.exerciseRepository = exerciseRepository;
		this.submissionService = submissionService;
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.personalDetailsRepository = personalDetailsRepository;
		this.preferencesRepository = preferencesRepository;
		this.userService = userService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		Path markerFile = Path.of(".sample_data");
		if (Files.exists(markerFile)) return;

		log.info("Generating sample data.");
		try {
			generateSampleData();
			secondPassGenerateSampleData();
			Files.writeString(markerFile, "Yes");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Transactional
	protected void generateSampleData() throws Exception {
		loadCsv("exercises", record -> {
			exerciseRepository.save(new Exercise(record.get("short-name"), record.get("name")));
		});
		loadCsv("countries", record -> {
			countryRepository.save(new Country(record.get("code"), record.get("name")));
		});
		loadCsv("cities", record -> {
			var country = countryRepository.findById(record.get("country-code")).orElseThrow();
			String shortName = record.get("short-name");
			String name = record.get("name");
			cityRepository.save(new City(shortName, name, country));
		});
		loadCsv("gyms", record -> {
			var city = cityRepository.findByShortNameAndCountryCode(
					record.get("city-short-name"),
					record.get("country-code")
			).orElseThrow();
			gymRepository.save(new Gym(
					city,
					record.get("short-name"),
					record.get("name"),
					record.get("website-url"),
					new GeoPoint(
							new BigDecimal(record.get("latitude")),
							new BigDecimal(record.get("longitude"))
					),
					record.get("street-address")
			));
		});

		// Loading sample submissions involves sending content to the Gymboard CDN service.
		// We upload a video for each submission, and wait until all uploads are processed before continuing.

		final CdnClient cdnClient = new CdnClient(cdnOrigin);

		loadCsv("submissions", record -> {
			var exercise = exerciseRepository.findById(record.get("exercise-short-name")).orElseThrow();
			BigDecimal weight = new BigDecimal(record.get("raw-weight"));
			WeightUnit unit = WeightUnit.parse(record.get("weight-unit"));
			int reps = Integer.parseInt(record.get("reps"));
			String name = record.get("submitter-name");
			CompoundGymId gymId = CompoundGymId.parse(record.get("gym-id"));
			String videoFilename = record.get("video-filename");

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
		});

		loadCsv("users", record -> {
			String email = record.get("email");
			String password = record.get("password");
			String name = record.get("name");
			String[] roleNames = record.get("roles").split("\\s*\\n\\s*");
			LocalDate birthDate = LocalDate.parse(record.get("birth-date"));
			BigDecimal currentWeight = new BigDecimal(record.get("current-weight"));
			WeightUnit currentWeightUnit = WeightUnit.parse(record.get("current-weight-unit"));
			BigDecimal metricWeight = new BigDecimal(currentWeight.toString());
			if (currentWeightUnit == WeightUnit.POUNDS) {
				metricWeight = WeightUnit.toKilograms(metricWeight);
			}
			UserPersonalDetails.PersonSex sex = UserPersonalDetails.PersonSex.parse(record.get("sex"));

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
			var pd = personalDetailsRepository.findById(user.getId()).orElseThrow();
			pd.setBirthDate(birthDate);
			pd.setCurrentWeight(currentWeight);
			pd.setCurrentWeightUnit(currentWeightUnit);
			pd.setCurrentMetricWeight(metricWeight);
			pd.setSex(sex);
			personalDetailsRepository.save(pd);
			var p = preferencesRepository.findById(user.getId()).orElseThrow();
			p.setLocale(record.get("locale"));
			p.setAccountPrivate(Boolean.parseBoolean(record.get("account-private")));
			preferencesRepository.save(p);
		});
	}

	@Transactional
	protected void secondPassGenerateSampleData() throws Exception {
		loadCsv("users", record -> {
			String email = record.get("email");
			String[] followingEmails = record.get("following").split("\\s*\\n\\s*");
			User user = userRepository.findByEmail(email).orElseThrow();
			for (String followingEmail : followingEmails) {
				User userToFollow = userRepository.findByEmail(followingEmail).orElseThrow();
				userService.followUser(user.getId(), userToFollow.getId());
			}
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
		CSVFormat format = CSVFormat.DEFAULT.builder()
				.setHeader()
				.setSkipHeaderRecord(true)
				.build();
		for (var record : format.parse(reader)) {
			try {
				recordConsumer.accept(record);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
