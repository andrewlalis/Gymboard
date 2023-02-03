package nl.andrewlalis.gymboardcdn.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stored_file")
public class StoredFile {
	/**
	 * ULID-based unique file identifier.
	 */
	@Id
	@Column(nullable = false, updatable = false, length = 26)
	private String id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	/**
	 * The timestamp at which the file was originally uploaded.
	 */
	@Column(nullable = false, updatable = false)
	private LocalDateTime uploadedAt;

	/**
	 * The original filename.
	 */
	@Column(nullable = false, updatable = false)
	private String name;

	/**
	 * The type of the file.
	 */
	@Column(updatable = false)
	private String mimeType;

	/**
	 * The file's size on the disk.
	 */
	@Column(nullable = false, updatable = false)
	private long size;

	public StoredFile() {}

	public StoredFile(String id, String name, String mimeType, long size, LocalDateTime uploadedAt) {
		this.id = id;
		this.name = name;
		this.mimeType = mimeType;
		this.size = size;
		this.uploadedAt = uploadedAt;
	}

	public String getId() {
		return id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public String getName() {
		return name;
	}

	public String getMimeType() {
		return mimeType;
	}

	public long getSize() {
		return size;
	}

	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}
}
