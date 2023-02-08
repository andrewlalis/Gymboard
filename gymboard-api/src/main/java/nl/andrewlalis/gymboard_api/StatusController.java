package nl.andrewlalis.gymboard_api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StatusController {
	@GetMapping(path = "/status")
	public ResponseEntity<?> getServiceStatus() {
		return ResponseEntity.ok(Map.of("online", true));
	}
}
