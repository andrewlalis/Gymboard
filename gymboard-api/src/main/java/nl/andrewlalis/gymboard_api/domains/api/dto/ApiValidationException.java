package nl.andrewlalis.gymboard_api.domains.api.dto;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ApiValidationException extends ResponseStatusException {
	private final ValidationResponse validationResponse;

	public ApiValidationException(ValidationResponse validationResponse) {
		super(HttpStatus.BAD_REQUEST, "Validation failed.");
		this.validationResponse = validationResponse;
	}

	public ValidationResponse getValidationResponse() {
		return validationResponse;
	}
}
