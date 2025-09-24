package project.ktc.springboot_app.cache.services.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.cache.interfaces.CacheService;
import project.ktc.springboot_app.cache.services.CacheStats;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Hybrid Redis cache service that tries direct Redis connection first,
 * then falls back to REST API if direct connection fails.
 * 
 * This provides resilience when TCP connections to Redis are blocked
 * but HTTPS REST API calls work.
 * 
 * @author KTC Team
 */
@Service
@Primary
@Slf4j
@RequiredArgsConstructor
public class HybridRedisCacheService implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UpstashRedisRestService restService;
    private final ObjectMapper objectMapper;

    // Track connection state to avoid repeated attempts
    private volatile boolean directConnectionAvailable = true;
    private volatile long lastDirectConnectionCheck = 0;
    private static final long CONNECTION_CHECK_INTERVAL = 30000; // 30 seconds

    @Override
    public void store(String key, Object value) {
        if (tryDirectConnection()) {
            try {
                redisTemplate.opsForValue().set(key, value);
                log.debug("Stored data in cache via direct connection with key: {}", key);
                markDirectConnectionWorking();
                return;
            } catch (Exception e) {
                log.warn("Direct Redis connection failed for store, falling back to REST API: {}", e.getMessage());
                markDirectConnectionFailed();
            }
        }

        // Fallback to REST API
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            restService.set(key, jsonValue);
            log.debug("Stored data in cache via REST API with key: {}", key);
        } catch (Exception e) {
            log.error("Error storing data in cache with key: {} via both direct and REST", key, e);
        }
    }

    @Override
    public void store(String key, Object value, Duration timeout) {
        if (tryDirectConnection()) {
            try {
                redisTemplate.opsForValue().set(key, value, timeout.toSeconds(), TimeUnit.SECONDS);
                log.debug("Stored data in cache via direct connection with key: {} and TTL: {} seconds",
                        key, timeout.toSeconds());
                markDirectConnectionWorking();
                return;
            } catch (Exception e) {
                log.warn("Direct Redis connection failed for store with TTL, falling back to REST API: {}",
                        e.getMessage());
                markDirectConnectionFailed();
            }
        }

        // Fallback to REST API
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            restService.setex(key, jsonValue, timeout);
            log.debug("Stored data in cache via REST API with key: {} and TTL: {} seconds",
                    key, timeout.toSeconds());
        } catch (Exception e) {
            log.error("Error storing data in cache with key: {} and TTL: {} via both direct and REST",
                    key, timeout, e);
        }
    }

    @Override
    public Object get(String key) {
        if (tryDirectConnection()) {
            try {
                Object value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    log.debug("Cache hit via direct connection for key: {}", key);
                } else {
                    log.debug("Cache miss via direct connection for key: {}", key);
                }
                markDirectConnectionWorking();
                return value;
            } catch (Exception e) {
                log.warn("Direct Redis connection failed for get, falling back to REST API: {}", e.getMessage());
                markDirectConnectionFailed();
            }
        }

        // Fallback to REST API
        try {
            String jsonValue = restService.get(key);
            if (jsonValue != null) {
                Object value = objectMapper.readValue(jsonValue, Object.class);
                log.debug("Cache hit via REST API for key: {}", key);
                return value;
            } else {
                log.debug("Cache miss via REST API for key: {}", key);
                return null;
            }
        } catch (Exception e) {
            log.error("Error retrieving data from cache with key: {} via both direct and REST", key, e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        if (tryDirectConnection()) {
            try {
                Object value = redisTemplate.opsForValue().get(key);
                if (value != null && clazz.isInstance(value)) {
                    log.debug("Cache hit via direct connection for key: {} with type: {}", key, clazz.getSimpleName());
                    markDirectConnectionWorking();
                    return (T) value;
                } else if (value != null) {
                    log.warn("Cache value type mismatch via direct connection for key: {}. Expected: {}, Got: {}",
                            key, clazz.getSimpleName(), value.getClass().getSimpleName());
                } else {
                    log.debug("Cache miss via direct connection for key: {}", key);
                }
                markDirectConnectionWorking();
                return null;
            } catch (Exception e) {
                log.warn("Direct Redis connection failed for typed get, falling back to REST API: {}", e.getMessage());
                markDirectConnectionFailed();
            }
        }

        // Fallback to REST API
        try {
            String jsonValue = restService.get(key);
            if (jsonValue != null) {
                T value = objectMapper.readValue(jsonValue, clazz);
                log.debug("Cache hit via REST API for key: {} with type: {}", key, clazz.getSimpleName());
                return value;
            } else {
                log.debug("Cache miss via REST API for key: {}", key);
                return null;
            }
        } catch (Exception e) {
            log.error("Error retrieving data from cache with key: {} and type: {} via both direct and REST",
                    key, clazz.getSimpleName(), e);
            return null;
        }
    }

    @Override
    public boolean remove(String key) {
        boolean directResult = false;
        boolean restResult = false;

        if (tryDirectConnection()) {
            try {
                Boolean deleted = redisTemplate.delete(key);
                directResult = Boolean.TRUE.equals(deleted);
                if (directResult) {
                    log.debug("Successfully removed cache entry via direct connection with key: {}", key);
                } else {
                    log.debug("No cache entry found to remove via direct connection with key: {}", key);
                }
                markDirectConnectionWorking();
            } catch (Exception e) {
                log.warn("Direct Redis connection failed for remove, trying REST API: {}", e.getMessage());
                markDirectConnectionFailed();
            }
        }

        // Try REST API (either as fallback or in addition to direct)
        try {
            restService.delete(key);
            restResult = true;
            log.debug("Successfully removed cache entry via REST API with key: {}", key);
        } catch (Exception e) {
            log.error("Error removing data from cache with key: {} via REST API", key, e);
        }

        return directResult || restResult;
    }

    @Override
    public boolean exists(String key) {
        if (tryDirectConnection()) {
            try {
                Boolean exists = redisTemplate.hasKey(key);
                boolean result = Boolean.TRUE.equals(exists);
                log.debug("Key existence check via direct connection for key: {} = {}", key, result);
                markDirectConnectionWorking();
                return result;
            } catch (Exception e) {
                log.warn("Direct Redis connection failed for exists check, falling back to REST API: {}",
                        e.getMessage());
                markDirectConnectionFailed();
            }
        }

        // Fallback to REST API
        try {
            boolean result = restService.exists(key);
            log.debug("Key existence check via REST API for key: {} = {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Error checking key existence with key: {} via both direct and REST", key, e);
            return false;
        }
    }

    @Override
    public boolean expire(String key, Duration timeout) {
        if (tryDirectConnection()) {
            try {
                Boolean result = redisTemplate.expire(key, timeout.toSeconds(), TimeUnit.SECONDS);
                boolean success = Boolean.TRUE.equals(result);
                log.debug("Set expiration via direct connection for key: {} to {} seconds, result: {}",
                        key, timeout.toSeconds(), success);
                markDirectConnectionWorking();
                return success;
            } catch (Exception e) {
                log.warn("Direct Redis connection failed for expire, falling back to REST API: {}", e.getMessage());
                markDirectConnectionFailed();
            }
        }

        // Fallback to REST API
        try {
            restService.expire(key, timeout);
            log.debug("Set expiration via REST API for key: {} to {} seconds", key, timeout.toSeconds());
            return true; // REST API doesn't return boolean, assume success if no exception
        } catch (Exception e) {
            log.error("Error setting expiration for key: {} via both direct and REST", key, e);
            return false;
        }
    }

    @Override
    public Set<String> getKeys(String pattern) {
        if (tryDirectConnection()) {
            try {
                Set<String> keys = redisTemplate.keys(pattern);
                log.debug("Retrieved {} keys via direct connection with pattern: {}",
                        keys != null ? keys.size() : 0, pattern);
                markDirectConnectionWorking();
                return keys;
            } catch (Exception e) {
                log.warn(
                        "Direct Redis connection failed for keys operation, REST API doesn't support pattern matching: {}",
                        e.getMessage());
                markDirectConnectionFailed();
            }
        }

        // REST API doesn't support KEYS pattern matching
        log.warn("Keys pattern matching not supported via REST API for pattern: {}", pattern);
        return Set.of();
    }

    @Override
    public long remove(Set<String> keys) {
        long totalRemoved = 0;

        if (tryDirectConnection()) {
            try {
                if (keys != null && !keys.isEmpty()) {
                    Long deleted = redisTemplate.delete(keys);
                    long directResult = deleted != null ? deleted : 0;
                    log.debug("Removed {} keys via direct connection", directResult);
                    markDirectConnectionWorking();
                    return directResult;
                }
                markDirectConnectionWorking();
                return 0;
            } catch (Exception e) {
                log.warn("Direct Redis connection failed for batch removal, trying REST API: {}", e.getMessage());
                markDirectConnectionFailed();
            }
        }

        // Fallback to REST API - remove keys one by one
        if (keys != null) {
            for (String key : keys) {
                try {
                    restService.delete(key);
                    totalRemoved++;
                } catch (Exception e) {
                    log.error("Error removing key {} via REST API", key, e);
                }
            }
            log.debug("Removed {} keys via REST API", totalRemoved);
        }

        return totalRemoved;
    }

    @Override
    public long getTtl(String key) {
        if (tryDirectConnection()) {
            try {
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                long result = ttl != null ? ttl : -2;
                log.debug("Retrieved TTL via direct connection for key: {} = {} seconds", key, result);
                markDirectConnectionWorking();
                return result;
            } catch (Exception e) {
                log.warn("Direct Redis connection failed for TTL check, REST API doesn't support TTL operations: {}",
                        e.getMessage());
                markDirectConnectionFailed();
            }
        }

        // REST API doesn't support TTL operations
        log.warn("TTL check not supported via REST API for key: {}", key);
        return -2; // Key doesn't exist (from Redis perspective)
    }

    @Override
    public void clear() {
        if (tryDirectConnection()) {
            try {
                Set<String> allKeys = redisTemplate.keys("*");
                if (allKeys != null && !allKeys.isEmpty()) {
                    redisTemplate.delete(allKeys);
                    log.info("Cleared {} keys via direct connection", allKeys.size());
                }
                markDirectConnectionWorking();
                return;
            } catch (Exception e) {
                log.warn("Direct Redis connection failed for clear operation, REST API doesn't support clear all: {}",
                        e.getMessage());
                markDirectConnectionFailed();
            }
        }

        // REST API doesn't support clear all operation
        log.warn("Clear all operation not supported via REST API");
    }

    public long removeByPattern(String pattern) {
        if (tryDirectConnection()) {
            try {
                Set<String> keys = redisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    Long deleted = redisTemplate.delete(keys);
                    long result = deleted != null ? deleted : 0;
                    log.debug("Removed {} keys via direct connection with pattern: {}", result, pattern);
                    markDirectConnectionWorking();
                    return result;
                }
                markDirectConnectionWorking();
                return 0;
            } catch (Exception e) {
                log.warn(
                        "Direct Redis connection failed for pattern removal, REST API doesn't support pattern operations: {}",
                        e.getMessage());
                markDirectConnectionFailed();
            }
        }

        // REST API doesn't support pattern operations
        log.warn("Pattern removal not supported via REST API for pattern: {}", pattern);
        return 0;
    }

    @Override
    public CacheStats getStats() {
        // Try to get basic stats
        try {
            // Test if any connection is available
            restService.testConnection();
            return CacheStats.builder()
                    .hits(0L) // We don't track detailed stats in this implementation
                    .misses(0L)
                    .hitRatio(0.0)
                    .totalOperations(0L)
                    .keyCount(0L)
                    .memoryUsage(0L)
                    .since(java.time.LocalDateTime.now())
                    .evictions(0L)
                    .expirations(0L)
                    .build();
        } catch (Exception e) {
            log.error("Error getting cache stats", e);
            return CacheStats.builder()
                    .hits(0L)
                    .misses(0L)
                    .hitRatio(0.0)
                    .totalOperations(0L)
                    .keyCount(0L)
                    .memoryUsage(0L)
                    .since(java.time.LocalDateTime.now())
                    .evictions(0L)
                    .expirations(0L)
                    .build();
        }
    }

    /**
     * Check if we should try the direct connection
     */
    private boolean tryDirectConnection() {
        if (directConnectionAvailable) {
            return true;
        }

        // Periodically retry the direct connection
        long now = System.currentTimeMillis();
        if (now - lastDirectConnectionCheck > CONNECTION_CHECK_INTERVAL) {
            lastDirectConnectionCheck = now;
            log.debug("Retrying direct Redis connection check...");
            return true;
        }

        return false;
    }

    /**
     * Mark direct connection as working
     */
    private void markDirectConnectionWorking() {
        if (!directConnectionAvailable) {
            log.info("Direct Redis connection restored");
            directConnectionAvailable = true;
        }
    }

    /**
     * Mark direct connection as failed
     */
    private void markDirectConnectionFailed() {
        if (directConnectionAvailable) {
            log.warn("Direct Redis connection failed, switching to REST API fallback");
            directConnectionAvailable = false;
            lastDirectConnectionCheck = System.currentTimeMillis();
        }
    }
}
