package nl.andrewlalis.gymboardsearch.dto;

import org.apache.lucene.document.Document;

public record UserResponse(
		String id,
		String name
) {
	public UserResponse(Document doc) {
		this(
				doc.get("id"),
				doc.get("name")
		);
	}
}
