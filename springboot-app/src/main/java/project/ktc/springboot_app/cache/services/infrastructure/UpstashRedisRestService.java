package project.ktc.springboot_app.cache.services.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * REST-based Redis service for Upstash Redis
 *
 * <p>
 * This service provides Redis functionality using Upstash REST API as an
 * alternative when direct
 * TCP connection to Redis is blocked. Only available in production profile.
 *
 * @author KTC Team
 */
@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class UpstashRedisRestService {

	@Value("${upstash.redis.rest.url:localhost}")
	private String restUrl;

	@Value("${upstash.redis.rest.token}")
	private String restToken;

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper;

	/** Test Redis connectivity via REST API */
	public boolean testConnection() {
		try {
			String response = ping();
			return "PONG".equals(response);
		} catch (Exception e) {
			log.error("Failed to test Redis connection via REST API", e);
			return false;
		}
	}

	/** Ping Redis server */
	public String ping() {
		try {
			String response = executeCommand("PING");
			return parseStringResult(response);
		} catch (Exception e) {
			log.error("Failed to ping Redis via REST API", e);
			throw new RuntimeException("Redis ping failed", e);
		}
	}

	/** Set a key-value pair */
	public void set(String key, String value) {
		try {
			executeCommand("SET", key, value);
			log.debug("Set key: {} via REST API", key);
		} catch (Exception e) {
			log.error("Failed to set key: {} via REST API", key, e);
			throw new RuntimeException("Redis SET failed", e);
		}
	}

	/** Set a key-value pair with expiration */
	public void setex(String key, String value, Duration ttl) {
		try {
			executeCommand("SETEX", key, String.valueOf(ttl.getSeconds()), value);
			log.debug("Set key: {} with TTL: {} seconds via REST API", key, ttl.getSeconds());
		} catch (Exception e) {
			log.error("Failed to set key: {} with TTL via REST API", key, e);
			throw new RuntimeException("Redis SETEX failed", e);
		}
	}

	/** Get value by key */
	public String get(String key) {
		try {
			String response = executeCommand("GET", key);
			return parseStringResult(response);
		} catch (Exception e) {
			log.error("Failed to get key: {} via REST API", key, e);
			throw new RuntimeException("Redis GET failed", e);
		}
	}

	/** Delete a key */
	public void delete(String key) {
		try {
			executeCommand("DEL", key);
			log.debug("Deleted key: {} via REST API", key);
		} catch (Exception e) {
			log.error("Failed to delete key: {} via REST API", key, e);
			throw new RuntimeException("Redis DEL failed", e);
		}
	}

	/** Check if key exists */
	public boolean exists(String key) {
		try {
			String response = executeCommand("EXISTS", key);
			JsonNode result = objectMapper.readTree(response);
			return result.path("result").asInt() == 1;
		} catch (Exception e) {
			log.error("Failed to check existence of key: {} via REST API", key, e);
			return false;
		}
	}

	/** Set expiration for a key */
	public void expire(String key, Duration ttl) {
		try {
			executeCommand("EXPIRE", key, String.valueOf(ttl.getSeconds()));
			log.debug("Set expiration for key: {} to {} seconds via REST API", key, ttl.getSeconds());
		} catch (Exception e) {
			log.error("Failed to set expiration for key: {} via REST API", key, e);
			throw new RuntimeException("Redis EXPIRE failed", e);
		}
	}

	/** Execute a Redis command via REST API */
	private String executeCommand(String... command) {
		try {
			// URL encode each command part to handle special characters
			StringBuilder urlBuilder = new StringBuilder(restUrl);
			for (String part : command) {
				urlBuilder.append("/");
				urlBuilder.append(URLEncoder.encode(part, StandardCharsets.UTF_8));
			}
			String url = urlBuilder.toString();

			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer " + restToken);
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			HttpEntity<String> entity = new HttpEntity<>(headers);

			log.debug("Executing Redis command via REST: {}", String.join(" ", command));
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				return response.getBody();
			} else {
				throw new RuntimeException("Redis REST API returned status: " + response.getStatusCode());
			}
		} catch (Exception e) {
			log.error("Failed to execute Redis command via REST API: {}", String.join(" ", command), e);
			throw new RuntimeException("Redis REST API call failed", e);
		}
	}

	/** Parse string result from REST API response */
	private String parseStringResult(String response) {
		try {
			JsonNode result = objectMapper.readTree(response);
			JsonNode resultNode = result.path("result");
			if (resultNode.isNull()) {
				return null;
			}

			String value = resultNode.asText();
			// URL decode the value since we URL encode when storing
			try {
				return URLDecoder.decode(value, StandardCharsets.UTF_8);
			} catch (Exception decodeException) {
				// If decoding fails, return the original value
				log.warn("Failed to URL decode value, returning as-is: {}", value);
				return value;
			}
		} catch (Exception e) {
			log.error("Failed to parse Redis REST API response: {}", response, e);
			throw new RuntimeException("Failed to parse Redis response", e);
		}
	}
}
