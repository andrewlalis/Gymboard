package nl.andrewlalis.gymboard_api.service;

import nl.andrewlalis.gymboard_api.controller.dto.ExerciseResponse;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExerciseService {
	private final ExerciseRepository exerciseRepository;

	public ExerciseService(ExerciseRepository exerciseRepository) {
		this.exerciseRepository = exerciseRepository;
	}

	@Transactional(readOnly = true)
	public List<ExerciseResponse> getExercises() {
		return exerciseRepository.findAll(Sort.by("shortName"))
				.stream().map(ExerciseResponse::new).toList();
	}
}
