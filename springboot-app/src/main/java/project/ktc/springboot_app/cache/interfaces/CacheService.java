package project.ktc.springboot_app.cache.interfaces;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import project.ktc.springboot_app.cache.services.CacheStats;

/**
 * Base interface for cache operations.
 * Provides common caching functionality that can be implemented
 * by specific cache services (Redis, Hazelcast, etc.).
 * 
 * @author KTC Team
 */
public interface CacheService {

    /**
     * Store data in cache with default TTL
     * 
     * @param key   cache key
     * @param value data to cache
     */
    void store(String key, Object value);

    /**
     * Store data in cache with custom TTL
     * 
     * @param key     cache key
     * @param value   data to cache
     * @param timeout cache expiration time
     */
    void store(String key, Object value, Duration timeout);

    /**
     * Retrieve data from cache
     * 
     * @param key cache key
     * @return cached data or null if not found
     */
    Object get(String key);

    /**
     * Retrieve and cast data from cache
     * 
     * @param key   cache key
     * @param clazz expected data type
     * @param <T>   data type
     * @return cached data or null if not found
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * Retrieve and cast data from cache as a List
     * 
     * @param key          cache key
     * @param elementClass expected element type in the list
     * @param <T>          element type
     * @return cached list or null if not found
     */
    <T> List<T> getList(String key, Class<T> elementClass);

    /**
     * Remove data from cache
     * 
     * @param key cache key
     * @return true if data was removed
     */
    boolean remove(String key);

    /**
     * Remove multiple keys from cache
     * 
     * @param keys set of cache keys
     * @return number of keys removed
     */
    long remove(Set<String> keys);

    /**
     * Check if key exists in cache
     * 
     * @param key cache key
     * @return true if key exists
     */
    boolean exists(String key);

    /**
     * Get keys matching pattern
     * 
     * @param pattern key pattern (supports wildcards)
     * @return set of matching keys
     */
    Set<String> getKeys(String pattern);

    /**
     * Set expiration time for existing key
     * 
     * @param key     cache key
     * @param timeout expiration time
     * @return true if expiration was set
     */
    boolean expire(String key, Duration timeout);

    /**
     * Get remaining TTL for key
     * 
     * @param key cache key
     * @return remaining TTL in seconds, -1 if no expiration, -2 if key doesn't
     *         exist
     */
    long getTtl(String key);

    /**
     * Clear all cache entries (use with caution)
     */
    void clear();

    /**
     * Get cache statistics (hits, misses, etc.)
     * 
     * @return cache statistics information
     */
    CacheStats getStats();
}