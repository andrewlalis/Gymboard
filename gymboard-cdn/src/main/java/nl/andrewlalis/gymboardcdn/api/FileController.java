package nl.andrewlalis.gymboardcdn.api;

import jakarta.servlet.http.HttpServletResponse;
import nl.andrewlalis.gymboardcdn.service.FileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileController {
	private final FileService fileService;

	public FileController(FileService fileService) {
		this.fileService = fileService;
	}

	@GetMapping(path = "/files/{id}")
	public void getFile(@PathVariable String id, HttpServletResponse response) {
		fileService.streamFile(id, response);
	}

	@GetMapping(path = "/files/{id}/metadata")
	public FileMetadataResponse getFileMetadata(@PathVariable String id) {
		return fileService.getFileMetadata(id);
	}
}
