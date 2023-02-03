package nl.andrewlalis.gymboard_api.domains.api.dao;

import nl.andrewlalis.gymboard_api.domains.api.model.City;
import nl.andrewlalis.gymboard_api.domains.api.model.CityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, CityId> {
	@Query("SELECT c FROM City c WHERE c.id.shortName = :shortName AND c.id.country.code = :countryCode")
	Optional<City> findByShortNameAndCountryCode(String shortName, String countryCode);
}
