package nl.andrewlalis.gymboard_api.domains.api.controller;

import nl.andrewlalis.gymboard_api.domains.api.dto.ExerciseResponse;
import nl.andrewlalis.gymboard_api.domains.api.service.ExerciseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ExerciseController {
	private final ExerciseService exerciseService;

	public ExerciseController(ExerciseService exerciseService) {
		this.exerciseService = exerciseService;
	}

	@GetMapping(path = "/exercises")
	public List<ExerciseResponse> getExercises() {
		return exerciseService.getExercises();
	}
}
