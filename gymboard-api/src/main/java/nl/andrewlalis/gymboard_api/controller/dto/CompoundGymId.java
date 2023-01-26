package nl.andrewlalis.gymboard_api.controller.dto;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record CompoundGymId(String country, String city, String gym) {
	/**
	 * Parses a compound gym id from a string expression.
	 * <p>
	 *     For example, `nl_groningen_trainmore-munnekeholm`.
	 * </p>
	 * @param idStr The id string.
	 * @return The compound gym id.
	 * @throws ResponseStatusException A not found exception is thrown if the id
	 * string is invalid.
	 */
	public static CompoundGymId parse(String idStr) throws ResponseStatusException {
		if (idStr == null || idStr.isBlank()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		String[] parts = idStr.strip().toLowerCase().split("_");
		if (parts.length != 3) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

		return new CompoundGymId(
				parts[0],
				parts[1],
				parts[2]
		);
	}
}
