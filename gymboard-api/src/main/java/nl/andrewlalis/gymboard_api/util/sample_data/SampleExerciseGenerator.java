package nl.andrewlalis.gymboard_api.util.sample_data;

import nl.andrewlalis.gymboard_api.domains.api.dao.exercise.ExerciseRepository;
import nl.andrewlalis.gymboard_api.domains.api.model.exercise.Exercise;
import nl.andrewlalis.gymboard_api.util.CsvUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@Profile("development")
public class SampleExerciseGenerator implements SampleDataGenerator {
	private final ExerciseRepository exerciseRepository;

	public SampleExerciseGenerator(ExerciseRepository exerciseRepository) {
		this.exerciseRepository = exerciseRepository;
	}

	@Override
	public void generate() throws Exception {
		CsvUtil.load(Path.of("sample_data", "exercises.csv"), r -> {
			exerciseRepository.save(new Exercise(r.get("short-name"), r.get("name")));
		});
	}
}
