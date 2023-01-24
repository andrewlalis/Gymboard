package nl.andrewlalis.gymboardsearch;

import nl.andrewlalis.gymboardsearch.dto.GymResponse;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GymIndexSearcher {
	public List<GymResponse> searchGyms(String rawQuery) {
		if (rawQuery == null || rawQuery.isBlank()) return Collections.emptyList();
		String[] terms = rawQuery.split("\\s+");
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		String[] searchableFields = {
			"short_name",
			"display_name",
			"city_short_name",
			"city_name",
			"country_code",
			"country_name",
			"street_address"
		};
		for (String term : terms) {
			for (String field : searchableFields) {
				queryBuilder.add(new TermQuery(new Term(field, term)), BooleanClause.Occur.SHOULD);
			}
		}
		BooleanQuery query = queryBuilder.build();
		Path gymIndexDir = Path.of("gym-index");
		try (
				var reader = DirectoryReader.open(FSDirectory.open(gymIndexDir));
		) {
			IndexSearcher searcher = new IndexSearcher(reader);
			List<GymResponse> results = new ArrayList<>(10);
			TopDocs topDocs = searcher.search(query, 10, Sort.RELEVANCE, false);
			for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
				Document doc = searcher.doc(scoreDoc.doc);
				results.add(new GymResponse(doc));
			}
			return results;
		} catch (IOException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
}
