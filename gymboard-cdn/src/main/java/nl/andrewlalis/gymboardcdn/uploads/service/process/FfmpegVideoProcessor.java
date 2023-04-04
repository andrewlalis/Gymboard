package nl.andrewlalis.gymboardcdn.uploads.service.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

public class FfmpegVideoProcessor extends FfmpegCommandExecutor implements VideoProcessor {
	private static final Logger log = LoggerFactory.getLogger(FfmpegVideoProcessor.class);

	@Override
	public void processVideo(Path inputFilePath, Path outputFilePath) throws IOException {
		Instant start = Instant.now();
		long inputFileSize = Files.size(inputFilePath);

		super.run("vid", inputFilePath, outputFilePath);

		Duration duration = Duration.between(start, Instant.now());
		long outputFileSize = Files.size(outputFilePath);
		double reductionFactor = inputFileSize / (double) outputFileSize;
		double durationSeconds = duration.toMillis() / 1000.0;
		log.info(
				"Processed video {} from {} to {} bytes, {} reduction in {} seconds.",
				inputFilePath.getFileName().toString(),
				inputFileSize,
				outputFileSize,
				String.format("%.3f%%", reductionFactor),
				String.format("%.3f", durationSeconds)
		);
	}

	@Override
	protected String[] buildCommand(Path inputFile, Path outputFile) {
		return new String[]{
				"ffmpeg",
				"-i", inputFile.toString(),
				"-vf", "scale=640x480:flags=lanczos",
				"-vcodec", "libx264",
				"-crf", "28",
				"-f", "mp4",
				outputFile.toString()
		};
	}
}
