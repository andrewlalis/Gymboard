package nl.andrewlalis.gymboardsearch.index;

import nl.andrewlalis.gymboardsearch.dto.GymResponse;
import nl.andrewlalis.gymboardsearch.dto.UserResponse;
import org.apache.lucene.document.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.DriverManager;

/**
 * Component that defines beans for the various indexes that this service
 * supports. Beans are primarily constructed using the reusable "jdbc"
 * components so that all index and search configuration is defined here.
 */
@Component
public class IndexComponents {
	@Bean
	public JdbcConnectionSupplier jdbcConnectionSupplier() {
		return () -> DriverManager.getConnection("jdbc:postgresql://localhost:5432/gymboard-api-dev", "gymboard-api-dev", "testpass");
	}

	@Bean
	public JdbcIndexGenerator userIndexGenerator(JdbcConnectionSupplier connectionSupplier) throws IOException {
		return new JdbcIndexGenerator(
				Path.of("user-index"),
				connectionSupplier,
				PlainQueryResultSetSupplier.fromResourceFile("/sql/select-users.sql"),
				rs -> {
					var doc = new Document();
					doc.add(new StoredField("id", rs.getString("id")));
					doc.add(new TextField("name", rs.getString("name"), Field.Store.YES));
					return doc;
				}
		);
	}

	@Bean
	public QueryIndexSearcher<UserResponse> userIndexSearcher() {
		return new QueryIndexSearcher<>(
				UserResponse::new,
				s -> new WeightedWildcardQueryBuilder()
						.withField("name", 1f)
						.build(s),
				10,
				Path.of("user-index")
		);
	}

	@Bean
	public JdbcIndexGenerator gymIndexGenerator(JdbcConnectionSupplier connectionSupplier) throws IOException {
		return new JdbcIndexGenerator(
				Path.of("gym-index"),
				connectionSupplier,
				PlainQueryResultSetSupplier.fromResourceFile("/sql/select-gyms.sql"),
				rs -> {
					String shortName = rs.getString("short_name");
					String displayName = rs.getString("display_name");
					String cityShortName = rs.getString("city_short_name");
					String cityName = rs.getString("city_name");
					String countryCode = rs.getString("country_code");
					String countryName = rs.getString("country_name");
					String streetAddress = rs.getString("street_address");
					BigDecimal latitude = rs.getBigDecimal("latitude");
					BigDecimal longitude = rs.getBigDecimal("longitude");
					long submissionCount = rs.getLong("submission_count");
					String gymCompoundId = String.format("%s_%s_%s", countryCode, cityShortName, shortName);

					Document doc = new Document();
					doc.add(new StoredField("compound_id", gymCompoundId));
					doc.add(new TextField("short_name", shortName, Field.Store.YES));
					doc.add(new TextField("display_name", displayName, Field.Store.YES));
					doc.add(new TextField("city_short_name", cityShortName, Field.Store.YES));
					doc.add(new TextField("city_name", cityName, Field.Store.YES));
					doc.add(new TextField("country_code", countryCode, Field.Store.YES));
					doc.add(new TextField("country_name", countryName, Field.Store.YES));
					doc.add(new TextField("street_address", streetAddress, Field.Store.YES));
					doc.add(new DoublePoint("latitude_point", latitude.doubleValue()));
					doc.add(new StoredField("latitude", latitude.doubleValue()));
					doc.add(new DoublePoint("longitude_point", longitude.doubleValue()));
					doc.add(new StoredField("longitude", longitude.doubleValue()));
					doc.add(new LongPoint("submission_count_point", submissionCount));
					doc.add(new StoredField("submission_count", submissionCount));
					return doc;
				}
		);
	}

	@Bean
	public QueryIndexSearcher<GymResponse> gymIndexSearcher() {
		return new QueryIndexSearcher<>(
				GymResponse::new,
				s -> new WeightedWildcardQueryBuilder()
						.withField("short_name", 3f)
						.withField("display_name", 3f)
						.withField("city_short_name", 1f)
						.withField("city_name", 1f)
						.withField("country_code", 0.25f)
						.withField("country_name", 0.5f)
						.withField("street_address", 0.1f)
						.build(s),
				10,
				Path.of("gym-index")
		);
	}
}
