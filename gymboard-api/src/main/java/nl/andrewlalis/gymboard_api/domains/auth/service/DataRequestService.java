package nl.andrewlalis.gymboard_api.domains.auth.service;

import nl.andrewlalis.gymboard_api.domains.auth.dao.UserAccountDataRequestRepository;
import nl.andrewlalis.gymboard_api.domains.auth.dao.UserRepository;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.domains.auth.model.UserAccountDataRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataRequestService {
	private final UserAccountDataRequestRepository dataRequestRepository;
	private final UserRepository userRepository;

	public DataRequestService(UserAccountDataRequestRepository dataRequestRepository, UserRepository userRepository) {
		this.dataRequestRepository = dataRequestRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public void createRequest(String userId) {
		if (dataRequestRepository.existsByUserIdAndFulfilledFalse(userId)) {
			return; // If there's already an open request that hasn't been fulfilled, ignore this one.
		}
		User user = userRepository.findById(userId).orElseThrow();
		dataRequestRepository.save(new UserAccountDataRequest(user));
	}

	// TODO: Add scheduled task and logic for preparing user data exports.
}
