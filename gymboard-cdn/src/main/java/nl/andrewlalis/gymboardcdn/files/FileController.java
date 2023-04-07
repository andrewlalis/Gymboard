package nl.andrewlalis.gymboardcdn.files;

import jakarta.servlet.http.HttpServletResponse;
import nl.andrewlalis.gymboardcdn.ServiceOnly;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

/**
 * Controller for general-purpose file access.
 */
@RestController
public class FileController {
	private final FileStorageService fileStorageService;

	public FileController(FileStorageService fileStorageService) {
		this.fileStorageService = fileStorageService;
	}

	@GetMapping(path = "/files/{id}")
	public void getFile(@PathVariable String id, HttpServletResponse response) {
		fileStorageService.streamToHttpResponse(id, response);
	}

	@GetMapping(path = "/files/{id}/metadata")
	public FullFileMetadata getFileMetadata(@PathVariable String id) {
		try {
			var data = fileStorageService.getMetadata(id);
			if (data == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
			return data;
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't read file metadata.", e);
		}
	}

	@DeleteMapping(path = "/files/{id}") @ServiceOnly
	public void deleteFile(@PathVariable String id) {
		try {
			fileStorageService.delete(id);
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file.", e);
		}
	}
}