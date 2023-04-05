package nl.andrewlalis.gymboard_api.domains.api.dto;

public record VideoProcessingCompletePayload(
		long taskId,
		String status,
		String videoFileId,
		String thumbnailFileId
) {}
