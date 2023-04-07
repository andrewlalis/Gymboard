package nl.andrewlalis.gymboardcdn.uploads.model;

public record VideoProcessingTaskStatusUpdate(
		long taskId,
		String status,
		String videoFileId,
		String thumbnailFileId
) {
	public VideoProcessingTaskStatusUpdate(VideoProcessingTask task) {
		this(task.getId(), task.getStatus().name(), task.getVideoFileId(), task.getThumbnailFileId());
	}
}
