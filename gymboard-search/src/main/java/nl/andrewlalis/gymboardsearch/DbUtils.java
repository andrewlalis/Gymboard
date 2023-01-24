package nl.andrewlalis.gymboardsearch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DbUtils {
	public static String loadClasspathString(String resourceName) throws IOException {
		InputStream in = DbUtils.class.getResourceAsStream(resourceName);
		if (in == null) throw new IOException("Resource " + resourceName + " not found.");
		String s = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		in.close();
		return s;
	}
}
