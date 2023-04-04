package nl.andrewlalis.gymboardcdn.uploads.service.process;

import java.nio.file.Path;

public interface ThumbnailGenerator {
	void generateThumbnailImage(Path videoInputFile, Path outputFilePath);
}
