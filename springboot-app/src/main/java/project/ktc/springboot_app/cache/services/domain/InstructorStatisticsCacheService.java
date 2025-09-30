package project.ktc.springboot_app.cache.services.domain;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.cache.dto.InstructorStatisticsCacheDto;
import project.ktc.springboot_app.cache.keys.CacheConstants;
import project.ktc.springboot_app.cache.keys.CacheKeyBuilder;
import project.ktc.springboot_app.cache.mappers.InstructorStatisticsCacheMapper;
import project.ktc.springboot_app.instructor_dashboard.dto.InsDashboardDto;

/**
 * Cache service for instructor statistics operations Handles caching of
 * instructor dashboard
 * statistics with appropriate TTL and provides clean separation between service
 * DTOs and cache DTOs
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorStatisticsCacheService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final CacheKeyBuilder cacheKeyBuilder;

	/**
	 * Retrieves instructor statistics from cache
	 *
	 * @param instructorId
	 *            the instructor identifier
	 * @return cached statistics or null if not found
	 */
	public InsDashboardDto getInstructorStatistics(String instructorId) {
		try {
			String cacheKey = cacheKeyBuilder.buildInstructorStatisticsKey(instructorId);
			log.debug("Attempting to retrieve instructor statistics from cache with key: {}", cacheKey);

			InstructorStatisticsCacheDto cacheDto = (InstructorStatisticsCacheDto) redisTemplate.opsForValue()
					.get(cacheKey);

			if (cacheDto != null) {
				log.debug("Cache hit for instructor statistics - instructor: {}", instructorId);
				return InstructorStatisticsCacheMapper.fromCacheDto(cacheDto);
			}

			log.debug("Cache miss for instructor statistics - instructor: {}", instructorId);
			return null;

		} catch (Exception e) {
			log.error(
					"Error retrieving instructor statistics from cache for instructor: {}", instructorId, e);
			return null;
		}
	}

	/**
	 * Stores instructor statistics in cache with TTL
	 *
	 * @param instructorId
	 *            the instructor identifier
	 * @param instructorStatistics
	 *            the statistics to cache
	 */
	public void storeInstructorStatistics(String instructorId, InsDashboardDto instructorStatistics) {
		try {
			String cacheKey = cacheKeyBuilder.buildInstructorStatisticsKey(instructorId);
			log.debug("Storing instructor statistics in cache with key: {}", cacheKey);

			InstructorStatisticsCacheDto cacheDto = InstructorStatisticsCacheMapper.toCacheDto(instructorStatistics,
					instructorId);

			redisTemplate
					.opsForValue()
					.set(
							cacheKey,
							cacheDto,
							CacheConstants.INSTRUCTOR_STATISTICS_TTL.toSeconds(),
							TimeUnit.SECONDS);

			log.debug(
					"Successfully cached instructor statistics for instructor: {} with TTL: {} seconds",
					instructorId,
					CacheConstants.INSTRUCTOR_STATISTICS_TTL.toSeconds());

		} catch (Exception e) {
			log.error("Error storing instructor statistics in cache for instructor: {}", instructorId, e);
		}
	}

	/**
	 * Invalidates instructor statistics cache for a specific instructor
	 *
	 * @param instructorId
	 *            the instructor identifier
	 */
	public void invalidateInstructorStatistics(String instructorId) {
		try {
			String cacheKey = cacheKeyBuilder.buildInstructorStatisticsKey(instructorId);
			log.debug("Invalidating instructor statistics cache with key: {}", cacheKey);

			Boolean deleted = redisTemplate.delete(cacheKey);

			if (Boolean.TRUE.equals(deleted)) {
				log.debug(
						"Successfully invalidated instructor statistics cache for instructor: {}",
						instructorId);
			} else {
				log.debug("No cache entry found to invalidate for instructor: {}", instructorId);
			}

		} catch (Exception e) {
			log.error(
					"Error invalidating instructor statistics cache for instructor: {}", instructorId, e);
		}
	}

	/**
	 * Checks if instructor statistics are cached
	 *
	 * @param instructorId
	 *            the instructor identifier
	 * @return true if cached, false otherwise
	 */
	public boolean isStatisticsCached(String instructorId) {
		try {
			String cacheKey = cacheKeyBuilder.buildInstructorStatisticsKey(instructorId);
			Boolean exists = redisTemplate.hasKey(cacheKey);
			return Boolean.TRUE.equals(exists);
		} catch (Exception e) {
			log.error(
					"Error checking instructor statistics cache existence for instructor: {}",
					instructorId,
					e);
			return false;
		}
	}
}
