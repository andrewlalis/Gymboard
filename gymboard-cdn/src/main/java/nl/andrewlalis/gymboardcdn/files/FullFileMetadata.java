package nl.andrewlalis.gymboardcdn.files;

public record FullFileMetadata(
		String filename,
		String mimeType,
		long size,
		String createdAt
) {}
