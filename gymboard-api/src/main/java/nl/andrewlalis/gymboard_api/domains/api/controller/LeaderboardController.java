package nl.andrewlalis.gymboard_api.domains.api.controller;

import nl.andrewlalis.gymboard_api.domains.submission.dto.SubmissionResponse;
import nl.andrewlalis.gymboard_api.domains.api.service.LeaderboardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(path = "/leaderboards")
public class LeaderboardController {
	private final LeaderboardService leaderboardService;

	public LeaderboardController(LeaderboardService leaderboardService) {
		this.leaderboardService = leaderboardService;
	}

	@GetMapping
	public Page<SubmissionResponse> getLeaderboard(
			@RequestParam(name = "exercise") Optional<String> exerciseShortName,
			@RequestParam(name = "gyms") Optional<String> gymCompoundIdsString,
			@RequestParam(name = "t") Optional<String> timeframe,
			Pageable pageable
	) {
		return leaderboardService.getTopSubmissions(
				exerciseShortName,
				gymCompoundIdsString,
				timeframe,
				pageable
		);
	}
}
