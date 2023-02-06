package nl.andrewlalis.gymboardsearch.index;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface JdbcResultSetSupplier {
	ResultSet supply(Connection conn) throws SQLException, IOException;
}
