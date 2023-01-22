package nl.andrewlalis.gymboard_api.controller;

import jakarta.servlet.http.HttpServletResponse;
import nl.andrewlalis.gymboard_api.dao.StoredFileRepository;
import nl.andrewlalis.gymboard_api.model.StoredFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
public class FileController {
	private final StoredFileRepository fileRepository;

	public FileController(StoredFileRepository fileRepository) {
		this.fileRepository = fileRepository;
	}

	@GetMapping(path = "/files/{fileId}")
	public void getFile(@PathVariable long fileId, HttpServletResponse response) throws IOException {
		StoredFile file = fileRepository.findById(fileId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		response.setContentType(file.getMimeType());
		response.setContentLengthLong(file.getSize());
		response.getOutputStream().write(file.getContent());
	}
}
