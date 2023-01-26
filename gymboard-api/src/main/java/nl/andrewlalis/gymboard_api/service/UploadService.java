package nl.andrewlalis.gymboard_api.service;

import nl.andrewlalis.gymboard_api.controller.dto.CompoundGymId;
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
	public static final Path SUBMISSION_TEMP_FILE_DIR = Path.of("exercise_submission_temp_files");
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
	public UploadedFileResponse handleSubmissionUpload(CompoundGymId gymId, MultipartFile multipartFile) {
		Gym gym = gymRepository.findByCompoundId(gymId)
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
			if (!Files.exists(SUBMISSION_TEMP_FILE_DIR)) {
				Files.createDirectory(SUBMISSION_TEMP_FILE_DIR);
			}
			Path tempFilePath = Files.createTempFile(SUBMISSION_TEMP_FILE_DIR, null, null);
			multipartFile.transferTo(tempFilePath);
			ExerciseSubmissionTempFile tempFileEntity = tempFileRepository.save(new ExerciseSubmissionTempFile(tempFilePath.toString()));
			return new UploadedFileResponse(tempFileEntity.getId());
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed.", e);
		}
	}
}
