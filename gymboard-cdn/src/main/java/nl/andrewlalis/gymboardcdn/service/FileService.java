package nl.andrewlalis.gymboardcdn.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import nl.andrewlalis.gymboardcdn.api.FileMetadataResponse;
import nl.andrewlalis.gymboardcdn.model.FileMetadata;
import nl.andrewlalis.gymboardcdn.util.ULID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

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

	private final ObjectMapper objectMapper = new ObjectMapper();

	public FileService(ULID ulid) {
		this.ulid = ulid;
	}

	public Path getStoragePathForFile(String rawId) {
		ULID.Value id = ULID.parseULID(rawId);
		LocalDateTime time = dateFromULID(id);
		Path dir = Path.of(storageDir)
				.resolve(Integer.toString(time.getYear()))
				.resolve(Integer.toString(time.getMonthValue()))
				.resolve(Integer.toString(time.getDayOfMonth()));
		return dir.resolve(rawId);
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

	private static LocalDateTime dateFromULID(ULID.Value value) {
		return Instant.ofEpochMilli(value.timestamp())
				.atOffset(ZoneOffset.UTC)
				.toLocalDateTime();
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

	public String saveFile(InputStream in, String filename, String mimeType) throws IOException {
		ULID.Value id = ulid.nextValue();

		FileMetadata meta = new FileMetadata();
		meta.filename = filename;
		meta.mimeType = mimeType;
		byte[] metaBytes = objectMapper.writeValueAsBytes(meta);

		Path filePath = getStoragePathForFile(id.toString());
		Files.createDirectories(filePath.getParent());
		try (var out = Files.newOutputStream(filePath)) {
			// Write metadata first.
			ByteBuffer metaBuffer = ByteBuffer.allocate(2 + metaBytes.length);
			metaBuffer.putShort((short) metaBytes.length);
			metaBuffer.put(metaBytes);
			out.write(metaBuffer.array());

			// Now write real data.
			byte[] buffer = new byte[8192];
			int readCount;
			while ((readCount = in.read(buffer)) > 0) {
				out.write(buffer, 0, readCount);
			}
		}
		return id.toString();
	}

	public FileMetadata readMetadata(InputStream in) throws IOException {
		ByteBuffer b = ByteBuffer.allocate(2);
		int bytesRead = in.read(b.array());
		if (bytesRead != 2) throw new IOException("Missing metadata length.");
		short length = b.getShort();
		b = ByteBuffer.allocate(length);
		bytesRead = in.read(b.array());
		if (bytesRead != length) throw new IOException("Metadata body does not equal length header.");
		return objectMapper.readValue(b.array(), FileMetadata.class);
	}

	/**
	 * Streams the contents of a stored file to a client via the Http response.
	 * @param rawId The file's unique identifier.
	 * @param response The response to stream the content to.
	 */
	public void streamFile(String rawId, HttpServletResponse response) {
		Path filePath = getStoragePathForFile(rawId);
		if (!Files.exists(filePath)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		try (var in = Files.newInputStream(filePath)) {
			FileMetadata metadata = readMetadata(in);
			response.setContentType(metadata.mimeType);
			response.setContentLengthLong(Files.size(filePath));
		}

		ULID.Value id = ULID.parseULID(rawId);
		Instant timestamp = Instant.ofEpochMilli(id.timestamp());
		LocalDateTime ldt = LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC);
		response.setContentType(file.getMimeType());
		response.setContentLengthLong(file.getSize());
		try {
			Path filePath = getStoragePathForFile(file);
			try (var in = Files.newInputStream(filePath)) {
				in.transferTo(response.getOutputStream());
			}
		} catch (IOException e) {
			log.error("Failed to write file to response.", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public FileMetadataResponse getFileMetadata(String id) {
		Path filePath = getStoragePathForFile(id);
		if (!Files.exists(filePath)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		try (var in = Files.newInputStream(filePath)) {
			FileMetadata metadata = readMetadata(in);
			return new FileMetadataResponse(
					metadata.filename,
					metadata.mimeType,
					Files.size(filePath),
					Files.getLastModifiedTime(filePath)
							.toInstant().atOffset(ZoneOffset.UTC)
							.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
					true
			);
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "IO error", e);
		}
	}
}
