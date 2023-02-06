package nl.andrewlalis.gymboardsearch.index;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class WeightedWildcardQueryBuilder {
	private final BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
	private final Map<String, Float> fieldWeights = new HashMap<>();

	public WeightedWildcardQueryBuilder withField(String fieldName, float weight) {
		fieldWeights.put(fieldName, weight);
		return this;
	}

	public WeightedWildcardQueryBuilder customize(Consumer<BooleanQuery.Builder> customizer) {
		customizer.accept(queryBuilder);
		return this;
	}

	public Optional<Query> build(String rawSearchQuery) {
		if (rawSearchQuery == null || rawSearchQuery.isBlank()) return Optional.empty();
		String[] terms = rawSearchQuery.toLowerCase().split("\\s+");
		for (String term : terms) {
			String searchTerm = term + "*";
			for (var entry : fieldWeights.entrySet()) {
				String fieldName = entry.getKey();
				float weight = entry.getValue();

				Query baseQuery = new WildcardQuery(new Term(fieldName, searchTerm));
				queryBuilder.add(new BoostQuery(baseQuery, weight), BooleanClause.Occur.SHOULD);
			}
		}
		return Optional.of(queryBuilder.build());
	}
}
