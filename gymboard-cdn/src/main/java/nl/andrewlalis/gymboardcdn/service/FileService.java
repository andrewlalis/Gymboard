package nl.andrewlalis.gymboardcdn.service;

import nl.andrewlalis.gymboardcdn.model.StoredFile;
import nl.andrewlalis.gymboardcdn.util.ULID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * The service that manages storing and retrieving files from a base filesystem.
 */
@Service
public class FileService {
	private static final Logger log = LoggerFactory.getLogger(FileService.class);

	@Value("${app.files.storage-dir}")
	private String storageDir;

	@Value("${app.files.temp-dir}")
	private String tempDir;

	private final ULID ulid;

	public FileService(ULID ulid) {
		this.ulid = ulid;
	}

	public Path getStoragePathForFile(StoredFile file) throws IOException {
		LocalDateTime time = file.getUploadedAt();
		Path dir = Path.of(storageDir)
				.resolve(Integer.toString(time.getYear()))
				.resolve(Integer.toString(time.getMonthValue()))
				.resolve(Integer.toString(time.getDayOfMonth()));
		if (Files.notExists(dir)) Files.createDirectories(dir);
		return dir.resolve(file.getId());
	}

	public String createNewFileIdentifier() {
		return ulid.nextULID();
	}

	public Path saveToTempFile(InputStream in, String filename) throws IOException {
		Path tempDir = getTempDir();
		String suffix = null;
		if (filename != null) {
			int idx = filename.lastIndexOf('.');
			if (idx >= 0) {
				suffix = filename.substring(idx);
			}
		}
		Path tempFile = Files.createTempFile(tempDir, null, suffix);
		try (var out = Files.newOutputStream(tempFile)) {
			in.transferTo(out);
		}
		return tempFile;
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
