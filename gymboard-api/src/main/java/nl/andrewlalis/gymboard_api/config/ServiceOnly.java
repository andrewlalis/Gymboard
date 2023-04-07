package nl.andrewlalis.gymboard_api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be applied to a controller or controller method to
 * restrict access to only requests from another service that provide a
 * legitimate service secret.
 * @see ServiceAccessInterceptor
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceOnly {}
