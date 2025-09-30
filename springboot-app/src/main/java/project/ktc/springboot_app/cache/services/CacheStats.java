package project.ktc.springboot_app.cache.services;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Cache statistics information for monitoring and debugging.
 *
 * @author KTC Team
 */
@Data
@Builder
public class CacheStats {

  /** Total number of cache hits */
  private long hits;

  /** Total number of cache misses */
  private long misses;

  /** Cache hit ratio (hits / (hits + misses)) */
  private double hitRatio;

  /** Total number of cache operations */
  private long totalOperations;

  /** Number of stored keys */
  private long keyCount;

  /** Approximate memory usage in bytes */
  private long memoryUsage;

  /** Cache uptime since last reset */
  private LocalDateTime since;

  /** Number of evicted keys */
  private long evictions;

  /** Number of expired keys */
  private long expirations;
}
