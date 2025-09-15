package project.ktc.springboot_app.cache.keys;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import project.ktc.springboot_app.course.enums.CourseLevel;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * Utility class for building cache keys with consistent naming conventions.
 * This class centralizes cache key generation logic to ensure consistency
 * and maintainability across the application.
 * 
 * Key Design Principles:
 * - Consistent naming: prefix:operation:identifier:parameters
 * - URL-safe characters only
 * - Deterministic key generation for same inputs
 * - Hierarchical structure for easy pattern matching
 * 
 * @author KTC Team
 */
@Component
@Slf4j
public class CacheKeyBuilder {

    private static final String KEY_SEPARATOR = ":";
    private static final String PARAM_SEPARATOR = "_";
    private static final String NULL_VALUE = "null";
    private static final String EMPTY_VALUE = "empty";

    // ==================== Course Cache Keys ====================

    /**
     * Builds cache key for course listing with filters
     * 
     * @param page       page number
     * @param size       page size
     * @param search     search term
     * @param categoryId category filter
     * @param minPrice   minimum price filter
     * @param maxPrice   maximum price filter
     * @param level      course level filter
     * @param sort       sort parameters
     * @return formatted cache key
     */
    public String buildCoursesListKey(int page, int size, String search, String categoryId,
            BigDecimal minPrice, BigDecimal maxPrice, CourseLevel level, String sort) {
        String filters = buildParameterString(search, categoryId, minPrice, maxPrice, level, sort);
        String key = String.format(CacheConstants.COURSES_LIST_PATTERN,
                page,
                size,
                filters);
        log.debug("Built courses list cache key: {}", key);
        return key;
    }

    /**
     * Builds cache key for shared course data
     * 
     * @param page       page number
     * @param size       page size
     * @param search     search term
     * @param categoryId category filter
     * @param minPrice   minimum price filter
     * @param maxPrice   maximum price filter
     * @param level      course level filter
     * @param sort       sort parameters
     * @return formatted cache key
     */
    public String buildSharedCoursesKey(int page, int size, String search, String categoryId,
            BigDecimal minPrice, BigDecimal maxPrice, CourseLevel level, String sort) {
        String key = String.format(CacheConstants.COURSES_SHARED_PATTERN,
                page,
                size,
                buildParameterString(search, categoryId, minPrice, maxPrice, level, sort));
        log.debug("Built shared courses cache key: {}", key);
        return key;
    }

    /**
     * Builds cache key for course detail
     * 
     * @param courseId course identifier
     * @return formatted cache key
     */
    public String buildCourseDetailKey(String courseId) {
        String key = String.format(CacheConstants.COURSE_DETAIL_PATTERN,
                sanitizeValue(courseId));
        log.debug("Built course detail cache key: {}", key);
        return key;
    }

    /**
     * Builds cache key for course slug lookup
     * 
     * @param slug course slug
     * @return formatted cache key
     */
    public String buildCourseSlugKey(String slug) {
        String key = String.format(CacheConstants.COURSE_SLUG_PATTERN,
                sanitizeValue(slug));
        log.debug("Built course slug cache key: {}", key);
        return key;
    }

    /**
     * Builds cache key for course structure
     * 
     * @param courseId course identifier
     * @return formatted cache key
     */
    public String buildCourseStructureKey(String courseId) {
        String key = String.format(CacheConstants.COURSE_STRUCTURE_PATTERN,
                sanitizeValue(courseId));
        log.debug("Built course structure cache key: {}", key);
        return key;
    }

