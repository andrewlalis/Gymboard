package nl.andrewlalis.gymboard_api.domains.api.service.cdn_client;

import java.nio.file.Path;

public record UploadsClient(CdnClient client) {
	public record FileUploadResponse(String id) {}
	public record VideoProcessingTaskStatusResponse(String status) {}

	public record FileMetadataResponse(
			String filename,
			String mimeType,
			long size,
			String uploadedAt,
			boolean availableForDownload
	) {}

	public FileUploadResponse uploadVideo(Path filePath, String contentType) throws Exception {
		return client.postFile("/uploads/video", filePath, contentType, FileUploadResponse.class);
	}

	public VideoProcessingTaskStatusResponse getVideoProcessingStatus(String id) throws Exception {
		return client.get("/uploads/video/" + id + "/status", VideoProcessingTaskStatusResponse.class);
	}

	public FileMetadataResponse getFileMetadata(String id) throws Exception {
		return client.get("/files/" + id + "/metadata", FileMetadataResponse.class);
	}
}
