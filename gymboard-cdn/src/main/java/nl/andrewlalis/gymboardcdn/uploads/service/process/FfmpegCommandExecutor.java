package nl.andrewlalis.gymboardcdn.uploads.service.process;

import nl.andrewlalis.gymboardcdn.uploads.service.CommandFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class FfmpegCommandExecutor {
	private static final Logger log = LoggerFactory.getLogger(FfmpegCommandExecutor.class);

	protected abstract String[] buildCommand(Path inputFile, Path outputFile);

	public void run(String label, Path inputFile, Path outputFile) throws IOException {
		String inputFilename = inputFile.getFileName().toString().strip();
		Path stdout = inputFile.resolveSibling(inputFilename + "-ffmpeg-" + label + "-out.log");
		Path stderr = inputFile.resolveSibling(inputFilename + "-ffmpeg-" + label + "-err.log");
		String[] command = buildCommand(inputFile, outputFile);
		Process process = new ProcessBuilder(buildCommand(inputFile, outputFile))
				.redirectOutput(stdout.toFile())
				.redirectError(stderr.toFile())
				.start();
		try {
			int result = process.waitFor();
			if (result != 0) {
				throw new CommandFailedException(command, result, stdout, stderr);
			}
		} catch (InterruptedException e) {
			throw new IOException("Interrupted while waiting for ffmpeg to finish.", e);
		}

		// Try to clean up output files when the command exited successfully.
		try {
			Files.deleteIfExists(stdout);
			Files.deleteIfExists(stderr);
		} catch (IOException e) {
			log.warn("Failed to delete output files after successful ffmpeg execution.", e);
		}
	}
}