    /**
     * Builds cache key for instructor course listing
     * 
     * @param instructorId instructor identifier
     * @param page         page number
     * @param size         page size
     * @param search       search term
     * @param status       course status filter
     * @param categoryIds  category filters
     * @param minPrice     minimum price filter
     * @param maxPrice     maximum price filter
     * @param rating       rating filter
     * @param level        course level filter
     * @param isPublished  published status filter
     * @param type         cache type (e.g., "base")
     * @return formatted cache key
     */
    public String buildInstructorCoursesKey(String instructorId, int page, int size, String search, String status,
            Object categoryIds, Object minPrice, Object maxPrice, Object rating, String level, Object isPublished,
            String type) {
        String filters = buildParameterString(search, status, categoryIds, minPrice, maxPrice, rating, level,
                isPublished);
        String key = String.format(CacheConstants.INSTRUCTOR_COURSES_PATTERN,
                sanitizeValue(instructorId),
                page,
                size,
                filters);
        log.debug("Built instructor courses cache key: {}", key);
        return key;
    }

    /**
     * Builds cache key for instructor course listing (simple version)
     * 
     * @param instructorId instructor identifier
     * @param page         page number
     * @param size         page size
     * @param search       search term
     * @param status       course status filter
     * @param sort         sort parameters
     * @return formatted cache key
     */
    public String buildInstructorCoursesKey(String instructorId, int page, int size, String search, String status,
            String sort) {
        String filters = buildParameterString(search, status, sort);
        String key = String.format(CacheConstants.INSTRUCTOR_COURSES_PATTERN,
                sanitizeValue(instructorId),
                page,
                size,
                filters);
        log.debug("Built instructor courses cache key: {}", key);
        return key;
    }

    // ==================== Category Cache Keys ====================

    /**
     * Builds cache key for categories list
     * 
     * @return formatted cache key
     */
    public String buildCategoriesListKey() {
        String key = CacheConstants.CATEGORIES_LIST_PATTERN;
        log.debug("Built categories list cache key: {}", key);
        return key;
    }

    /**
     * Builds cache key for category detail
     * 
     * @param categoryId category identifier
     * @return formatted cache key
     */
    public String buildCategoryDetailKey(String categoryId) {
        String key = String.format(CacheConstants.CATEGORY_DETAIL_PATTERN,
                sanitizeValue(categoryId));
        log.debug("Built category detail cache key: {}", key);
        return key;
    }

    // ==================== User Cache Keys ====================

    /**
     * Builds cache key for enrollment status
     * 
     * @param userId    user identifier
     * @param courseIds comma-separated course IDs
     * @return formatted cache key
     */
    public String buildUserEnrollmentStatusKey(String userId, String courseIds) {
        String key = String.format(CacheConstants.USER_ENROLLMENT_STATUS_PATTERN,
                sanitizeValue(userId),
                sanitizeValue(courseIds));
        log.debug("Built user enrollment status cache key: {}", key);
        return key;
    }

    /**
     * Builds cache key for current user profile detail
     * 
     * @param userEmail user email identifier
     * @return formatted cache key
     */
    public String buildUserProfileDetailKey(String userEmail) {
        String key = String.format(CacheConstants.USER_PROFILE_DETAIL_PATTERN,
                sanitizeValue(userEmail));
        log.debug("Built user profile detail cache key: {}", key);
        return key;
    }

    // ==================== Reviews Cache Keys ====================

    /**
     * Builds cache key for course reviews
     * 
     * @param courseId course identifier
     * @param page     page number
     * @param size     page size
     * @param sort     sort parameters
     * @return formatted cache key
     */
    public String buildCourseReviewsKey(String courseId, int page, int size, String sort) {
        String key = String.format(CacheConstants.COURSE_REVIEWS_PATTERN,
                sanitizeValue(courseId),
                page,
                size,
                sanitizeValue(sort));
        log.debug("Built course reviews cache key: {}", key);
        return key;
    }

    /**
     * Builds cache key for course reviews (without sort parameter for backward
     * compatibility)
     * 
     * @param courseId course identifier
     * @param page     page number
     * @param size     page size
     * @return formatted cache key
     */
    public String buildCourseReviewsKey(String courseId, int page, int size) {
        return buildCourseReviewsKey(courseId, page, size, "default");
    }

