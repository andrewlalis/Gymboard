package nl.andrewlalis.gymboardcdn.files;

public record FileMetadata (
		String filename,
		String mimeType,
		boolean accessible
) {}
