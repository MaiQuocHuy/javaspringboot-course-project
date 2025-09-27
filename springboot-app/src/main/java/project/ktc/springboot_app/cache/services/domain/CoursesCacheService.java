package project.ktc.springboot_app.cache.services.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import project.ktc.springboot_app.cache.interfaces.CacheService;
import project.ktc.springboot_app.cache.keys.CacheConstants;
import project.ktc.springboot_app.cache.keys.CacheKeyBuilder;
import project.ktc.springboot_app.course.dto.CourseResponseDto;
import project.ktc.springboot_app.course.dto.cache.SharedCourseCacheDto;
import project.ktc.springboot_app.course.enums.CourseLevel;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Set;

/**
 * Course-specific cache service that provides high-level caching operations for
 * courses.
 * Handles course listing, details, and invalidation using the underlying cache
 * service.
 * 
 * @author KTC Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CoursesCacheService {

    private final CacheService cacheService;
    private final CacheKeyBuilder cacheKeyBuilder;

    /**
     * Stores paginated course list response in cache
     */
    public void storeCoursesList(Pageable pageable, String categoryId, String search,
            CourseLevel level, BigDecimal minPrice, BigDecimal maxPrice,
            String sort, Object paginatedResponse) {
        try {
            String cacheKey = cacheKeyBuilder.buildCoursesListKey(
                    pageable.getPageNumber(), pageable.getPageSize(), search, categoryId,
                    minPrice, maxPrice, level, sort);

            log.debug("Caching courses list with key: {}", cacheKey);
            cacheService.store(cacheKey, paginatedResponse, CacheConstants.COURSES_DEFAULT_TTL);

        } catch (Exception e) {
            log.error("Failed to cache courses list", e);
            // Don't throw exception - caching failure shouldn't break the application
        }
    }

    /**
     * Retrieves paginated course list response from cache
     */
    @SuppressWarnings("unchecked")
    public <T> T getCoursesList(Pageable pageable, String categoryId,
            String search, CourseLevel level,
            BigDecimal minPrice, BigDecimal maxPrice,
            String sort) {
        try {
            String cacheKey = cacheKeyBuilder.buildCoursesListKey(
                    pageable.getPageNumber(), pageable.getPageSize(), search, categoryId,
                    minPrice, maxPrice, level, sort);

            log.debug("Retrieving courses list from cache with key: {}", cacheKey);
            return (T) cacheService.get(cacheKey);

        } catch (Exception e) {
            log.error("Failed to retrieve courses list from cache", e);
            return null; // Cache miss or error
        }
    }

    /**
     * Stores course details in cache
     */
    public void storeCourseDetails(String courseId, CourseResponseDto courseDetails) {
        try {
            String cacheKey = cacheKeyBuilder.buildCourseDetailKey(courseId);
            log.debug("Caching course details with key: {}", cacheKey);
            cacheService.store(cacheKey, courseDetails, CacheConstants.COURSE_DETAILS_TTL);

        } catch (Exception e) {
            log.error("Failed to cache course details for course: {}", courseId, e);
        }
    }

    /**
     * Retrieves course details from cache
     */
    public CourseResponseDto getCourseDetails(String courseId) {
        try {
            String cacheKey = cacheKeyBuilder.buildCourseDetailKey(courseId);
            log.debug("Retrieving course details from cache with key: {}", cacheKey);
            return cacheService.get(cacheKey, CourseResponseDto.class);

        } catch (Exception e) {
            log.error("Failed to retrieve course details from cache for course: {}", courseId, e);
            return null;
        }
    }

    /**
     * Stores course by slug lookup in cache
     */
    public void storeCourseBySlug(String slug, CourseResponseDto course) {
        try {
            String cacheKey = cacheKeyBuilder.buildCourseSlugKey(slug);
            log.debug("Caching course by slug with key: {}", cacheKey);
            cacheService.store(cacheKey, course, CacheConstants.COURSE_DETAILS_TTL);

        } catch (Exception e) {
            log.error("Failed to cache course by slug: {}", slug, e);
        }
    }

    /**
     * Retrieves course by slug from cache
     */
    public CourseResponseDto getCourseBySlug(String slug) {
        try {
            String cacheKey = cacheKeyBuilder.buildCourseSlugKey(slug);
            log.debug("Retrieving course by slug from cache with key: {}", cacheKey);
            return cacheService.get(cacheKey, CourseResponseDto.class);

        } catch (Exception e) {
            log.error("Failed to retrieve course by slug from cache: {}", slug, e);
            return null;
        }
    }

    /**
     * Stores shared course data in cache (non-user-specific)
     */
    public void storeSharedCoursesData(Pageable pageable, String categoryId,
            String search, CourseLevel level,
            BigDecimal minPrice, BigDecimal maxPrice,
            String sort, SharedCourseCacheDto sharedData) {
        try {
            String cacheKey = cacheKeyBuilder.buildSharedCoursesKey(
                    pageable.getPageNumber(), pageable.getPageSize(),
                    search, categoryId, minPrice, maxPrice, level, sort);

            log.debug("Caching shared courses data with key: {}", cacheKey);
            cacheService.store(cacheKey, sharedData, CacheConstants.COURSES_SHARED_TTL);

        } catch (Exception e) {
            log.error("Failed to cache shared courses data", e);
        }
    }

    /**
     * Retrieves shared course data from cache
     */
    public SharedCourseCacheDto getSharedCoursesData(Pageable pageable, String categoryId,
            String search, CourseLevel level,
            BigDecimal minPrice, BigDecimal maxPrice,
            String sort) {
        try {
            String cacheKey = cacheKeyBuilder.buildSharedCoursesKey(
                    pageable.getPageNumber(), pageable.getPageSize(),
                    search, categoryId, minPrice, maxPrice, level, sort);

            log.debug("Retrieving shared courses data from cache with key: {}", cacheKey);
            return cacheService.get(cacheKey, SharedCourseCacheDto.class);

        } catch (Exception e) {
            log.error("Failed to retrieve shared courses data from cache", e);
            return null;
        }
    }

    /**
     * Invalidates all course-related cache entries
     */
    public void invalidateAllCoursesCaches() {
        try {
            log.debug("Invalidating all courses cache entries");
            Set<String> keys = cacheService.getKeys(CacheConstants.COURSES_INVALIDATION_PATTERN);

            if (!keys.isEmpty()) {
                cacheService.remove(keys);
                log.debug("Invalidated {} course cache entries", keys.size());
            } else {
                log.debug("No course cache entries found to invalidate");
            }

        } catch (Exception e) {
            log.error("Failed to invalidate courses cache", e);
        }
    }

    /**
     * Invalidates course-specific cache entries
     */
    public void invalidateCourseCache(String courseId) {
        try {
            log.debug("Invalidating cache for course: {}", courseId);

            // Invalidate course details
            String detailKey = cacheKeyBuilder.buildCourseDetailKey(courseId);
            cacheService.remove(detailKey);

            // Invalidate course structure
            String structureKey = cacheKeyBuilder.buildCourseStructureKey(courseId);
            cacheService.remove(structureKey);

            // Invalidate all courses list cache (since course might appear in lists)
            invalidateAllCoursesCaches();

            log.debug("Successfully invalidated cache for course: {}", courseId);

        } catch (Exception e) {
            log.error("Failed to invalidate cache for course: {}", courseId, e);
        }
    }

    /**
     * Invalidates course cache by slug
     */
    public void invalidateCourseBySlug(String slug) {
        try {
            String cacheKey = cacheKeyBuilder.buildCourseSlugKey(slug);
            log.debug("Invalidating course cache by slug: {}", slug);
            cacheService.remove(cacheKey);

        } catch (Exception e) {
            log.error("Failed to invalidate course cache by slug: {}", slug, e);
        }
    }

    /**
     * Warm up cache with popular courses
     */
    public void warmUpCache(Duration warmUpDuration) {
        try {
            log.info("Starting courses cache warm-up for duration: {}", warmUpDuration);
            // Implementation would pre-load popular courses
            // This is a placeholder for warm-up logic
            log.info("Courses cache warm-up completed");

        } catch (Exception e) {
            log.error("Failed to warm up courses cache", e);
        }
    }

    /**
     * Get cache statistics for courses
     */
    public void logCacheStatistics() {
        try {
            Set<String> keys = cacheService.getKeys(CacheConstants.COURSES_CACHE_PREFIX + ":*");
            log.info("Courses cache contains {} entries", keys.size());

        } catch (Exception e) {
            log.error("Failed to get courses cache statistics", e);
        }
    }

    /**
     * Stores course structure in cache
     */
    public void storeCourseStructure(String courseId, Object courseStructure) {
        try {
            String cacheKey = cacheKeyBuilder.buildCourseStructureKey(courseId);
            log.debug("Caching course structure with key: {}", cacheKey);
            cacheService.store(cacheKey, courseStructure, CacheConstants.COURSE_STRUCTURE_TTL);

        } catch (Exception e) {
            log.error("Failed to cache course structure for course: {}", courseId, e);
        }
    }

    /**
     * Retrieves course structure from cache
     */
    @SuppressWarnings("unchecked")
    public <T> T getCourseStructure(String courseId) {
        try {
            String cacheKey = cacheKeyBuilder.buildCourseStructureKey(courseId);
            log.debug("Retrieving course structure from cache with key: {}", cacheKey);
            return (T) cacheService.get(cacheKey);

        } catch (Exception e) {
            log.error("Failed to retrieve course structure from cache for course: {}", courseId, e);
            return null;
        }
    }

    /**
     * Invalidates course structure cache
     */
    public void invalidateCourseStructure(String courseId) {
        try {
            String cacheKey = cacheKeyBuilder.buildCourseStructureKey(courseId);
            log.debug("Invalidating course structure cache for course: {}", courseId);
            cacheService.remove(cacheKey);

        } catch (Exception e) {
            log.error("Failed to invalidate course structure cache for course: {}", courseId, e);
        }
    }

    // ==================== Additional Methods for Compatibility
    // ====================

    /**
     * Stores shared course data in cache
     */
    public void storeSharedCourseData(int page, int size, String search, String categoryId,
            BigDecimal minPrice, BigDecimal maxPrice, CourseLevel level, String sort,
            SharedCourseCacheDto sharedData) {
        try {
            String cacheKey = cacheKeyBuilder.buildSharedCoursesKey(
                    page, size, search, categoryId, minPrice, maxPrice, level, sort);

            log.debug("Caching shared course data with key: {}", cacheKey);
            cacheService.store(cacheKey, sharedData, CacheConstants.COURSES_SHARED_TTL);

        } catch (Exception e) {
            log.error("Failed to cache shared course data", e);
        }
    }

    /**
     * Retrieves shared course data from cache
     */
    public SharedCourseCacheDto getSharedCourseData(int page, int size,
            String search, String categoryId,
            BigDecimal minPrice, BigDecimal maxPrice, CourseLevel level, String sort) {
        try {
            String cacheKey = cacheKeyBuilder.buildSharedCoursesKey(
                    page, size, search, categoryId, minPrice, maxPrice, level, sort);

            log.debug("Retrieving shared course data from cache with key: {}", cacheKey);
            return cacheService.get(cacheKey, SharedCourseCacheDto.class);

        } catch (Exception e) {
            log.error("Failed to retrieve shared course data from cache", e);
            return null;
        }
    }

    /**
     * Stores user-specific enrollment status in cache
     */
    public void storeUserEnrollmentStatus(String userId, String courseIdsKey,
            java.util.Map<String, Boolean> enrollmentStatus) {
        try {
            String cacheKey = cacheKeyBuilder.buildUserEnrollmentStatusKey(userId, courseIdsKey);

            log.debug("Caching user enrollment status with key: {}", cacheKey);
            cacheService.store(cacheKey, enrollmentStatus, CacheConstants.USER_ENROLLMENT_TTL);

        } catch (Exception e) {
            log.error("Failed to cache user enrollment status for userId: {}", userId, e);
        }
    }

    /**
     * Retrieves user-specific enrollment status from cache
     */
    @SuppressWarnings("unchecked")
    public java.util.Map<String, Boolean> getUserEnrollmentStatus(String userId, String courseIdsKey) {
        try {
            String cacheKey = cacheKeyBuilder.buildUserEnrollmentStatusKey(userId, courseIdsKey);

            log.debug("Retrieving user enrollment status from cache with key: {}", cacheKey);
            return (java.util.Map<String, Boolean>) cacheService.get(cacheKey);

        } catch (Exception e) {
            log.error("Failed to retrieve user enrollment status from cache for userId: {}", userId, e);
            return null;
        }
    }

    /**
     * Stores course details in cache by slug
     */
    public void storeCourseDetailsBySlug(String slug, Object courseDetails) {
        try {
            String cacheKey = cacheKeyBuilder.buildCourseSlugKey(slug);

            log.info("Caching course details by slug with key: {}", cacheKey);
            cacheService.store(cacheKey, courseDetails, CacheConstants.COURSES_SHARED_TTL);

        } catch (Exception e) {
            log.error("Failed to cache course details by slug: {}", slug, e);
        }
    }

    /**
     * Retrieves course details from cache by slug
     */
    public <T> T getCourseDetailsBySlug(String slug, Class<T> targetClass) {
        try {
            String cacheKey = cacheKeyBuilder.buildCourseSlugKey(slug);

            log.debug("Retrieving course details by slug with key: {}", cacheKey);
            Object cachedData = cacheService.get(cacheKey);

            if (cachedData != null) {
                return targetClass.cast(cachedData);
            }
            return null;

        } catch (Exception e) {
            log.error("Failed to retrieve course details by slug: {}", slug, e);
            return null;
        }
    }

    /**
     * Invalidates all courses cache - compatibility method
     */
    public void invalidateAllCoursesCache() {
        // Call the new method name for consistency
        invalidateAllCoursesCaches();
    }

    /**
     * Invalidates course cache by both course ID and slug
     */
    public void invalidateCourseByIdAndSlug(String courseId, String slug) {
        try {
            // Invalidate by course ID
            invalidateCourseCache(courseId);

            // Invalidate by slug
            invalidateCourseBySlug(slug);

            log.info("Invalidated course cache for courseId: {} and slug: {}", courseId, slug);

        } catch (Exception e) {
            log.error("Failed to invalidate course cache for courseId: {} and slug: {}", courseId, slug, e);
        }
    }

    /**
     * Invalidates cache when enrollment changes occur (always refresh for
     * consistency)
     */
    public void checkAndInvalidateForEnrollmentChange(String courseId, String slug,
            long previousCount, long newCount) {
        try {
            // Always invalidate cache when enrollment count changes
            if (previousCount != newCount) {
                log.info("Enrollment change detected for course {} (from {} to {}). Invalidating cache.",
                        courseId, previousCount, newCount);

                // Invalidate individual course cache (detail + slug)
                invalidateCourseByIdAndSlug(courseId, slug);

                // Invalidate all shared course list caches to ensure consistent enrollment
                // counts
                // This includes findAllPublic API cache which contains enrollment counts
                invalidateAllCoursesCaches();

                log.info("Successfully invalidated all course caches for enrollment change in course: {}", courseId);
            } else {
                log.debug("No enrollment change detected for course: {}", courseId);
            }

        } catch (Exception e) {
            log.error("Failed to invalidate cache for enrollment change in course: {}", courseId, e);
        }
    }

    /**
     * Invalidates user-specific enrollment status cache
     * This ensures that enrollment status is immediately updated after purchase
     */
    public void invalidateUserEnrollmentStatus(String userId) {
        try {
            log.debug("Invalidating user enrollment status cache for user: {}", userId);

            // Build pattern to match all enrollment status keys for this user
            String pattern = cacheKeyBuilder.buildUserEnrollmentStatusPattern(userId);

            // Find all cache keys matching the pattern
            Set<String> keysToDelete = cacheService.getKeys(pattern);

            if (!keysToDelete.isEmpty()) {
                // Delete all matching cache entries
                long deletedCount = cacheService.remove(keysToDelete);
                log.debug("Invalidated {} user enrollment status cache entries for user: {}",
                        deletedCount, userId);
            } else {
                log.debug("No user enrollment status cache entries found to invalidate for user: {}", userId);
            }

        } catch (Exception e) {
            log.error("Failed to invalidate user enrollment status cache for user: {}", userId, e);
        }
    }

    /**
     * Invalidates all course-related cache after enrollment change
     * This ensures consistent enrollment counts across all course APIs
     */
    public void invalidateCacheForEnrollmentChange(String courseId, String slug) {
        try {
            log.info("Invalidating cache after enrollment change for course: {}", courseId);

            // Invalidate individual course cache (detail + slug)
            invalidateCourseByIdAndSlug(courseId, slug);

            // Invalidate all shared course list caches to ensure consistent enrollment
            // counts
            // This includes findAllPublic API cache which contains enrollment counts
            invalidateAllCoursesCaches();

            log.info("Successfully invalidated all course caches after enrollment change for course: {}", courseId);

        } catch (Exception e) {
            log.error("Failed to invalidate cache after enrollment change for course: {}", courseId, e);
        }
    }
}