    /**
     * Builds cache key for course review statistics
     * 
     * @param courseId course identifier
     * @return formatted cache key
     */
    public String buildCourseReviewStatsKey(String courseId) {
        String key = String.format(CacheConstants.COURSE_REVIEW_STATS_PATTERN,
                sanitizeValue(courseId));
        log.debug("Built course review stats cache key: {}", key);
        return key;
    }

    // ==================== Instructor Statistics Cache Keys ====================

    /**
     * Builds cache key for instructor statistics
     * 
     * @param instructorId instructor identifier
     * @return formatted cache key
     */
    public String buildInstructorStatisticsKey(String instructorId) {
        String key = String.format(CacheConstants.INSTRUCTOR_STATISTICS_PATTERN,
                sanitizeValue(instructorId));
        log.debug("Built instructor statistics cache key: {}", key);
        return key;
    }

    /**
     * Builds cache key for instructor courses statistics
     * 
     * @param instructorId instructor identifier
     * @return formatted cache key
     */
    public String buildInstructorCoursesStatisticsKey(String instructorId) {
        String key = String.format(CacheConstants.INSTRUCTOR_COURSES_STATISTICS_PATTERN,
                sanitizeValue(instructorId));
        log.debug("Built instructor courses statistics cache key: {}", key);
        return key;
    }

    /**
     * Builds cache key for instructor earnings statistics
     * 
     * @param instructorId instructor identifier
     * @return formatted cache key
     */
    public String buildInstructorEarningsStatisticsKey(String instructorId) {
        String key = String.format(CacheConstants.INSTRUCTOR_EARNINGS_STATISTICS_PATTERN,
                sanitizeValue(instructorId));
        log.debug("Built instructor earnings statistics cache key: {}", key);
        return key;
    }

    /**
     * Builds cache key for instructor students statistics
     * 
     * @param instructorId instructor identifier
     * @return formatted cache key
     */
    public String buildInstructorStudentsStatisticsKey(String instructorId) {
        String key = String.format(CacheConstants.INSTRUCTOR_STUDENTS_STATISTICS_PATTERN,
                sanitizeValue(instructorId));
        log.debug("Built instructor students statistics cache key: {}", key);
        return key;
    }

    /**
     * Builds cache key for instructor reviews statistics
     * 
     * @param instructorId instructor identifier
     * @return formatted cache key
     */
    public String buildInstructorReviewsStatisticsKey(String instructorId) {
        String key = String.format(CacheConstants.INSTRUCTOR_REVIEWS_STATISTICS_PATTERN,
                sanitizeValue(instructorId));
        log.debug("Built instructor reviews statistics cache key: {}", key);
        return key;
    }

    /**
     * Builds cache key for course dynamic data
     * 
     * @param courseId course identifier
     * @return formatted cache key
     */
    public String buildCourseDynamicKey(String courseId) {
        String key = String.format(CacheConstants.COURSE_DYNAMIC_PATTERN,
                sanitizeValue(courseId));
        log.debug("Built course dynamic cache key: {}", key);
        return key;
    }

    // ==================== Pattern Builder Methods ====================

    /**
     * Builds pattern for course-related cache invalidation
     * 
     * @param courseId course identifier
     * @return pattern for cache invalidation
     */
    public String buildCoursePattern(String courseId) {
        String pattern = CacheConstants.COURSES_CACHE_PREFIX + "*" + sanitizeValue(courseId) + "*";
        log.debug("Built course invalidation pattern: {}", pattern);
        return pattern;
    }

    /**
     * Builds pattern for category-related cache invalidation
     * 
     * @param categoryId category identifier (optional)
     * @return pattern for cache invalidation
     */
    public String buildCategoryPattern(String categoryId) {
        if (categoryId != null && !categoryId.trim().isEmpty()) {
            String pattern = CacheConstants.CATEGORIES_CACHE_PREFIX + "*" + sanitizeValue(categoryId) + "*";
            log.debug("Built category invalidation pattern: {}", pattern);
            return pattern;
        } else {
            String pattern = CacheConstants.CATEGORIES_CACHE_PREFIX + "*";
            log.debug("Built all categories invalidation pattern: {}", pattern);
            return pattern;
        }
    }

