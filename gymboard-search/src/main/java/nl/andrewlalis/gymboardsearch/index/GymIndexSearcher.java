package nl.andrewlalis.gymboardsearch.index;

import nl.andrewlalis.gymboardsearch.dto.GymResponse;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Searcher that uses a Lucene {@link IndexSearcher} to search for gyms using
 * a query that's built from a weighted list of wildcard search terms.
 * <ol>
 *     <li>If the query is blank, return an empty list.</li>
 *     <li>Split the query into words, append the wildcard '*' to each word.</li>
 *     <li>For each word, add a boosted wildcard query for each weighted field.</li>
 * </ol>
 */
@Service
public class GymIndexSearcher {
	public List<GymResponse> searchGyms(String rawQuery) {
		if (rawQuery == null || rawQuery.isBlank()) return Collections.emptyList();
		String[] terms = rawQuery.split("\\s+");
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		Map<String, Float> fieldWeights = new HashMap<>();
		fieldWeights.put("short_name", 3f);
		fieldWeights.put("display_name", 3f);
		fieldWeights.put("city_short_name", 1f);
		fieldWeights.put("city_name", 1f);
		fieldWeights.put("country_code", 0.25f);
		fieldWeights.put("country_name", 0.5f);
		fieldWeights.put("street_address", 0.1f);
		for (String term : terms) {
			String searchTerm = term.strip().toLowerCase() + "*";
			for (var entry : fieldWeights.entrySet()) {
				Query baseQuery = new WildcardQuery(new Term(entry.getKey(), searchTerm));
				queryBuilder.add(new BoostQuery(baseQuery, entry.getValue()), BooleanClause.Occur.SHOULD);
			}
		}
		BooleanQuery query = queryBuilder.build();
		Path gymIndexDir = Path.of("gym-index");
		try (
				var reader = DirectoryReader.open(FSDirectory.open(gymIndexDir))
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
