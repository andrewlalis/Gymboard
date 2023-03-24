package nl.andrewlalis.gymboard_api.util;

import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class DataUtils {
	/**
	 * Finds an entity by its id, or throws a 404 not found exception.
	 * @param id The id to look for.
	 * @param repo The repository to search in.
	 * @return The entity that was found.
	 * @param <T> The entity type.
	 * @param <ID> The id type.
	 * @throws ResponseStatusException If the entity wasn't found.
	 */
	public static <T, ID> T findByIdOrThrow(ID id, CrudRepository<T, ID> repo) throws ResponseStatusException {
		return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}
}
