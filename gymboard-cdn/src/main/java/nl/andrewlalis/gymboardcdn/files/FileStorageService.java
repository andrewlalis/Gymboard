package nl.andrewlalis.gymboardcdn.files;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import nl.andrewlalis.gymboardcdn.files.util.ULID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * This service acts as a low-level driver for interacting with the storage
 * system. This includes reading and writing files and their metadata.
 * <p>
 *     Files are stored in a top-level directory, then in 3 sub-directories
 *     according to their creation date. So if a file is created on 2023-04-02,
 *     then it will be stored in BASE_DIR/2023/04/02/. All files are uniquely
 *     identified by a ULID; a monotonic, time-sorted id.
 * </p>
 * <p>
 *     Each file has a 1Kb (1024 bytes) metadata block baked into it when it's
 *     first saved. This metadata block stores a JSON-serialized set of metadata
 *     properties about the file.
 * </p>
 */
public class FileStorageService {
	private static final int HEADER_SIZE = 1024;

	private final ULID ulid;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String baseStorageDir;

	public FileStorageService(ULID ulid, String baseStorageDir) {
		this.ulid = ulid;
		this.baseStorageDir = baseStorageDir;
	}

	public String generateFileId() {
		return ulid.nextULID();
	}

	/**
	 * Saves a new file to the storage.
	 * @param in The input stream to the file contents.
	 * @param metadata The file's metadata.
	 * @param maxSize The maximum allowable filesize to download. If the given
	 *                input stream has more content than this size, an exception
	 *                is thrown.
	 * @return The file's id.
	 * @throws IOException If an error occurs.
	 */
	public String save(InputStream in, FileMetadata metadata, long maxSize) throws IOException {
		ULID.Value id = ulid.nextValue();
		return save(id, in, metadata, maxSize);
	}

	/**
	 * Saves a new file to the storage using a specific file id.
	 * @param id The file id to save to.
	 * @param in The input stream to the file contents.
	 * @param metadata The file's metadata.
	 * @param maxSize The maximum allowable filesize to download. If the given
	 *                input stream has more content than this size, an exception
	 *                is thrown.
	 * @return The file's id.
	 * @throws IOException If an error occurs.
	 */
	public String save(ULID.Value id, InputStream in, FileMetadata metadata, long maxSize) throws IOException {
		Path filePath = getStoragePathForFile(id.toString());
		Files.createDirectories(filePath.getParent());
		try (var out = Files.newOutputStream(filePath)) {
			writeMetadata(out, metadata);
			byte[] buffer = new byte[8192];
			int bytesRead;
			long totalBytesWritten = 0;
			while ((bytesRead = in.read(buffer)) > 0) {
				out.write(buffer, 0, bytesRead);
				totalBytesWritten += bytesRead;
				if (maxSize > 0 && totalBytesWritten > maxSize) {
					out.close();
					Files.delete(filePath);
					throw new IOException("File too large.");
				}
			}
		}
		return id.toString();
	}

	/**
	 * Gets metadata for a file identified by the given id.
	 * @param rawId The file's id.
	 * @return The metadata for the file, or null if no file is found.
	 * @throws IOException If an error occurs while reading metadata.
	 */
	public FullFileMetadata getMetadata(String rawId) throws IOException {
		Path filePath = getStoragePathForFile(rawId);
		if (Files.notExists(filePath)) return null;
		try (var in = Files.newInputStream(filePath)) {
			FileMetadata metadata = readMetadata(in);
			LocalDateTime date = dateFromULID(ULID.parseULID(rawId));
			return new FullFileMetadata(
					metadata.filename(),
					metadata.mimeType(),
					Files.size(filePath) - HEADER_SIZE,
					date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
			);
		}
	}

	/**
	 * Streams a stored file to an HTTP response. A NOT_FOUND response is sent
	 * if the file doesn't exist. Responses include a cache-control header to
	 * allow clients to cache the response for a long time, as stored files are
	 * considered to be immutable, unless rarely deleted.
	 * @param rawId The file's id.
	 * @param response The HTTP response to write to.
	 */
	public void streamToHttpResponse(String rawId, HttpServletResponse response) {
		Path filePath = getStoragePathForFile(rawId);
		if (Files.notExists(filePath)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		try (var in = Files.newInputStream(filePath)) {
			FileMetadata metadata = readMetadata(in);
			response.setContentType(metadata.mimeType());
			response.setContentLengthLong(Files.size(filePath) - HEADER_SIZE);
			response.addHeader("Cache-Control", "max-age=604800, immutable");
			var out = response.getOutputStream();
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) > 0) {
				out.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "IO error", e);
		}
	}

	/**
	 * Gets the path to the physical location of the file with the given id.
	 * Note that this method makes no guarantee as to whether the file exists.
	 * @param rawId The id of the file.
	 * @return The path to the location where the file is stored.
	 */
	public Path getStoragePathForFile(String rawId) {
		ULID.Value id = ULID.parseULID(rawId);
		LocalDateTime time = dateFromULID(id);
		Path dir = Path.of(baseStorageDir)
				.resolve(String.format("%04d", time.getYear()))
				.resolve(String.format("%02d", time.getMonthValue()))
				.resolve(String.format("%02d", time.getDayOfMonth()));
		return dir.resolve(rawId);
	}

	/**
	 * Deletes the file with a given id, if it exists.
	 * @param rawId The file's id.
	 * @throws IOException If an error occurs.
	 */
	public void delete(String rawId) throws IOException {
		Path filePath = getStoragePathForFile(rawId);
		Files.deleteIfExists(filePath);
	}

	private static LocalDateTime dateFromULID(ULID.Value value) {
		return Instant.ofEpochMilli(value.timestamp())
				.atOffset(ZoneOffset.UTC)
				.toLocalDateTime();
	}

	private FileMetadata readMetadata(InputStream in) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
		int readCount = in.read(buffer.array(), 0, HEADER_SIZE);
		if (readCount != HEADER_SIZE) throw new IOException("Invalid header.");
		short metadataBytesLength = buffer.getShort();
		byte[] metadataBytes = new byte[metadataBytesLength];
		buffer.get(metadataBytes);
		return objectMapper.readValue(metadataBytes, FileMetadata.class);
	}

	private void writeMetadata(OutputStream out, FileMetadata metadata) throws IOException {
		ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_SIZE);
		byte[] metadataBytes = objectMapper.writeValueAsBytes(metadata);
		if (metadataBytes.length > HEADER_SIZE - 2) {
			throw new IOException("Metadata is too large.");
		}
		headerBuffer.putShort((short) metadataBytes.length);
		headerBuffer.put(metadataBytes);
		out.write(headerBuffer.array());
	}
}
