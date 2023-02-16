package nl.andrewlalis.gymboardsearch.dto;

import org.apache.lucene.document.Document;

public record UserResponse(
		String id,
		String name,
		long submissionCount,
		boolean accountPrivate,
		String locale
) {
	public UserResponse(Document doc) {
		this(
				doc.get("id"),
				doc.get("name"),
				doc.getField("submission_count").numericValue().longValue(),
				Boolean.parseBoolean(doc.get("account_private")),
				doc.get("locale")
		);
	}
}
