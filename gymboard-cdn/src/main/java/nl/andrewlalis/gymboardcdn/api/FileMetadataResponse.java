package nl.andrewlalis.gymboardcdn.api;

public record FileMetadataResponse(
		String filename,
		String mimeType,
		long size,
		String uploadedAt,
		boolean availableForDownload
) {}
