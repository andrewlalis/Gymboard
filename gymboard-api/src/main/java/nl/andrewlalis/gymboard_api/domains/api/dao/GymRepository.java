package nl.andrewlalis.gymboard_api.domains.api.dao;

import nl.andrewlalis.gymboard_api.domains.api.dto.CompoundGymId;
import nl.andrewlalis.gymboard_api.domains.api.model.Gym;
import nl.andrewlalis.gymboard_api.domains.api.model.GymId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GymRepository extends JpaRepository<Gym, GymId>, JpaSpecificationExecutor<Gym> {
	@Query("SELECT g FROM Gym g " +
			"WHERE g.id.shortName = :#{#id.gym()} AND " +
			"g.id.city.id.shortName = :#{#id.city()} AND " +
			"g.id.city.id.country.code = :#{#id.country()}")
	Optional<Gym> findByCompoundId(CompoundGymId id);

	@Query("SELECT COUNT(g) > 0 FROM Gym g " +
			"WHERE g.id.shortName = :#{#id.gym()} AND " +
			"g.id.city.id.shortName = :#{#id.city()} AND " +
			"g.id.city.id.country.code = :#{#id.country()}")
	boolean existsByCompoundId(CompoundGymId id);
}
