package nl.andrewlalis.gymboard_api.service;

import nl.andrewlalis.gymboard_api.controller.dto.RawGymId;
import nl.andrewlalis.gymboard_api.controller.dto.UploadedFileResponse;
import nl.andrewlalis.gymboard_api.dao.GymRepository;
import nl.andrewlalis.gymboard_api.dao.exercise.ExerciseSubmissionTempFileRepository;
import nl.andrewlalis.gymboard_api.model.Gym;
import nl.andrewlalis.gymboard_api.model.exercise.ExerciseSubmissionTempFile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service for handling large file uploads.
 */
@Service
public class UploadService {
	private static final String[] ALLOWED_VIDEO_TYPES = {
			"video/mp4"
	};

	private final ExerciseSubmissionTempFileRepository tempFileRepository;
	private final GymRepository gymRepository;

	public UploadService(ExerciseSubmissionTempFileRepository tempFileRepository, GymRepository gymRepository) {
		this.tempFileRepository = tempFileRepository;
		this.gymRepository = gymRepository;
	}

	/**
	 * Handles the upload of an exercise submission's video file by saving the
	 * file to a temporary location, and recording that location in the
	 * database for when the exercise submission is completed. We'll only do
	 * the computationally expensive video processing if a user successfully
	 * submits their submission; otherwise, the raw video is discarded after a
	 * while.
	 * @param gymId The gym's id.
	 * @param multipartFile The uploaded file.
	 * @return A response containing the uploaded file's id, to be included in
	 * the user's submission.
	 */
	@Transactional
	public UploadedFileResponse handleSubmissionUpload(RawGymId gymId, MultipartFile multipartFile) {
		Gym gym = gymRepository.findByRawId(gymId.gymName(), gymId.cityCode(), gymId.countryCode())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		// TODO: Check that user is allowed to upload.
		boolean fileTypeAcceptable = false;
		for (String allowedType : ALLOWED_VIDEO_TYPES) {
			if (allowedType.equalsIgnoreCase(multipartFile.getContentType())) {
				fileTypeAcceptable = true;
				break;
			}
		}
		if (!fileTypeAcceptable) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid content type.");
		}
		try {
			Path tempFileDir = Path.of("exercise_submission_temp_files");
			if (!Files.exists(tempFileDir)) {
				Files.createDirectory(tempFileDir);
			}
			Path tempFilePath = Files.createTempFile(tempFileDir, null, null);
			multipartFile.transferTo(tempFilePath);
			ExerciseSubmissionTempFile tempFileEntity = tempFileRepository.save(new ExerciseSubmissionTempFile(tempFilePath.toString()));
			return new UploadedFileResponse(tempFileEntity.getId());
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed.", e);
		}

//		Path tempDir = Files.createTempDirectory("gymboard-file-upload");
//		Path tempFile = tempDir.resolve("video-file");
//		multipartFile.transferTo(tempFile);
//		Process ffmpegProcess = new ProcessBuilder()
//				.command("ffmpeg", "-i", "video-file", "-vf", "scale=640x480:flags=lanczos", "-vcodec", "libx264", "-crf", "28", "output.mp4")
//				.inheritIO()
//				.directory(tempDir.toFile())
//				.start();
//		try {
//			int result = ffmpegProcess.waitFor();
//			if (result != 0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ffmpeg exited with code " + result);
//		} catch (InterruptedException e) {
//			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ffmpeg process interrupted", e);
//		}
//		Path compressedFile = tempDir.resolve("output.mp4");
//		StoredFile file = fileRepository.save(new StoredFile(
//				"compressed.mp4",
//				"video/mp4",
//				Files.size(compressedFile),
//				Files.readAllBytes(compressedFile)
//		));
//		FileSystemUtils.deleteRecursively(tempDir);
//		return new UploadedFileResponse(file.getId());
	}
}
