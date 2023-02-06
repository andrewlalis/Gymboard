package nl.andrewlalis.gymboardsearch;

import nl.andrewlalis.gymboardsearch.dto.GymResponse;
import nl.andrewlalis.gymboardsearch.dto.UserResponse;
import nl.andrewlalis.gymboardsearch.index.QueryIndexSearcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {
	private final QueryIndexSearcher<GymResponse> gymIndexSearcher;
	private final QueryIndexSearcher<UserResponse> userIndexSearcher;

	public SearchController(QueryIndexSearcher<GymResponse> gymIndexSearcher, QueryIndexSearcher<UserResponse> userIndexSearcher) {
		this.gymIndexSearcher = gymIndexSearcher;
		this.userIndexSearcher = userIndexSearcher;
	}

	@GetMapping(path = "/search/gyms")
	public List<GymResponse> searchGyms(@RequestParam(name = "q", required = false) String query) {
		return gymIndexSearcher.search(query);
	}

	@GetMapping(path = "/search/users")
	public List<UserResponse> searchUsers(@RequestParam(name = "q", required = false) String query) {
		return userIndexSearcher.search(query);
	}
}
