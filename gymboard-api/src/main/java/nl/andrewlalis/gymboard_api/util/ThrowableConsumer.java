package nl.andrewlalis.gymboard_api.util;

public interface ThrowableConsumer<T> {
	void accept(T item) throws Exception;
}
