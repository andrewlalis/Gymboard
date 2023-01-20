package nl.andrewlalis.gymboard_api.dao;

import nl.andrewlalis.gymboard_api.model.Gym;
import nl.andrewlalis.gymboard_api.model.GymId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GymRepository extends JpaRepository<Gym, GymId> {
	@Query("SELECT g FROM Gym g " +
			"WHERE g.id.shortName = :gym AND " +
			"g.id.city.id.shortName = :city AND " +
			"g.id.city.id.country.code = :country")
	Optional<Gym> findByRawId(String gym, String city, String country);
}
