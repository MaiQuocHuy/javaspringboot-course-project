package project.ktc.springboot_app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import project.ktc.springboot_app.common.dto.ApiErrorResponse;
import project.ktc.springboot_app.security.exception.ExpiredJwtTokenException;
import project.ktc.springboot_app.security.exception.InvalidJwtTokenException;
import project.ktc.springboot_app.security.exception.MalformedJwtTokenException;
import project.ktc.springboot_app.utils.JwtTokenProvider;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserDetailsService userDetailsService;
	private final ObjectMapper objectMapper;

	public JwtAuthenticationFilter(
			JwtTokenProvider jwtTokenProvider,
			UserDetailsService userDetailsService,
			ObjectMapper objectMapper) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.userDetailsService = userDetailsService;
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(7);

		try {
			String username = jwtTokenProvider.extractUsername(token);

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				if (jwtTokenProvider.validateToken(token, userDetails)) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
		} catch (ExpiredJwtTokenException e) {
			log.error("JWT token is expired: {}", e.getMessage());
			handleJwtException(
					response,
					HttpStatus.UNAUTHORIZED,
					"JWT_TOKEN_EXPIRED",
					"JWT token has expired. Please obtain a new token.",
					request.getServletPath());
			return;
		} catch (MalformedJwtTokenException e) {
			log.error("JWT token is malformed: {}", e.getMessage());
			handleJwtException(
					response,
					HttpStatus.UNAUTHORIZED,
					"JWT_TOKEN_MALFORMED",
					"JWT token is malformed. Please provide a valid token.",
					request.getServletPath());
			return;
		} catch (InvalidJwtTokenException e) {
			log.error("JWT token is invalid: {}", e.getMessage());
			handleJwtException(
					response,
					HttpStatus.UNAUTHORIZED,
					"JWT_TOKEN_INVALID",
					"JWT token is invalid. Please provide a valid token.",
					request.getServletPath());
			return;
		} catch (Exception e) {
			log.error("Unexpected error during JWT token processing: {}", e.getMessage());
			handleJwtException(
					response,
					HttpStatus.UNAUTHORIZED,
					"JWT_TOKEN_ERROR",
					"An error occurred while processing JWT token.",
					request.getServletPath());
			return;
		}

		filterChain.doFilter(request, response);
	}

	private void handleJwtException(
			HttpServletResponse response,
			HttpStatus status,
			String errorCode,
			String message,
			String path)
			throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		ApiErrorResponse errorResponse = ApiErrorResponse.builder()
				.statusCode(status.value())
				.message(message)
				.error(errorCode)
				.timestamp(ZonedDateTime.now())
				.path(path)
				.build();

		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
