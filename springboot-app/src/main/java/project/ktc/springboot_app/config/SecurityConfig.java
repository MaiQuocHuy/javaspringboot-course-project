package project.ktc.springboot_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationProvider;
import project.ktc.springboot_app.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final AuthenticationProvider authenticationProvider;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                        AuthenticationProvider authenticationProvider) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.authenticationProvider = authenticationProvider;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
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
                                                                "/error")
                                                .permitAll()
                                                // Admin
                                                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                                                // Instructor
                                                .requestMatchers("/api/instructor/**").hasAnyAuthority("INSTRUCTOR")

                                                // Student
                                                .requestMatchers("/api/student/**", "/api/enrollments/**")
                                                .hasAnyAuthority("STUDENT")

                                                .requestMatchers("/api/users/profile").authenticated() // Profile

                                                // authentication
                                                .requestMatchers("/api/upload/**")
                                                .hasAnyAuthority("STUDENT", "INSTRUCTOR", "ADMIN") // Secured

                                                .anyRequest().authenticated())
                                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .build();
        }
}