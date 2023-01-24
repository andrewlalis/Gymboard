package nl.andrewlalis.gymboardsearch;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Service
public class GymIndexGenerator {
	private static final Logger log = LoggerFactory.getLogger(GymIndexGenerator.class);

	void generateIndex() throws Exception {
		log.info("Starting Gym index generation.");
		Path gymIndexDir = Path.of("gym-index");
		FileSystemUtils.deleteRecursively(gymIndexDir);
		Files.createDirectory(gymIndexDir);
		long count = 0;
		try (
				Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/gymboard-api-dev", "gymboard-api-dev", "testpass");
				PreparedStatement stmt = conn.prepareStatement(DbUtils.loadClasspathString("/sql/select-gyms.sql"));
				ResultSet resultSet = stmt.executeQuery();

				Analyzer analyzer = new StandardAnalyzer();
				Directory indexDir = FSDirectory.open(gymIndexDir);
				IndexWriter indexWriter = new IndexWriter(indexDir, new IndexWriterConfig(analyzer))
		) {
			while (resultSet.next()) {
				String shortName = resultSet.getString("short_name");
				String displayName = resultSet.getString("display_name");
				String cityShortName = resultSet.getString("city_short_name");
				String cityName = resultSet.getString("city_name");
				String countryCode = resultSet.getString("country_code");
				String countryName = resultSet.getString("country_name");
				String streetAddress = resultSet.getString("street_address");
				BigDecimal latitude = resultSet.getBigDecimal("latitude");
				BigDecimal longitude = resultSet.getBigDecimal("longitude");

				Document doc = new Document();
				doc.add(new StringField("short_name", shortName, Field.Store.YES));
				doc.add(new StringField("display_name", displayName, Field.Store.YES));
				doc.add(new StringField("city_short_name", cityShortName, Field.Store.YES));
				doc.add(new StringField("city_name", cityName, Field.Store.YES));
				doc.add(new StringField("country_code", countryCode, Field.Store.YES));
				doc.add(new StringField("country_name", countryName, Field.Store.YES));
				doc.add(new StringField("street_address", streetAddress, Field.Store.YES));
				doc.add(new DoublePoint("latitude_point", latitude.doubleValue()));
				doc.add(new StoredField("latitude", latitude.doubleValue()));
				doc.add(new DoublePoint("longitude_point", longitude.doubleValue()));
				doc.add(new StoredField("longitude", longitude.doubleValue()));
				indexWriter.addDocument(doc);
				count++;
			}
		}
		log.info("Gym index generation complete. {} gyms indexed.", count);
	}
}
