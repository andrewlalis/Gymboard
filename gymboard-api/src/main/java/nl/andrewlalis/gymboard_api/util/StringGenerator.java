package nl.andrewlalis.gymboard_api.util;

import java.security.SecureRandom;
import java.util.Random;

public class StringGenerator {
	public enum Alphabet {
		ALPHANUMERIC("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

		public final String value;

		Alphabet(String value) {
			this.value = value;
		}
	}

	public static String randomString(int length, Alphabet alphabet) {
		Random random = new SecureRandom();
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(alphabet.value.charAt(random.nextInt(alphabet.value.length())));
		}
		return sb.toString();
	}
}
