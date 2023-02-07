package nl.andrewlalis.gymboard_api.util.sample_data;

import nl.andrewlalis.gymboard_api.domains.api.dao.exercise.ExerciseRepository;
import nl.andrewlalis.gymboard_api.domains.api.dto.CompoundGymId;
import nl.andrewlalis.gymboard_api.domains.api.dto.ExerciseSubmissionPayload;
import nl.andrewlalis.gymboard_api.domains.api.model.WeightUnit;
import nl.andrewlalis.gymboard_api.domains.api.service.cdn_client.CdnClient;
import nl.andrewlalis.gymboard_api.domains.api.service.submission.ExerciseSubmissionService;
import nl.andrewlalis.gymboard_api.util.CsvUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

@Component
@Profile("development")
public class SampleSubmissionGenerator implements SampleDataGenerator {
	private final ExerciseRepository exerciseRepository;
	private final ExerciseSubmissionService submissionService;

	@Value("${app.cdn-origin}")
	private String cdnOrigin;

	public SampleSubmissionGenerator(ExerciseRepository exerciseRepository, ExerciseSubmissionService submissionService) {
		this.exerciseRepository = exerciseRepository;
		this.submissionService = submissionService;
	}

	@Override
	public void generate() throws Exception {
		final CdnClient cdnClient = new CdnClient(cdnOrigin);
		CsvUtil.load(Path.of("sample_data", "submissions.csv"), r -> {
			var exercise = exerciseRepository.findById(r.get("exercise-short-name")).orElseThrow();
			BigDecimal weight = new BigDecimal(r.get("raw-weight"));
			WeightUnit unit = WeightUnit.parse(r.get("weight-unit"));
			int reps = Integer.parseInt(r.get("reps"));
			String name = r.get("submitter-name");
			CompoundGymId gymId = CompoundGymId.parse(r.get("gym-id"));
			String videoFilename = r.get("video-filename");

			var video = cdnClient.uploads.uploadVideo(Path.of("sample_data", videoFilename), "video/mp4");
			submissionService.createSubmission(gymId, new ExerciseSubmissionPayload(
					name,
					exercise.getShortName(),
					weight.floatValue(),
					unit.name(),
					reps,
					video.id()
			));
		});
	}

	@Override
	public Collection<Class<? extends SampleDataGenerator>> dependencies() {
		return Set.of(SampleExerciseGenerator.class, SampleUserGenerator.class);
	}
}
