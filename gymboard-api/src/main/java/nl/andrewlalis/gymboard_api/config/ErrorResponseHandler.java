package nl.andrewlalis.gymboard_api.config;

import nl.andrewlalis.gymboard_api.domains.api.dto.ApiValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for properly formatting error responses for exceptions that rest
 * controllers in this service may throw.
 */
@RestControllerAdvice
public class ErrorResponseHandler {
	@ExceptionHandler
	public ResponseEntity<?> handle(ResponseStatusException e) {
		Map<String, Object> responseContent = new HashMap<>(1);
		String message;
		if (e.getReason() != null) {
			message = e.getReason();
		} else {
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				message = "Resource not found.";
			} else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
				message = "Access to this resource is forbidden.";
			} else if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
				message = "An internal error occurred. Please try again later.";
			} else {
				message = "The request could not be completed.";
			}
		}
		responseContent.put("message", message);
		if (e instanceof ApiValidationException validationException) {
			responseContent.put("validation_messages", validationException.getValidationResponse().getMessages());
		}
		return ResponseEntity.status(e.getStatusCode()).body(responseContent);
	}
}
