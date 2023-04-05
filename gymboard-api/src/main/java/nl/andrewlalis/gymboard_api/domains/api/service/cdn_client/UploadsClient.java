package nl.andrewlalis.gymboard_api.domains.api.service.cdn_client;

import java.nio.file.Path;

public record UploadsClient(CdnClient client) {
	public record FileUploadResponse(long taskId) {}
	public record VideoProcessingTaskStatusResponse(
			String status,
			String videoFileId,
			String thumbnailFileId
	) {}

	public record FileMetadataResponse(
			String filename,
			String mimeType,
			long size,
			String uploadedAt,
			boolean availableForDownload
	) {}

	public long uploadVideo(Path filePath, String contentType) throws Exception {
		return client.postFile("/uploads/video", filePath, contentType, FileUploadResponse.class).taskId();
	}

	public VideoProcessingTaskStatusResponse getVideoProcessingTaskStatus(long id) throws Exception {
		return client.get("/uploads/video/" + id + "/status", VideoProcessingTaskStatusResponse.class);
	}

	public FileMetadataResponse getFileMetadata(String id) throws Exception {
		return client.get("/files/" + id + "/metadata", FileMetadataResponse.class);
	}

	public void startTask(long taskId) throws Exception {
		client.post("/uploads/video/" + taskId + "/start");
	}
}
