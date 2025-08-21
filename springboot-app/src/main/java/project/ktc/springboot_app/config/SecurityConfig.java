package project.ktc.springboot_app.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationProvider;

import project.ktc.springboot_app.security.CustomPermissionEvaluator;
import project.ktc.springboot_app.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize and @PostAuthorize
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final AuthenticationProvider authenticationProvider;
        private final CustomPermissionEvaluator customPermissionEvaluator;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                        AuthenticationProvider authenticationProvider,
                        CustomPermissionEvaluator customPermissionEvaluator) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.authenticationProvider = authenticationProvider;
                this.customPermissionEvaluator = customPermissionEvaluator;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/auth/**",
                                                                "/api-docs/**",
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui/index.html",
                                                                "/swagger-resources/**",
                                                                "/webjars/**",
                                                                "/v3/api-docs/**",
                                                                "/v3/api-docs.yaml",
                                                                "/swagger-config/**",
                                                                "/api/courses/**",
                                                                "/api/categories/**",
                                                                "/api/stripe/webhook",
                                                                "/error")
                                                .permitAll()
                                                // Admin
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                                                // Instructor
                                                .requestMatchers("/api/instructor/**").hasAnyRole("INSTRUCTOR")

                                                // Student
                                                .requestMatchers("/api/student/**", "/api/enrollments/**")
                                                .hasAnyRole("STUDENT")

                                                .requestMatchers("/api/users/profile").authenticated() // Profile

                                                // authentication
                                                .requestMatchers("/api/upload/**")
                                                .hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN") // Secured

                                                .anyRequest().authenticated())
                                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .build();
        }

        /**
         * Configure method security expression handler with custom permission evaluator
         */
        @Bean
        public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
                DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
                expressionHandler.setPermissionEvaluator(customPermissionEvaluator);
                return expressionHandler;
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                config.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }
}