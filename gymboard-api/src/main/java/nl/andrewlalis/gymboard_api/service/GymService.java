package nl.andrewlalis.gymboard_api.service;

import jakarta.persistence.criteria.Predicate;
import nl.andrewlalis.gymboard_api.controller.dto.CompoundGymId;
import nl.andrewlalis.gymboard_api.controller.dto.ExerciseSubmissionResponse;
import nl.andrewlalis.gymboard_api.controller.dto.GymResponse;
import nl.andrewlalis.gymboard_api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseSubmissionRepository;
import nl.andrewlalis.gymboard_api.model.Gym;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
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

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(criteriaBuilder.equal(root.get("gym"), gym));
			predicates.add(criteriaBuilder.or(
					criteriaBuilder.equal(root.get("status"), ExerciseSubmission.Status.COMPLETED),
					criteriaBuilder.equal(root.get("status"), ExerciseSubmission.Status.VERIFIED)
			));

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		}, PageRequest.of(0, 10))
				.map(ExerciseSubmissionResponse::new)
				.toList();
	}
}
