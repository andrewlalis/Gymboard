package nl.andrewlalis.gymboard_api.domains.api.service;

import nl.andrewlalis.gymboard_api.domains.api.dto.CompoundGymId;
import nl.andrewlalis.gymboard_api.domains.api.dto.SubmissionResponse;
import nl.andrewlalis.gymboard_api.domains.api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.ExerciseRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.submission.SubmissionRepository;
import nl.andrewlalis.gymboard_api.domains.api.model.Gym;
import nl.andrewlalis.gymboard_api.domains.api.model.LeaderboardTimeframe;
import nl.andrewlalis.gymboard_api.domains.api.model.Exercise;
import nl.andrewlalis.gymboard_api.util.PredicateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This service is responsible for the various methods of fetching submissions
 * for a gym's leaderboard pages.
 */
@Service
public class LeaderboardService {
	private final SubmissionRepository submissionRepository;
	private final ExerciseRepository exerciseRepository;
	private final GymRepository gymRepository;

	public LeaderboardService(SubmissionRepository submissionRepository, ExerciseRepository exerciseRepository, GymRepository gymRepository) {
		this.submissionRepository = submissionRepository;
		this.exerciseRepository = exerciseRepository;
		this.gymRepository = gymRepository;
	}

	@Transactional(readOnly = true)
	public Page<SubmissionResponse> getTopSubmissions(
			Optional<String> exerciseShortName,
			Optional<String> gymCompoundIdsString,
			Optional<String> optionalTimeframe,
			Pageable pageable
	) {
		Optional<LocalDateTime> cutoffTime = optionalTimeframe.flatMap(s ->
				LeaderboardTimeframe.parse(s, LeaderboardTimeframe.DAY)
						.getCutoffTime(LocalDateTime.now())
		);
		Optional<Exercise> optionalExercise = exerciseShortName.flatMap(exerciseRepository::findById);
		List<Gym> gyms = gymCompoundIdsString.map(this::parseGymCompoundIdsString).orElse(Collections.emptyList());

		return submissionRepository.findAll((root, query, criteriaBuilder) -> {
			query.distinct(true);
			query.orderBy(criteriaBuilder.desc(root.get("metricWeight")));

			PredicateBuilder pb = PredicateBuilder.and(criteriaBuilder);

			cutoffTime.ifPresent(time -> pb.with(criteriaBuilder.greaterThan(root.get("createdAt"), time)));
			optionalExercise.ifPresent(exercise -> pb.with(criteriaBuilder.equal(root.get("exercise"), exercise)));
			if (!gyms.isEmpty()) {
				PredicateBuilder or = PredicateBuilder.or(criteriaBuilder);
				for (Gym gym : gyms) {
					or.with(criteriaBuilder.equal(root.get("gym"), gym));
				}
				pb.with(or.build());
			}

			return pb.build();
		}, pageable).map(SubmissionResponse::new);
	}

	private List<Gym> parseGymCompoundIdsString(String s) {
		if (s == null || s.isBlank()) return Collections.emptyList();
		String[] ids = s.split(",");
		List<Gym> gyms = new ArrayList<>(ids.length);
		for (String compoundId : ids) {
			try {
				CompoundGymId id = CompoundGymId.parse(compoundId);
				gymRepository.findByCompoundId(id).ifPresent(gyms::add);
			} catch (ResponseStatusException ignored) {}
		}
		return gyms;
	}
}
