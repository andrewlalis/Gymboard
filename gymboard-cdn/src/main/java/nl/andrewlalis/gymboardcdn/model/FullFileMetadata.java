package nl.andrewlalis.gymboardcdn.model;

public record FullFileMetadata(
		String filename,
		String mimeType,
		long size,
		String createdAt
) {}
