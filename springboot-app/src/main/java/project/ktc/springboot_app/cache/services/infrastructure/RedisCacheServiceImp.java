package project.ktc.springboot_app.cache.services.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.cache.interfaces.CacheService;
import project.ktc.springboot_app.cache.services.CacheStats;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis implementation of the CacheService interface.
 * Provides low-level Redis operations for the cache infrastructure.
 * 
 * @author KTC Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheServiceImp implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void store(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Stored data in cache with key: {}", key);
        } catch (Exception e) {
            log.error("Error storing data in cache with key: {}", key, e);
        }
    }

    @Override
    public void store(String key, Object value, Duration timeout) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout.toSeconds(), TimeUnit.SECONDS);
            log.debug("Stored data in cache with key: {} and TTL: {} seconds", key, timeout.toSeconds());
        } catch (Exception e) {
            log.error("Error storing data in cache with key: {} and TTL: {}", key, timeout, e);
        }
    }

    @Override
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Cache hit for key: {}", key);
            } else {
                log.debug("Cache miss for key: {}", key);
            }
            return value;
        } catch (Exception e) {
            log.error("Error retrieving data from cache with key: {}", key, e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && clazz.isInstance(value)) {
                log.debug("Cache hit for key: {} with type: {}", key, clazz.getSimpleName());
                return (T) value;
            } else if (value != null) {
                log.warn("Cache value type mismatch for key: {}. Expected: {}, Got: {}",
                        key, clazz.getSimpleName(), value.getClass().getSimpleName());
            } else {
                log.debug("Cache miss for key: {}", key);
            }
            return null;
        } catch (Exception e) {
            log.error("Error retrieving data from cache with key: {} and type: {}", key, clazz.getSimpleName(), e);
            return null;
        }
    }

    @Override
    public boolean remove(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            boolean result = Boolean.TRUE.equals(deleted);
            if (result) {
                log.debug("Successfully removed cache entry with key: {}", key);
            } else {
                log.debug("No cache entry found to remove with key: {}", key);
            }
            return result;
        } catch (Exception e) {
            log.error("Error removing data from cache with key: {}", key, e);
            return false;
        }
    }

    @Override
    public long remove(Set<String> keys) {
        try {
            if (keys == null || keys.isEmpty()) {
                return 0;
            }

            Long deleted = redisTemplate.delete(keys);
            long result = deleted != null ? deleted : 0;
            log.debug("Successfully removed {} cache entries", result);
            return result;
        } catch (Exception e) {
            log.error("Error removing multiple cache entries", e);
            return 0;
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Error checking existence for key: {}", key, e);
            return false;
        }
    }

    @Override
    public Set<String> getKeys(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            log.debug("Found {} keys matching pattern: {}", keys != null ? keys.size() : 0, pattern);
            return keys;
        } catch (Exception e) {
            log.error("Error getting keys with pattern: {}", pattern, e);
            return Set.of();
        }
    }

    @Override
    public boolean expire(String key, Duration timeout) {
        try {
            Boolean result = redisTemplate.expire(key, timeout.toSeconds(), TimeUnit.SECONDS);
            boolean success = Boolean.TRUE.equals(result);
            if (success) {
                log.debug("Set expiration for key: {} to {} seconds", key, timeout.toSeconds());
            } else {
                log.debug("Failed to set expiration for key: {} (key may not exist)", key);
            }
            return success;
        } catch (Exception e) {
            log.error("Error setting expiration for key: {}", key, e);
            return false;
        }
    }

    @Override
    public long getTtl(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null ? ttl : -2; // -2 means key doesn't exist
        } catch (Exception e) {
            log.error("Error getting TTL for key: {}", key, e);
            return -2;
        }
    }

    @Override
    public void clear() {
        try {
            log.warn("Clearing all cache entries - this should be used with caution!");
            // Use a safer approach to clear cache
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            log.info("Successfully cleared all cache entries");
        } catch (Exception e) {
            log.error("Error clearing all cache entries", e);
        }
    }

    @Override
    public CacheStats getStats() {
        try {
            // Basic implementation - could be enhanced with more detailed Redis stats
            Set<String> allKeys = redisTemplate.keys("*");
            int totalKeys = allKeys != null ? allKeys.size() : 0;

            return CacheStats.builder()
                    .keyCount(totalKeys)
                    .hits(0L) // Would need Redis INFO command parsing for actual stats
                    .misses(0L)
                    .hitRatio(0.0)
                    .totalOperations(0L)
                    .memoryUsage(0L)
                    .evictions(0L)
                    .expirations(0L)
                    .since(null)
                    .build();
        } catch (Exception e) {
            log.error("Error getting cache statistics", e);
            return CacheStats.builder()
                    .keyCount(0)
                    .hits(0L)
                    .misses(0L)
                    .hitRatio(0.0)
                    .totalOperations(0L)
                    .memoryUsage(0L)
                    .evictions(0L)
                    .expirations(0L)
                    .since(null)
                    .build();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key, Class<T> elementClass) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && value instanceof List) {
                log.debug("Cache hit for list key: {} with element type: {}", key, elementClass.getSimpleName());
                return (List<T>) value;
            } else if (value != null) {
                log.warn("Cache value type mismatch for key: {}. Expected: List<{}>, Got: {}",
                        key, elementClass.getSimpleName(), value.getClass().getSimpleName());
            } else {
                log.debug("Cache miss for list key: {}", key);
            }
            return null;
        } catch (Exception e) {
            log.error("Error retrieving list from cache with key: {} and element type: {}",
                    key, elementClass.getSimpleName(), e);
            return null;
        }
    }
}