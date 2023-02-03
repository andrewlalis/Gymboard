package nl.andrewlalis.gymboard_api.domains.api.service;

import nl.andrewlalis.gymboard_api.domains.api.dto.CompoundGymId;
import nl.andrewlalis.gymboard_api.domains.api.dto.ExerciseSubmissionResponse;
import nl.andrewlalis.gymboard_api.domains.api.dto.GymResponse;
import nl.andrewlalis.gymboard_api.domains.api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.exercise.ExerciseSubmissionRepository;
import nl.andrewlalis.gymboard_api.domains.api.model.Gym;
import nl.andrewlalis.gymboard_api.util.PredicateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GymService {
	private static final Logger log = LoggerFactory.getLogger(GymService.class);

	private final GymRepository gymRepository;
	private final ExerciseSubmissionRepository submissionRepository;

	public GymService(GymRepository gymRepository, ExerciseSubmissionRepository submissionRepository) {
		this.gymRepository = gymRepository;
		this.submissionRepository = submissionRepository;
	}

	@Transactional(readOnly = true)
	public GymResponse getGym(CompoundGymId id) {
		Gym gym = gymRepository.findByCompoundId(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new GymResponse(gym);
	}

	@Transactional(readOnly = true)
	public List<ExerciseSubmissionResponse> getRecentSubmissions(CompoundGymId id) {
		Gym gym = gymRepository.findByCompoundId(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return submissionRepository.findAll((root, query, criteriaBuilder) -> {
			query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
			query.distinct(true);

			// TODO: Filter to only verified submissions.
			return PredicateBuilder.and(criteriaBuilder)
					.with(criteriaBuilder.equal(root.get("gym"), gym))
					.build();
		}, PageRequest.of(0, 10))
				.map(ExerciseSubmissionResponse::new)
				.toList();
	}
}
