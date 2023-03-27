package nl.andrewlalis.gymboardsearch.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;

public class JdbcIndexGenerator {
	private static final Logger log = LoggerFactory.getLogger(JdbcIndexGenerator.class);

	private final Path indexDir;
	private final JdbcConnectionSupplier connectionSupplier;
	private final JdbcResultSetSupplier resultSetSupplier;
	private final JdbcResultDocumentMapper resultMapper;

	public JdbcIndexGenerator(Path indexDir, JdbcConnectionSupplier connectionSupplier, JdbcResultSetSupplier resultSetSupplier, JdbcResultDocumentMapper resultMapper) {
		this.indexDir = indexDir;
		this.connectionSupplier = connectionSupplier;
		this.resultSetSupplier = resultSetSupplier;
		this.resultMapper = resultMapper;
	}

	public void generate() {
		log.info("Generating index at {}.", indexDir);
		long start = System.currentTimeMillis();
		try (
				Connection conn = connectionSupplier.getConnection();
				ResultSet rs = resultSetSupplier.supply(conn);

				Analyzer analyzer = new StandardAnalyzer();
				Directory luceneDir = FSDirectory.open(indexDir)
		) {
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			IndexWriter indexWriter = new IndexWriter(luceneDir, config);

			long count = 0;
			while (rs.next()) {
				try {
					indexWriter.addDocument(resultMapper.map(rs));
					count++;
				} catch (Exception e) {
					log.error("Failed to add document.", e);
				}
			}
			long dur = System.currentTimeMillis() - start;
			log.info("Indexed {} entities for {} index in {} ms.", count, indexDir, dur);
			indexWriter.close();
		} catch (Exception e) {
			log.error("Failed to prepare indexing components.", e);
		}
	}
}
