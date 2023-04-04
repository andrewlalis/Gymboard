package nl.andrewlalis.gymboardcdn.uploads.service.process;

import nl.andrewlalis.gymboardcdn.uploads.service.CommandFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

public class FfmpegVideoProcessor implements VideoProcessor {
	private static final Logger log = LoggerFactory.getLogger(FfmpegVideoProcessor.class);

	@Override
	public void processVideo(Path inputFilePath, Path outputFilePath) throws IOException {
		String inputFilename = inputFilePath.getFileName().toString().strip();
		Path stdoutFile = inputFilePath.resolveSibling(inputFilename + "-ffmpeg-video-stdout.log");
		Path stderrFile = inputFilePath.resolveSibling(inputFilename + "-ffmpeg-video-stderr.log");
		final String[] command = {
				"ffmpeg",
				"-i", inputFilePath.toAbsolutePath().toString(),
				"-vf", "scale=640x480:flags=lanczos",
				"-vcodec", "libx264",
				"-crf", "28",
				"-f", "mp4",
				outputFilePath.toAbsolutePath().toString()
		};
		long startFileSize = Files.size(inputFilePath);
		Instant startTime = Instant.now();
		Process process = new ProcessBuilder(command)
				.redirectOutput(stdoutFile.toAbsolutePath().toFile())
				.redirectError(stderrFile.toAbsolutePath().toFile())
				.start();
		int result = process.waitFor();
		if (result != 0) {
			throw new CommandFailedException(command, result, stdoutFile, stderrFile);
		}
		long endFileSize = Files.size(outputFilePath);
		Duration duration = Duration.between(startTime, Instant.now());
		double reductionFactor = startFileSize / (double) endFileSize;
		String reductionFactorStr = String.format("%.3f%%", reductionFactor * 100);
		log.info("Processed video from {} bytes to {} bytes in {} seconds, {} reduction.", startSize, endSize, dur.getSeconds(), reductionFactorStr);

	}
}
