package nl.andrewlalis.gymboard_api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

/**
 * An interceptor that checks that requests to endpoints annotated with
 * {@link ServiceOnly} have a valid service secret header value.
 */
@Component
public class ServiceAccessInterceptor implements HandlerInterceptor {
	public static final String HEADER_NAME = "X-Gymboard-Service-Secret";

	@Value("${app.service-secret}")
	private String serviceSecret;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		Method handlerMethod = ((HandlerMethod) handler).getMethod();
		Class<?> handlerClass = handlerMethod.getDeclaringClass();

		ServiceOnly methodAnnotation = handlerMethod.getAnnotation(ServiceOnly.class);
		ServiceOnly classAnnotation = handlerClass.getAnnotation(ServiceOnly.class);
		if (methodAnnotation != null || classAnnotation != null) {
			String secret = request.getHeader(HEADER_NAME);
			if (secret == null || !secret.trim().equals(serviceSecret)) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return false;
			}
		}
		return true;
	}
}
