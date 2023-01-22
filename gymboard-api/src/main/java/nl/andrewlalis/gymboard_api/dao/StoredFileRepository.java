package nl.andrewlalis.gymboard_api.dao;

import nl.andrewlalis.gymboard_api.model.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {
}
