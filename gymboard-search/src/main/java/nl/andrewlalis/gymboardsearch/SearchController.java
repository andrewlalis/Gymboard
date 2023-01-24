package nl.andrewlalis.gymboardsearch;

import nl.andrewlalis.gymboardsearch.dto.GymResponse;
import nl.andrewlalis.gymboardsearch.index.GymIndexSearcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {
	private final GymIndexSearcher gymIndexSearcher;

	public SearchController(GymIndexSearcher gymIndexSearcher) {
		this.gymIndexSearcher = gymIndexSearcher;
	}

	@GetMapping(path = "/search/gyms")
	public List<GymResponse> searchGyms(@RequestParam(name = "q", required = false) String query) {
		return gymIndexSearcher.searchGyms(query);
	}
}
