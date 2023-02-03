package nl.andrewlalis.gymboardcdn.service;

import nl.andrewlalis.gymboardcdn.api.FileUploadResponse;
import nl.andrewlalis.gymboardcdn.model.StoredFileRepository;
import nl.andrewlalis.gymboardcdn.model.VideoProcessingTask;
import nl.andrewlalis.gymboardcdn.model.VideoProcessingTaskRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UploadServiceTest {
	@Test
	public void processableVideoUploadSuccess() throws IOException {
		StoredFileRepository storedFileRepository = Mockito.mock(StoredFileRepository.class);
		VideoProcessingTaskRepository videoTaskRepository = Mockito.mock(VideoProcessingTaskRepository.class);
		when(videoTaskRepository.save(any(VideoProcessingTask.class)))
				.then(returnsFirstArg());
		FileService fileService = Mockito.mock(FileService.class);
		MultipartFile multipartFile = new MockMultipartFile(
				"file",
				"testing.mp4",
				"video/mp4",
				new byte[]{1, 2, 3}
		);
		when(fileService.saveToTempFile(any(MultipartFile.class)))
				.thenReturn(Path.of("test-cdn-files", "tmp", "bleh.mp4"));

		when(fileService.createNewFileIdentifier()).thenReturn("abc");

		UploadService uploadService = new UploadService(
				storedFileRepository,
				videoTaskRepository,
				fileService
		);
		var expectedResponse = new FileUploadResponse("abc");
		var response = uploadService.processableVideoUpload(multipartFile);
		assertEquals(expectedResponse, response);
		verify(fileService, times(1)).saveToTempFile(multipartFile);
		verify(videoTaskRepository, times(1)).save(any());
		verify(fileService, times(1)).createNewFileIdentifier();
	}
}
