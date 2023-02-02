package nl.andrewlalis.gymboardcdn.service;

import nl.andrewlalis.gymboardcdn.model.StoredFileRepository;
import nl.andrewlalis.gymboardcdn.model.VideoProcessingTaskRepository;
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
import java.util.Random;

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

	private final StoredFileRepository storedFileRepository;
	private final VideoProcessingTaskRepository videoProcessingTaskRepository;

	public FileService(StoredFileRepository storedFileRepository, VideoProcessingTaskRepository videoProcessingTaskRepository) {
		this.storedFileRepository = storedFileRepository;
		this.videoProcessingTaskRepository = videoProcessingTaskRepository;
	}

	public Path getStorageDirForTime(LocalDateTime time) throws IOException {
		Path dir = getStorageDir()
				.resolve(Integer.toString(time.getYear()))
				.resolve(Integer.toString(time.getMonthValue()))
				.resolve(Integer.toString(time.getDayOfMonth()));
		if (Files.notExists(dir)) Files.createDirectories(dir);
		return dir;
	}

	public String createNewFileIdentifier() {
		String ident = generateRandomIdentifier();
		int attempts = 0;
		while (storedFileRepository.existsByIdentifier(ident) || videoProcessingTaskRepository.existsByVideoIdentifier(ident)) {
			ident = generateRandomIdentifier();
			attempts++;
			if (attempts > 10) {
				log.warn("Took more than 10 attempts to generate a unique file identifier.");
			}
			if (attempts > 100) {
				log.error("Couldn't generate a unique file identifier after 100 attempts. Quitting!");
				throw new RuntimeException("Couldn't generate a unique file identifier.");
			}
		}
		return ident;
	}

	private String generateRandomIdentifier() {
		StringBuilder sb = new StringBuilder(9);
		String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random rand = new Random();
		for (int i = 0; i < 9; i++) sb.append(alphabet.charAt(rand.nextInt(alphabet.length())));
		return sb.toString();
	}

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
