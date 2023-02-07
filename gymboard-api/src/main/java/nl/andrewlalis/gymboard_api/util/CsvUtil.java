package nl.andrewlalis.gymboard_api.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public final class CsvUtil {
	private CsvUtil() {}

	public static void load(Path csvFile, ThrowableConsumer<CSVRecord> recordConsumer) throws IOException {
		var reader = new FileReader(csvFile.toFile());
		CSVFormat format = CSVFormat.DEFAULT.builder()
				.setHeader()
				.setSkipHeaderRecord(true)
				.build();
		for (var record : format.parse(reader)) {
			try {
				recordConsumer.accept(record);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
