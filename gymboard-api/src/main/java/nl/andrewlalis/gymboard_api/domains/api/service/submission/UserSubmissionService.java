package nl.andrewlalis.gymboard_api.domains.api.service.submission;

import nl.andrewlalis.gymboard_api.domains.api.dao.submission.SubmissionRepository;
import nl.andrewlalis.gymboard_api.domains.api.dto.SubmissionResponse;
import nl.andrewlalis.gymboard_api.domains.auth.dao.UserRepository;
import nl.andrewlalis.gymboard_api.domains.auth.model.User;
import nl.andrewlalis.gymboard_api.util.PredicateBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserSubmissionService {
	private final UserRepository userRepository;
	private final SubmissionRepository submissionRepository;

	public UserSubmissionService(UserRepository userRepository, SubmissionRepository submissionRepository) {
		this.userRepository = userRepository;
		this.submissionRepository = submissionRepository;
	}

	@Transactional(readOnly = true)
	public List<SubmissionResponse> getRecentSubmissions(String userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (user.getPreferences().isAccountPrivate()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		return submissionRepository.findAll((root, query, criteriaBuilder) -> {
			query.orderBy(
					criteriaBuilder.desc(root.get("performedAt")),
					criteriaBuilder.desc(root.get("createdAt"))
			);
			PredicateBuilder pb = PredicateBuilder.and(criteriaBuilder)
					.with(criteriaBuilder.equal(root.get("user"), user));

			return pb.build();
		}, PageRequest.of(0, 5)).map(SubmissionResponse::new).toList();
	}
}
