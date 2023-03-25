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

	/**
	 * Formats a large integer value as a human-readable string with a size
	 * suffix. For example, instead of 1,245,300, we would return "1.2M".
	 * @param value The value to format.
	 * @return The string representation of the value.
	 */
	public static String formatLargeInt(long value) {
		if (value < 1000) return Long.toString(value);
		long scale = 1000;
		final long MAX_SCALE = 1_000_000_000;
		final char[] SCALE_SUFFIXES = {'K', 'M', 'B'};
		int scaleSuffixIdx = 0;

		while (scale <= MAX_SCALE) {
			if (value < scale * 1000 || scale == MAX_SCALE) {
				long baseValue = value / scale;
				long remainderDigit = (value % scale) / (scale / 10);
				StringBuilder sb = new StringBuilder(6);
				sb.append(baseValue);
				if (remainderDigit > 0) sb.append('.').append(remainderDigit);
				sb.append(SCALE_SUFFIXES[scaleSuffixIdx]);
				return sb.toString();
			}
			scale *= 1000;
			scaleSuffixIdx++;
		}
		throw new IllegalStateException();
	}

	/**
	 * "Fuzzes" an integer value such that it is randomly incremented or
	 * decremented by some amount proportional to its original value, so that
	 * its original true value is obscured slightly.
	 * @param value The value to fuzz.
	 * @return The fuzzed value.
	 */
	public static long fuzzInt(long value) {
		double fuzz;
		if (value < 1000) {
			fuzz = 0.01;
		} else if (value < 1_000_000) {
			fuzz = 0.0001;
		} else {
			fuzz = 0.000001;
		}
		double modification = fuzz * Math.random() * value;
		return value + Math.round(modification);
	}
}
