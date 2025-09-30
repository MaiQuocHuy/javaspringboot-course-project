package project.ktc.springboot_app.cache.services.domain;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.cache.interfaces.CacheService;
import project.ktc.springboot_app.cache.keys.CacheConstants;
import project.ktc.springboot_app.cache.keys.CacheKeyBuilder;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.course.dto.cache.InstructorCourseBaseCacheDto;
import project.ktc.springboot_app.course.dto.cache.InstructorCourseDynamicCacheDto;
import project.ktc.springboot_app.course.entity.CourseReviewStatus;
import project.ktc.springboot_app.course.enums.CourseLevel;

/**
 * Instructor-specific cache service that provides high-level caching operations
 * for instructor
 * courses with two-tier caching strategy: - Base info: Less frequently changing
 * data (5-10 min TTL)
 * - Dynamic info: Frequently changing data (30-60s TTL)
 *
 * @author KTC Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorCacheService {

	private final CacheService cacheService;
	private final CacheKeyBuilder cacheKeyBuilder;

	// TTL Constants
	private static final Duration BASE_INFO_TTL = CacheConstants.INSTRUCTOR_COURSES_BASE_TTL;
	private static final Duration DYNAMIC_INFO_TTL = CacheConstants.INSTRUCTOR_COURSES_DYNAMIC_TTL;

	/**
	 * Gets cached paginated instructor courses base info (less frequently changing
	 * data) Base info
	 * includes: id, title, description, price, categories, thumbnailUrl, level,
	 * isApproved,
	 * isPublished, createdAt, updatedAt, status
	 */
	@SuppressWarnings("unchecked")
	public PaginatedResponse<InstructorCourseBaseCacheDto> getInstructorCoursesBaseInfo(
			String instructorId,
			Pageable pageable,
			String search,
			CourseReviewStatus.ReviewStatus status,
			List<String> categoryIds,
			Double minPrice,
			Double maxPrice,
			Integer rating,
			CourseLevel level,
			Boolean isPublished) {

		try {
			String cacheKey = buildInstructorCoursesBaseCacheKey(
					instructorId,
					pageable,
					search,
					status,
					categoryIds,
					minPrice,
					maxPrice,
					rating,
					level,
					isPublished);

			log.debug(
					"Attempting to retrieve instructor courses base info from cache with key: {}", cacheKey);

			PaginatedResponse<InstructorCourseBaseCacheDto> cachedData = (PaginatedResponse<InstructorCourseBaseCacheDto>) cacheService
					.get(cacheKey);

			if (cachedData != null) {
				log.debug(
						"Cache hit for instructor courses base info - {} courses found",
						cachedData.getContent().size());
				return cachedData;
			}

			log.debug("Cache miss for instructor courses base info");
			return null;

		} catch (Exception e) {
			log.error(
					"Failed to retrieve instructor courses base info from cache for instructor: {}",
					instructorId,
					e);
			return null;
		}
	}

	/** Stores instructor courses base info in cache */
	public void storeInstructorCoursesBaseInfo(
			String instructorId,
			Pageable pageable,
			String search,
			CourseReviewStatus.ReviewStatus status,
			List<String> categoryIds,
			Double minPrice,
			Double maxPrice,
			Integer rating,
			CourseLevel level,
			Boolean isPublished,
			PaginatedResponse<InstructorCourseBaseCacheDto> courseData) {

		try {
			String cacheKey = buildInstructorCoursesBaseCacheKey(
					instructorId,
					pageable,
					search,
					status,
					categoryIds,
					minPrice,
					maxPrice,
					rating,
					level,
					isPublished);

			log.debug(
					"Caching instructor courses base info with key: {} - {} courses",
					cacheKey,
					courseData.getContent().size());

			cacheService.store(cacheKey, courseData, BASE_INFO_TTL);
			log.debug(
					"Successfully cached instructor courses base info for instructor: {}", instructorId);

		} catch (Exception e) {
			log.error("Failed to cache instructor courses base info for instructor: {}", instructorId, e);
		}
	}

	/**
	 * Gets cached dynamic course info for multiple courses Dynamic info includes:
	 * enrollmentCount,
	 * averageRating, revenue, sectionCount, lastContentUpdate, permissions
	 * (canEdit, canDelete, etc.)
	 */
	public Map<String, InstructorCourseDynamicCacheDto> getInstructorCoursesDynamicInfo(
			Set<String> courseIds) {
		// Filter out null course IDs to prevent NullPointerException in
		// Collectors.toMap
		Set<String> validCourseIds = courseIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());

		if (validCourseIds.size() != courseIds.size()) {
			log.warn(
					"Filtered out {} null course IDs from dynamic info lookup",
					courseIds.size() - validCourseIds.size());
		}

		// Create a map by filtering out null values from getSingleCourseDynamicInfo
		Map<String, InstructorCourseDynamicCacheDto> dynamicInfoMap = new HashMap<>();
		int nullValueCount = 0;

		for (String courseId : validCourseIds) {
			InstructorCourseDynamicCacheDto dynamicInfo = getSingleCourseDynamicInfo(courseId);
			if (dynamicInfo != null) {
				dynamicInfoMap.put(courseId, dynamicInfo);
			} else {
				nullValueCount++;
			}
		}

		if (nullValueCount > 0) {
			log.warn("Filtered out {} courses with null dynamic info from cache lookup", nullValueCount);
		}

		return dynamicInfoMap;
	}

	/** Gets cached dynamic info for a single course */
	private InstructorCourseDynamicCacheDto getSingleCourseDynamicInfo(String courseId) {
		try {
			String cacheKey = buildCourseDynamicCacheKey(courseId);
			log.debug("Attempting to retrieve course dynamic info from cache with key: {}", cacheKey);

			InstructorCourseDynamicCacheDto cachedData = (InstructorCourseDynamicCacheDto) cacheService.get(cacheKey);

			if (cachedData != null) {
				log.debug("Cache hit for course dynamic info: {}", courseId);
				return cachedData;
			}

			log.debug("Cache miss for course dynamic info: {}", courseId);
			return null;

		} catch (Exception e) {
			log.error("Failed to retrieve course dynamic info from cache for course: {}", courseId, e);
			return null;
		}
	}

	/** Stores dynamic course info in cache */
	public void storeCourseDynamicInfo(String courseId, InstructorCourseDynamicCacheDto dynamicInfo) {
		try {
			String cacheKey = buildCourseDynamicCacheKey(courseId);
			log.debug("Caching course dynamic info with key: {}", cacheKey);

			cacheService.store(cacheKey, dynamicInfo, DYNAMIC_INFO_TTL);
			log.debug("Successfully cached course dynamic info for course: {}", courseId);

		} catch (Exception e) {
			log.error("Failed to cache course dynamic info for course: {}", courseId, e);
		}
	}

	/** Stores multiple courses dynamic info in cache */
	public void storeCoursesDynamicInfo(Map<String, InstructorCourseDynamicCacheDto> dynamicInfoMap) {
		dynamicInfoMap.forEach(this::storeCourseDynamicInfo);
	}

	/** Invalidates all instructor courses cache for a specific instructor */
	public void invalidateInstructorCoursesCache(String instructorId) {
		try {
			String pattern = buildInstructorCachePattern(instructorId);
			log.debug("Invalidating instructor courses cache with pattern: {}", pattern);

			// Get all keys matching the pattern and remove them
			Set<String> keysToRemove = cacheService.getKeys(pattern);
			if (!keysToRemove.isEmpty()) {
				long removedCount = cacheService.remove(keysToRemove);
				log.debug(
						"Successfully invalidated {} instructor courses cache entries for instructor: {}",
						removedCount,
						instructorId);
			} else {
				log.debug("No cache entries found to invalidate for instructor: {}", instructorId);
			}

		} catch (Exception e) {
			log.error(
					"Failed to invalidate instructor courses cache for instructor: {}", instructorId, e);
		}
	}

	/** Invalidates dynamic cache for a specific course */
	public void invalidateCourseDynamicCache(String courseId) {
		try {
			String cacheKey = buildCourseDynamicCacheKey(courseId);
			log.debug("Invalidating course dynamic cache with key: {}", cacheKey);

			boolean removed = cacheService.remove(cacheKey);
			if (removed) {
				log.debug("Successfully invalidated course dynamic cache for course: {}", courseId);
			} else {
				log.debug("No dynamic cache entry found for course: {}", courseId);
			}

		} catch (Exception e) {
			log.error("Failed to invalidate course dynamic cache for course: {}", courseId, e);
		}
	}

	/** Invalidates dynamic cache for multiple courses */
	public void invalidateCoursesDynamicCache(Set<String> courseIds) {
		courseIds.forEach(this::invalidateCourseDynamicCache);
	}

	/**
	 * Builds cache key for instructor courses base info with all filter parameters
	 */
	private String buildInstructorCoursesBaseCacheKey(
			String instructorId,
			Pageable pageable,
			String search,
			CourseReviewStatus.ReviewStatus status,
			List<String> categoryIds,
			Double minPrice,
			Double maxPrice,
			Integer rating,
			CourseLevel level,
			Boolean isPublished) {

		return cacheKeyBuilder.buildInstructorCoursesKey(
				instructorId,
				pageable.getPageNumber(),
				pageable.getPageSize(),
				search,
				status != null ? status.name() : null,
				categoryIds,
				minPrice,
				maxPrice,
				rating,
				level != null ? level.name() : null,
				isPublished,
				"base");
	}

	/** Builds cache key for course dynamic info */
	private String buildCourseDynamicCacheKey(String courseId) {
		return cacheKeyBuilder.buildCourseDynamicKey(courseId);
	}

	/** Builds cache pattern for instructor cache invalidation */
	private String buildInstructorCachePattern(String instructorId) {
		return cacheKeyBuilder.buildInstructorCachePattern(instructorId);
	}
}
