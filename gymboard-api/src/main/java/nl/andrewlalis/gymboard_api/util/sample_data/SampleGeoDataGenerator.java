package nl.andrewlalis.gymboard_api.util.sample_data;

import nl.andrewlalis.gymboard_api.domains.api.dao.CityRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.CountryRepository;
import nl.andrewlalis.gymboard_api.domains.api.model.City;
import nl.andrewlalis.gymboard_api.domains.api.model.Country;
import nl.andrewlalis.gymboard_api.util.CsvUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@Profile("development")
public class SampleGeoDataGenerator implements SampleDataGenerator {
	private final CityRepository cityRepository;
	private final CountryRepository countryRepository;

	public SampleGeoDataGenerator(CityRepository cityRepository, CountryRepository countryRepository) {
		this.cityRepository = cityRepository;
		this.countryRepository = countryRepository;
	}

	@Override
	public void generate() throws Exception {
		CsvUtil.load(Path.of("sample_data", "countries.csv"), r -> {
			countryRepository.save(new Country(r.get("code"), r.get("name")));
		});
		CsvUtil.load(Path.of("sample_data", "cities.csv"), r -> {
			var country = countryRepository.findById(r.get("country-code")).orElseThrow();
			cityRepository.save(new City(r.get("short-name"), r.get("name"), country));
		});
	}
}
