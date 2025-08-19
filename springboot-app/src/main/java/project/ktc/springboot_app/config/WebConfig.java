package project.ktc.springboot_app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import project.ktc.springboot_app.filter_rule.interceptors.FilterContextCleanupInterceptor;

/**
 * Web configuration for registering interceptors
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final FilterContextCleanupInterceptor filterContextCleanupInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(filterContextCleanupInterceptor)
                .addPathPatterns("/**");
    }
}
