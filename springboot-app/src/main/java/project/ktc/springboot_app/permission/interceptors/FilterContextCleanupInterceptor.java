package project.ktc.springboot_app.permission.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.permission.security.CustomPermissionEvaluator;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to clean up thread-local context after request processing
 */
@Component
@Slf4j
public class FilterContextCleanupInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) throws Exception {
        try {
            // Clean up thread-local context to prevent memory leaks
            CustomPermissionEvaluator.EffectiveFilterContext.clear();
            log.trace("Cleaned up effective filter context for request: {}", request.getRequestURI());
        } catch (Exception e) {
            log.error("Error cleaning up filter context", e);
        }
    }
}
