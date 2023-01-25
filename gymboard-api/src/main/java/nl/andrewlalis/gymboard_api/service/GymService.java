package nl.andrewlalis.gymboard_api.service;

import nl.andrewlalis.gymboard_api.controller.dto.GymResponse;
import nl.andrewlalis.gymboard_api.controller.dto.RawGymId;
import nl.andrewlalis.gymboard_api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.model.Gym;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GymService {
	private static final Logger log = LoggerFactory.getLogger(GymService.class);

	private final GymRepository gymRepository;

	public GymService(GymRepository gymRepository) {
		this.gymRepository = gymRepository;
	}

	@Transactional(readOnly = true)
	public GymResponse getGym(RawGymId id) {
		Gym gym = gymRepository.findByRawId(id.gymName(), id.cityCode(), id.countryCode())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new GymResponse(gym);
	}


}