    /**
     * Builds pattern for user-related cache invalidation
     * 
     * @param userEmail user email identifier
     * @return pattern for cache invalidation
     */
    public String buildUserPattern(String userEmail) {
        String pattern = CacheConstants.USERS_CACHE_PREFIX + "*" + sanitizeValue(userEmail) + "*";
        log.debug("Built user invalidation pattern: {}", pattern);
        return pattern;
    }

    /**
     * Builds pattern for enrollment-related cache invalidation
     * 
     * @param userId user identifier
     * @return pattern for cache invalidation
     */
    public String buildEnrollmentPattern(String userId) {
        String pattern = CacheConstants.ENROLLMENTS_CACHE_PREFIX + "*" + sanitizeValue(userId) + "*";
        log.debug("Built enrollment invalidation pattern: {}", pattern);
        return pattern;
    }

    /**
     * Builds pattern for review-related cache invalidation
     * 
     * @param courseId course identifier
     * @return pattern for cache invalidation
     */
    public String buildReviewPattern(String courseId) {
        String pattern = CacheConstants.REVIEWS_CACHE_PREFIX + "*" + sanitizeValue(courseId) + "*";
        log.debug("Built review invalidation pattern: {}", pattern);
        return pattern;
    }

    /**
     * Builds pattern for instructor statistics cache invalidation
     * 
     * @param instructorId instructor identifier
     * @return pattern for cache invalidation
     */
    public String buildInstructorStatisticsPattern(String instructorId) {
        String pattern = CacheConstants.INSTRUCTOR_STATISTICS_CACHE_PREFIX + "*" + sanitizeValue(instructorId) + "*";
        log.debug("Built instructor statistics invalidation pattern: {}", pattern);
        return pattern;
    }

    /**
     * Builds pattern for instructor cache invalidation
     * 
     * @param instructorId instructor identifier
     * @return pattern for cache invalidation
     */
    public String buildInstructorCachePattern(String instructorId) {
        String pattern = CacheConstants.INSTRUCTOR_COURSES_CACHE_PREFIX + "*" + sanitizeValue(instructorId) + "*";
        log.debug("Built instructor cache invalidation pattern: {}", pattern);
        return pattern;
    }

    // ==================== Utility Methods ====================

    /**
     * Sanitizes cache key values to ensure safe key generation
     * 
     * @param value the value to sanitize
     * @return sanitized value safe for use in cache keys
     */
    private String sanitizeValue(Object value) {
        if (value == null) {
            return NULL_VALUE;
        }

        String stringValue = value.toString().trim();
        if (stringValue.isEmpty()) {
            return EMPTY_VALUE;
        }

        // Replace unsafe characters with underscores
        // Keep alphanumeric, hyphens, underscores, periods
        return stringValue.replaceAll("[^a-zA-Z0-9\\-_.]", PARAM_SEPARATOR);
    }

    /**
     * Builds a deterministic sort key from Pageable
     * 
     * @param pageable the pageable object
     * @return deterministic sort string
     */
    private String buildSortKey(Pageable pageable) {
        if (pageable == null || pageable.getSort().isEmpty()) {
            return "unsorted";
        }

        return pageable.getSort().stream()
                .map(order -> order.getProperty() + "_" + order.getDirection())
                .collect(Collectors.joining(PARAM_SEPARATOR));
    }

    /**
     * Creates a safe parameter string from multiple values
     * 
     * @param values the values to join
     * @return safe parameter string
     */
    private String buildParameterString(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(PARAM_SEPARATOR);
            }
            sb.append(sanitizeValue(values[i]));
        }
        return sb.toString();
    }
}