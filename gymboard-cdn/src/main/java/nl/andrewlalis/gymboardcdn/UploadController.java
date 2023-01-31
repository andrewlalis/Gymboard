package nl.andrewlalis.gymboardcdn;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {
	@PostMapping(path = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public void uploadContent(@RequestParam MultipartFile file) {

	}
}
