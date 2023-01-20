package nl.andrewlalis.gymboard_api.dao;

import nl.andrewlalis.gymboard_api.model.City;
import nl.andrewlalis.gymboard_api.model.CityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, CityId> {
}
