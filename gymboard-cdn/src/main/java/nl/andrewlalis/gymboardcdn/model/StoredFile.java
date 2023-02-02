package nl.andrewlalis.gymboardcdn.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stored_file")
public class StoredFile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

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
	 * The internal id that's used to find this file wherever it's placed on
	 * our service's storage. It is universally unique.
	 */
	@Column(nullable = false, updatable = false, unique = true)
	private String identifier;

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

	public StoredFile(String name, String identifier, String mimeType, long size, LocalDateTime uploadedAt) {
		this.name = name;
		this.identifier = identifier;
		this.mimeType = mimeType;
		this.size = size;
		this.uploadedAt = uploadedAt;
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public String getName() {
		return name;
	}

	public String getIdentifier() {
		return identifier;
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
