package nl.andrewlalis.gymboardsearch.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class QueryIndexSearcher<R> {
	private static final Logger log = LoggerFactory.getLogger(QueryIndexSearcher.class);

	private final Function<Document, R> mapper;
	private final Function<String, Optional<Query>> querySupplier;
	private final int maxResults;
	private final Path indexDir;

	public QueryIndexSearcher(Function<Document, R> mapper, Function<String, Optional<Query>> querySupplier, int maxResults, Path indexDir) {
		this.mapper = mapper;
		this.querySupplier = querySupplier;
		this.maxResults = maxResults;
		this.indexDir = indexDir;
	}

	public List<R> search(String rawQuery) {
		Optional<Query> optionalQuery = querySupplier.apply(rawQuery);
		if (optionalQuery.isEmpty()) return Collections.emptyList();
		Query query = optionalQuery.get();
		try (
				var reader = DirectoryReader.open(FSDirectory.open(indexDir))
		) {
			IndexSearcher searcher = new IndexSearcher(reader);
			List<R> results = new ArrayList<>(maxResults);
			TopDocs topDocs = searcher.search(query, maxResults, Sort.RELEVANCE, false);
			for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
				Document doc = searcher.doc(scoreDoc.doc);
				results.add(mapper.apply(doc));
			}
			return results;
		} catch (IOException e) {
			log.error("Could not search index.", e);
			return Collections.emptyList();
		}
	}
}
