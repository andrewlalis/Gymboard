package nl.andrewlalis.gymboardcdn.uploads.service.process;

import java.io.IOException;
import java.nio.file.Path;

public interface VideoProcessor {
	void processVideo(Path inputFilePath, Path outputFilePath) throws IOException;
}
