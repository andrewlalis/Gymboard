package nl.andrewlalis.gymboard_api.service;

import nl.andrewlalis.gymboard_api.controller.dto.RawGymId;
import nl.andrewlalis.gymboard_api.controller.dto.UploadedFileResponse;
import nl.andrewlalis.gymboard_api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.dao.StoredFileRepository;
import nl.andrewlalis.gymboard_api.model.Gym;
import nl.andrewlalis.gymboard_api.model.StoredFile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Service for handling large file uploads.
 * TODO: Use this instead of simple multipart form data.
 */
@Service
public class UploadService {
	private final StoredFileRepository fileRepository;
	private final GymRepository gymRepository;

	public UploadService(StoredFileRepository fileRepository, GymRepository gymRepository) {
		this.fileRepository = fileRepository;
		this.gymRepository = gymRepository;
	}

	@Transactional
	public UploadedFileResponse handleUpload(RawGymId gymId, MultipartFile multipartFile) throws IOException {
		Gym gym = gymRepository.findByRawId(gymId.gymName(), gymId.cityCode(), gymId.countryCode())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		// TODO: Check that user is allowed to upload.
		// TODO: Robust file type check.
		if (!"video/mp4".equalsIgnoreCase(multipartFile.getContentType())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid content type.");
		}
		Path tempDir = Files.createTempDirectory("gymboard-file-upload");
		Path tempFile = tempDir.resolve("video-file");
		multipartFile.transferTo(tempFile);
		Process ffmpegProcess = new ProcessBuilder()
				.command("ffmpeg", "-i", "video-file", "-vf", "scale=640x480:flags=lanczos", "-vcodec", "libx264", "-crf", "28", "output.mp4")
				.inheritIO()
				.directory(tempDir.toFile())
				.start();
		try {
			int result = ffmpegProcess.waitFor();
			if (result != 0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ffmpeg exited with code " + result);
		} catch (InterruptedException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ffmpeg process interrupted", e);
		}
		Path compressedFile = tempDir.resolve("output.mp4");
		StoredFile file = fileRepository.save(new StoredFile(
				"compressed.mp4",
				"video/mp4",
				Files.size(compressedFile),
				Files.readAllBytes(compressedFile)
		));
		FileSystemUtils.deleteRecursively(tempDir);
		return new UploadedFileResponse(file.getId());
	}
}
