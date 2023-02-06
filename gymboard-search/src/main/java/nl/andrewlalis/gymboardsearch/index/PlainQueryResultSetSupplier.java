package nl.andrewlalis.gymboardsearch.index;

import nl.andrewlalis.gymboardsearch.DbUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlainQueryResultSetSupplier implements JdbcResultSetSupplier {
	private final String query;

	public PlainQueryResultSetSupplier(String query) {
		this.query = query;
	}

	public static PlainQueryResultSetSupplier fromResourceFile(String resource) throws IOException {
		return new PlainQueryResultSetSupplier(DbUtils.loadClasspathString(resource));
	}

	@Override
	public ResultSet supply(Connection conn) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(query);
		return stmt.executeQuery();
	}
}
