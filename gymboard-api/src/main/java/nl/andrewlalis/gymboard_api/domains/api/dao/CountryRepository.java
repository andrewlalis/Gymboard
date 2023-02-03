package nl.andrewlalis.gymboard_api.domains.api.dao;

import nl.andrewlalis.gymboard_api.domains.api.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, String> {
}
