package nl.andrewlalis.gymboardcdn;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The service that manages storing and retrieving files from a base filesystem.
 */
@Service
public class FileService {
	@Value("${app.files.storage-dir}")
	private String storageDir;

	@Value("${app.files.temp-dir}")
	private String tempDir;

	public Path saveToTempFile(MultipartFile file) throws IOException {
		Path tempDir = getTempDir();
		String suffix = null;
		String filename = file.getOriginalFilename();
		if (filename != null) {
			int idx = filename.lastIndexOf('.');
			if (idx >= 0) {
				suffix = filename.substring(idx);
			}
		}
		Path tempFile = Files.createTempFile(tempDir, null, suffix);
		file.transferTo(tempFile);
		return tempFile;
	}

	public Path saveToStorage(String filename, InputStream in) throws IOException {
		throw new RuntimeException("Not implemented!");
	}

	private Path getStorageDir() throws IOException {
		Path dir = Path.of(storageDir);
		if (Files.notExists(dir)) {
			Files.createDirectories(dir);
		}
		return dir;
	}

	private Path getTempDir() throws IOException {
		Path dir = Path.of(tempDir);
		if (Files.notExists(dir)) {
			Files.createDirectories(dir);
		}
		return dir;
	}
}
