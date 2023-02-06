package nl.andrewlalis.gymboardsearch.index;

import org.apache.lucene.document.Document;

import java.sql.ResultSet;

@FunctionalInterface
public interface JdbcResultDocumentMapper {
	Document map(ResultSet rs) throws Exception;
}
