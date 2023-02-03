package nl.andrewlalis.gymboardcdn.service;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import nl.andrewlalis.gymboardcdn.api.FileUploadResponse;
import nl.andrewlalis.gymboardcdn.model.StoredFileRepository;
import nl.andrewlalis.gymboardcdn.model.VideoProcessingTask;
import nl.andrewlalis.gymboardcdn.model.VideoProcessingTaskRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UploadServiceTest {
	/**
	 * Tests that when a processable video is uploaded, that it's saved to a
	 * temporary file, a new video processing task is created, and a new file
	 * identifier is generated for the file that may result.
	 * @throws IOException If an error occurs.
	 */
	@Test
	public void processableVideoUploadSuccess() throws IOException {
		StoredFileRepository storedFileRepository = Mockito.mock(StoredFileRepository.class);
		VideoProcessingTaskRepository videoTaskRepository = Mockito.mock(VideoProcessingTaskRepository.class);
		when(videoTaskRepository.save(any(VideoProcessingTask.class)))
				.then(returnsFirstArg());
		FileService fileService = Mockito.mock(FileService.class);
		when(fileService.saveToTempFile(any(InputStream.class), any(String.class)))
				.thenReturn(Path.of("test-cdn-files", "tmp", "bleh.mp4"));

		when(fileService.createNewFileIdentifier()).thenReturn("abc");

		UploadService uploadService = new UploadService(
				storedFileRepository,
				videoTaskRepository,
				fileService
		);
		HttpServletRequest mockRequest = mock(HttpServletRequest.class);
		when(mockRequest.getHeader("X-Filename")).thenReturn("testing.mp4");
		ServletInputStream mockRequestInputStream = mock(ServletInputStream.class);
		when(mockRequest.getInputStream()).thenReturn(mockRequestInputStream);
		var expectedResponse = new FileUploadResponse("abc");
		var response = uploadService.processableVideoUpload(mockRequest);
		assertEquals(expectedResponse, response);
		verify(fileService, times(1)).saveToTempFile(any(), any());
		verify(videoTaskRepository, times(1)).save(any());
		verify(fileService, times(1)).createNewFileIdentifier();
	}
}
