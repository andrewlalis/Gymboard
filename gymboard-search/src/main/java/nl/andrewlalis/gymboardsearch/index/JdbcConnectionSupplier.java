package nl.andrewlalis.gymboardsearch.index;

import java.sql.Connection;
import java.sql.SQLException;

public interface JdbcConnectionSupplier {
	Connection getConnection() throws SQLException;
}
