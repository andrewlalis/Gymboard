package nl.andrewlalis.gymboardcdn.uploads.service.process;

import java.io.IOException;
import java.nio.file.Path;

public class FfmpegThumbnailGenerator extends FfmpegCommandExecutor implements ThumbnailGenerator {
	@Override
	public void generateThumbnailImage(Path videoInputFile, Path outputFilePath) throws IOException {
		super.run("thm", videoInputFile, outputFilePath);
	}

	@Override
	protected String[] buildCommand(Path inputFile, Path outputFile) {
		return new String[]{
				"ffmpeg",
				"-i", inputFile.toString(),
				"-vframes", "1",
				outputFile.toString()
		};
	}
}
