package nl.andrewlalis.gymboard_api.util.sample_data;

import java.util.Collection;
import java.util.Collections;

/**
 * Interface that defines a component that can generate sample data for testing
 * the application. It must define a method to generate data, and optionally, it
 * can specify a collection of <em>other</em> sample data generator classes that
 * it depends on. For example, a gym generator might depend on a generator that
 * creates countries and cities.
 * <p>
 *     Note that all classes which implement this interface should be annotated
 *     as a <code>@Component</code> and <code>@Profile("development")</code> to
 *     ensure that we keep sample data generation away from production.
 * </p>
 */
public interface SampleDataGenerator {
	void generate() throws Exception;

	default Collection<Class<? extends SampleDataGenerator>> dependencies() {
		return Collections.emptySet();
	}
}
