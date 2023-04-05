package nl.andrewlalis.gymboardcdn.uploads.api;

public record VideoProcessingTaskStatusResponse(
		String status,
		String videoFileId,
		String thumbnailFileId
) {}
