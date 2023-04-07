package nl.andrewlalis.gymboard_api.domains.api.service.cdn_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.andrewlalis.gymboard_api.config.ServiceAccessInterceptor;

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
	private final String cdnSecret;
	private final ObjectMapper objectMapper;

	public final UploadsClient uploads;
	public final FilesClient files;

	public CdnClient(String baseUrl, String cdnSecret) {
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(3))
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
		this.baseUrl = baseUrl;
		this.cdnSecret = cdnSecret;
		this.objectMapper = new ObjectMapper();
		this.uploads = new UploadsClient(this);
		this.files = new FilesClient(this);
	}

	public <T> T get(String urlPath, Class<T> responseType) throws IOException, InterruptedException {
		HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + urlPath))
				.GET()
				.header(ServiceAccessInterceptor.HEADER_NAME, cdnSecret)
				.build();
		HttpResponse<String> response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 200) {
			return objectMapper.readValue(response.body(), responseType);
		} else if (response.statusCode() == 404) {
			return null;
		} else {
			throw new IOException("Request failed with code " + response.statusCode());
		}
	}

	public <T> T postFile(String urlPath, Path filePath, String contentType, Class<T> responseType) throws IOException, InterruptedException {
		HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + urlPath))
				.POST(HttpRequest.BodyPublishers.ofFile(filePath))
				.header("Content-Type", contentType)
				.header("X-Gymboard-Filename", filePath.getFileName().toString())
				.header(ServiceAccessInterceptor.HEADER_NAME, cdnSecret)
				.build();
		HttpResponse<String> response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
		return objectMapper.readValue(response.body(), responseType);
	}

	public void post(String urlPath) throws IOException, InterruptedException {
		HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + urlPath))
				.POST(HttpRequest.BodyPublishers.noBody())
				.header(ServiceAccessInterceptor.HEADER_NAME, cdnSecret)
				.build();
		HttpResponse<Void> response = httpClient.send(req, HttpResponse.BodyHandlers.discarding());
		if (response.statusCode() != 200) {
			throw new IOException("Request failed with code " + response.statusCode());
		}
	}

	public void delete(String urlPath) throws IOException, InterruptedException {
		HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + urlPath))
				.DELETE()
				.header(ServiceAccessInterceptor.HEADER_NAME, cdnSecret)
				.build();
		HttpResponse<Void> response = httpClient.send(req, HttpResponse.BodyHandlers.discarding());
		if (response.statusCode() >= 400) {
			throw new IOException("Request failed with code " + response.statusCode());
		}
	}
}
