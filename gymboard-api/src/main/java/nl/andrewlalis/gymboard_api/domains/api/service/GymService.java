package nl.andrewlalis.gymboard_api.domains.api.service;

import nl.andrewlalis.gymboard_api.domains.api.dto.CompoundGymId;
import nl.andrewlalis.gymboard_api.domains.api.dto.SubmissionResponse;
import nl.andrewlalis.gymboard_api.domains.api.dto.GymResponse;
import nl.andrewlalis.gymboard_api.domains.api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.domains.api.dao.submission.SubmissionRepository;
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
	private final SubmissionRepository submissionRepository;

	public GymService(GymRepository gymRepository, SubmissionRepository submissionRepository) {
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
	public List<SubmissionResponse> getRecentSubmissions(CompoundGymId id) {
		Gym gym = gymRepository.findByCompoundId(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return submissionRepository.findAll((root, query, criteriaBuilder) -> {
			query.orderBy(
					criteriaBuilder.desc(root.get("performedAt")),
					criteriaBuilder.desc(root.get("createdAt"))
			);
			query.distinct(true);
			return PredicateBuilder.and(criteriaBuilder)
					.with(criteriaBuilder.equal(root.get("gym"), gym))
					.with(criteriaBuilder.isTrue(root.get("verified")))
					.build();
		}, PageRequest.of(0, 5))
				.map(SubmissionResponse::new)
				.toList();
	}
}
