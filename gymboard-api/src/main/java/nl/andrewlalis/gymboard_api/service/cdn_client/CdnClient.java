package nl.andrewlalis.gymboard_api.service.cdn_client;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;

public class CdnClient {
	private final HttpClient httpClient;
	private final String baseUrl;
	private final ObjectMapper objectMapper;

	public final UploadsClient uploads;

	public CdnClient(String baseUrl) {
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(3))
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
		this.baseUrl = baseUrl;
		this.objectMapper = new ObjectMapper();
		this.uploads = new UploadsClient(this);
	}

	public <T> T get(String urlPath, Class<T> responseType) throws IOException, InterruptedException {
		HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + urlPath))
				.GET()
				.build();
		HttpResponse<String> response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
		return objectMapper.readValue(response.body(), responseType);
	}

	public <T> T postFile(String urlPath, Path filePath, String contentType, Class<T> responseType) throws IOException, InterruptedException {
		HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + urlPath))
				.POST(HttpRequest.BodyPublishers.ofFile(filePath))
				.header("Content-Type", contentType)
				.header("X-Gymboard-Filename", filePath.getFileName().toString())
				.build();
		HttpResponse<String> response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
		return objectMapper.readValue(response.body(), responseType);
	}
}
