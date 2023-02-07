package nl.andrewlalis.gymboard_api.util.sample_data;

import nl.andrewlalis.gymboard_api.domains.api.model.WeightUnit;
import nl.andrewlalis.gymboard_api.domains.auth.dao.RoleRepository;
import nl.andrewlalis.gymboard_api.domains.auth.dao.UserFollowingRepository;
import nl.andrewlalis.gymboard_api.domains.auth.dao.UserRepository;
import nl.andrewlalis.gymboard_api.domains.auth.model.Role;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.domains.auth.model.UserFollowing;
import nl.andrewlalis.gymboard_api.domains.auth.model.UserPersonalDetails;
import nl.andrewlalis.gymboard_api.util.CsvUtil;
import nl.andrewlalis.gymboard_api.util.ULID;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

@Component
@Profile("development")
public class SampleUserGenerator implements SampleDataGenerator {
	private final ULID ulid;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RoleRepository roleRepository;
	private final UserFollowingRepository followingRepository;

	public SampleUserGenerator(ULID ulid, UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, UserFollowingRepository followingRepository) {
		this.ulid = ulid;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.roleRepository = roleRepository;
		this.followingRepository = followingRepository;
	}

	@Override
	public void generate() throws Exception {
		Path usersCsvPath = Path.of("sample_data", "users.csv");
		CsvUtil.load(usersCsvPath, r -> {
			User user = new User(
					ulid.nextULID(),
					true,
					r.get("email"),
					passwordEncoder.encode(r.get("password")),
					r.get("name")
			);
			String[] roleNames = r.get("roles").split("\\s*\\n\\s*");
			for (var roleName : roleNames) {
				if (roleName.isBlank()) continue;
				Role role = roleRepository.findById(roleName.strip().toLowerCase())
						.orElseGet(() -> roleRepository.save(new Role(roleName.strip().toLowerCase())));
				user.getRoles().add(role);
			}

			// Set up the user's personal details.
			var pd = user.getPersonalDetails();
			String birthDateStr = r.get("birth-date");
			if (birthDateStr != null && !birthDateStr.isBlank()) {
				pd.setBirthDate(LocalDate.parse(birthDateStr));
			}
			String currentWeightStr = r.get("current-weight");
			String currentWeightUnitStr = r.get("current-weight-unit");
			if (
					currentWeightStr != null && !currentWeightStr.isBlank() &&
					currentWeightUnitStr != null && !currentWeightUnitStr.isBlank()
			) {
				BigDecimal currentWeight = new BigDecimal(currentWeightStr);
				WeightUnit currentWeightUnit = WeightUnit.parse(currentWeightUnitStr);
				BigDecimal metricWeight = new BigDecimal(currentWeightStr);
				if (currentWeightUnit == WeightUnit.POUNDS) {
					metricWeight = WeightUnit.toKilograms(metricWeight);
				}
				pd.setCurrentWeight(currentWeight);
				pd.setCurrentWeightUnit(currentWeightUnit);
				pd.setCurrentMetricWeight(metricWeight);
			}
			pd.setSex(UserPersonalDetails.PersonSex.parse(r.get("sex")));

			// Set up the user's preferences.
			var p = user.getPreferences();
			p.setLocale(r.get("locale"));
			p.setAccountPrivate(Boolean.parseBoolean(r.get("account-private")));
			userRepository.save(user);
		});

		// Do a second pass to add follower information.
		CsvUtil.load(usersCsvPath, r -> {
			User user = userRepository.findByEmail(r.get("email")).orElseThrow();
			String[] followingEmails = r.get("following").split("\\s+");
			for (String followingEmail : followingEmails) {
				User userToFollow = userRepository.findByEmail(followingEmail).orElseThrow();
				followingRepository.save(new UserFollowing(userToFollow, user));
			}
		});
	}
}
