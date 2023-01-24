package nl.andrewlalis.gymboard_api.service;

import jakarta.persistence.criteria.Predicate;
import nl.andrewlalis.gymboard_api.controller.dto.GymResponse;
import nl.andrewlalis.gymboard_api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.model.Gym;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class GymService {
	private final GymRepository gymRepository;

	public GymService(GymRepository gymRepository) {
		this.gymRepository = gymRepository;
	}

	@Transactional(readOnly = true)
	public GymResponse getGym(String countryCode, String city, String gymName) {
		Gym gym = gymRepository.findByRawId(gymName, city, countryCode)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new GymResponse(gym);
	}

	@Transactional(readOnly = true)
	public List<GymResponse> searchGyms(String queryText) {
		return gymRepository.findAll((root, query, criteriaBuilder) -> {
			query.distinct(true);
			List<Predicate> predicates = new ArrayList<>();
			if (queryText != null && !queryText.isBlank()) {
				String queryTextStr = "%" + queryText.toUpperCase() + "%";
				predicates.add(criteriaBuilder.like(
						criteriaBuilder.upper(root.get("displayName")),
						queryTextStr
				));
				predicates.add(criteriaBuilder.like(
						criteriaBuilder.upper(root.get("id").get("shortName")),
						queryTextStr
				));
			}
			return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
		}).stream().map(GymResponse::new).toList();
	}
}
