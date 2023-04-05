package nl.andrewlalis.gymboard_api.domains.api.service.cdn_client;

public record FilesClient(CdnClient client) {
	public record FileMetadataResponse(
			String filename,
			String mimeType,
			long size,
			String createdAt
	) {}

	public FileMetadataResponse getFileMetadata(String id) throws Exception {
		return client.get("/files/" + id + "/metadata", FileMetadataResponse.class);
	}

	public void deleteFile(String id) throws Exception {
		client.delete("/files/" + id);
	}
}
