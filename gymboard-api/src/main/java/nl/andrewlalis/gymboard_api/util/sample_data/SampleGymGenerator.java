package nl.andrewlalis.gymboard_api.util.sample_data;

import nl.andrewlalis.gymboard_api.domains.api.dao.CityRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.domains.api.model.GeoPoint;
import nl.andrewlalis.gymboard_api.domains.api.model.Gym;
import nl.andrewlalis.gymboard_api.util.CsvUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

@Component
@Profile("development")
public class SampleGymGenerator implements SampleDataGenerator {
	private final CityRepository cityRepository;
	private final GymRepository gymRepository;

	public SampleGymGenerator(CityRepository cityRepository, GymRepository gymRepository) {
		this.cityRepository = cityRepository;
		this.gymRepository = gymRepository;
	}

	@Override
	public void generate() throws Exception {
		CsvUtil.load(Path.of("sample_data", "gyms.csv"), r -> {
			var city = cityRepository.findByShortNameAndCountryCode(
					r.get("city-short-name"),
					r.get("country-code")
			).orElseThrow();
			gymRepository.save(new Gym(
					city,
					r.get("short-name"),
					r.get("name"),
					r.get("website-url"),
					new GeoPoint(
							new BigDecimal(r.get("latitude")),
							new BigDecimal(r.get("longitude"))
					),
					r.get("street-address")
			));
		});
	}

	@Override
	public Collection<Class<? extends SampleDataGenerator>> dependencies() {
		return Set.of(SampleGeoDataGenerator.class);
	}
}
