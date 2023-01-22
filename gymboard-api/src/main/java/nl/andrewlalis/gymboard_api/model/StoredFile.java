package nl.andrewlalis.gymboard_api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Base class for file storage. Files (mostly gym videos) are stored in the
 * database as blobs, after they've been pre-processed with compression and/or
 * resizing.
 */
@Entity
@Table(name = "stored_file")
public class StoredFile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@Column(nullable = false, updatable = false)
	private String filename;

	@Column(nullable = false, updatable = false)
	private String mimeType;

	@Column(nullable = false, updatable = false)
	private long size;

	@Lob
	@Column(nullable = false, updatable = false)
	private byte[] content;

	public StoredFile() {}

	public StoredFile(String filename, String mimeType, long size, byte[] content) {
		this.filename = filename;
		this.mimeType = mimeType;
		this.size = size;
		this.content = content;
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public String getFilename() {
		return filename;
	}

	public String getMimeType() {
		return mimeType;
	}

	public long getSize() {
		return size;
	}

	public byte[] getContent() {
		return content;
	}
}
