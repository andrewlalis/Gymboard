package nl.andrewlalis.gymboardcdn.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {
	Optional<StoredFile> findByIdentifier(String identifier);
	boolean existsByIdentifier(String identifier);
}
