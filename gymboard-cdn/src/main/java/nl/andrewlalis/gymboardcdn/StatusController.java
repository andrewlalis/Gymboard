package nl.andrewlalis.gymboardcdn;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
public class StatusController {
	@GetMapping(path = "/status")
	public ResponseEntity<?> getStatus() {
		return ResponseEntity.ok(Map.of("online", true));
	}
}
