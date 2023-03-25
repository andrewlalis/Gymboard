package nl.andrewlalis.gymboard_api.util;

import org.junit.jupiter.api.Test;

import static nl.andrewlalis.gymboard_api.util.DataUtils.formatLargeInt;
import static org.junit.jupiter.api.Assertions.*;

public class DataUtilsTest {
	@Test
	public void testFormatLargeInt() {
		assertEquals("0", formatLargeInt(0));
		assertEquals("1", formatLargeInt(1));
		assertEquals("42", formatLargeInt(42));
		assertEquals("999", formatLargeInt(999));
		assertEquals("1K", formatLargeInt(1000));
		assertEquals("1K", formatLargeInt(1099));
		assertEquals("1.1K", formatLargeInt(1100));
		assertEquals("1.1K", formatLargeInt(1199));
		assertEquals("25.2K", formatLargeInt(25_231));
		assertEquals("999K", formatLargeInt(999_000));
		assertEquals("1M", formatLargeInt(1_000_000));
		assertEquals("1.5M", formatLargeInt(1_500_000));
		assertEquals("3B", formatLargeInt(3_000_000_000L));
		assertEquals("1024B", formatLargeInt(1_024_000_000_000L));
	}
}
