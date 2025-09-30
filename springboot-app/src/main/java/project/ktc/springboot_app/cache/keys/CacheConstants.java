package project.ktc.springboot_app.cache.keys;

import java.time.Duration;

/**
 * Central repository for all cache-related constants. This class ensures
 * consistency across the
 * application for cache prefixes, TTL values, and other cache configuration
 * constants.
 *
 * @author KTC Team
 */
public final class CacheConstants {

	// Prevent instantiation
	private CacheConstants() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	// ==================== Cache Prefixes ====================

	/** Cache prefix for course-related data */
	public static final String COURSES_CACHE_PREFIX = "courses";

	/** Cache prefix for user-related data */
	public static final String USERS_CACHE_PREFIX = "users";

	/** Cache prefix for enrollment data */
	public static final String ENROLLMENTS_CACHE_PREFIX = "enrollments";

	/** Cache prefix for reviews data */
	public static final String REVIEWS_CACHE_PREFIX = "reviews";

	/** Cache prefix for categories data */
	public static final String CATEGORIES_CACHE_PREFIX = "categories";

	/** Cache prefix for instructor statistics data */
	public static final String INSTRUCTOR_STATISTICS_CACHE_PREFIX = "instructor_statistics";

	/** Cache prefix for instructor courses data */
	public static final String INSTRUCTOR_COURSES_CACHE_PREFIX = "instructor_courses";

	// ==================== Cache TTL Values ====================

	/** Default TTL for course listing cache (5 minutes) */
	public static final Duration COURSES_DEFAULT_TTL = Duration.ofMinutes(5);

	/**
	 * TTL for shared course data cache (30 minutes) Used for courses + categories +
	 * enrollmentCounts
	 * (non-user-specific data)
	 */
	public static final Duration COURSES_SHARED_TTL = Duration.ofMinutes(30);

	/** TTL for user-specific enrollment status cache (5 minutes) */
	public static final Duration USER_ENROLLMENT_TTL = Duration.ofMinutes(5);

	/** TTL for course details cache (10 minutes) */
	public static final Duration COURSE_DETAILS_TTL = Duration.ofMinutes(10);

	/**
	 * TTL for course structure cache (30 minutes) Course structure includes
	 * sections, lessons,
	 * videos, and quiz questions
	 */
	public static final Duration COURSE_STRUCTURE_TTL = Duration.ofMinutes(30);

	/** TTL for user profile cache (15 minutes) */
	public static final Duration USER_PROFILE_TTL = Duration.ofMinutes(15);

	/**
	 * TTL for public user profile detail cache (3 hours - for GET
	 * /api/users/profile/{id}) Public
	 * profiles change less frequently and can be cached longer
	 */
	public static final Duration USER_PROFILE_DETAIL_TTL = Duration.ofHours(3);

	/** TTL for categories cache (1 hour - relatively static data) */
	public static final Duration CATEGORIES_TTL = Duration.ofHours(1);

	/** TTL for enrollment data (5 minutes) */
	public static final Duration ENROLLMENTS_TTL = Duration.ofMinutes(5);

	/** TTL for course reviews cache (10 minutes) */
	public static final Duration REVIEWS_DEFAULT_TTL = Duration.ofMinutes(10);

	/**
	 * TTL for instructor statistics cache (15 minutes) Statistics data is
	 * aggregated from multiple
	 * sources and changes less frequently
	 */
	public static final Duration INSTRUCTOR_STATISTICS_TTL = Duration.ofMinutes(15);

	/**
	 * TTL for instructor courses base info cache (7 minutes) Base course info
	 * changes less frequently
	 */
	public static final Duration INSTRUCTOR_COURSES_BASE_TTL = Duration.ofMinutes(7);

	/**
	 * TTL for instructor courses dynamic info cache (45 seconds) Dynamic info like
	 * enrollments and
	 * ratings change frequently
	 */
	public static final Duration INSTRUCTOR_COURSES_DYNAMIC_TTL = Duration.ofSeconds(45);

	// ==================== Cache Key Patterns ====================

	/**
	 * Pattern for course listing cache keys Format:
	 * courses:page:0:size:10:filters:search_category_price_level_sort
	 */
	public static final String COURSES_LIST_PATTERN = COURSES_CACHE_PREFIX + ":page:%d:size:%d:filters:%s";

	/**
	 * Pattern for shared course data cache keys (non-user-specific data) Format:
	 * courses:shared:page:0:size:10:filters:search_category_price_level_sort
	 */
	public static final String COURSES_SHARED_PATTERN = COURSES_CACHE_PREFIX + ":shared:page:%d:size:%d:filters:%s";

	/**
	 * Pattern for user-specific enrollment status cache Format:
	 * enrollments:user:user-id:courses:course-id1,course-id2,...
	 */
	public static final String USER_ENROLLMENT_STATUS_PATTERN = ENROLLMENTS_CACHE_PREFIX + ":user:%s:courses:%s";

	/** Pattern for current user profile cache Format: users:profile:user-email */
	public static final String USER_PROFILE_DETAIL_PATTERN = USERS_CACHE_PREFIX + ":profile:%s";

	/** Pattern for course detail cache keys Format: courses:detail:course-id */
	public static final String COURSE_DETAIL_PATTERN = COURSES_CACHE_PREFIX + ":detail:%s";

	/** Pattern for course slug lookup Format: courses:slug:course-slug */
	public static final String COURSE_SLUG_PATTERN = COURSES_CACHE_PREFIX + ":slug:%s";

	/** Pattern for course structure cache Format: courses:structure:course-id */
	public static final String COURSE_STRUCTURE_PATTERN = COURSES_CACHE_PREFIX + ":structure:%s";

	/**
	 * Pattern for instructor course listing Format:
	 * instructor_courses:instructor:instructor-id:page:0:size:10:filters:search_status_sort
	 */
	public static final String INSTRUCTOR_COURSES_PATTERN = INSTRUCTOR_COURSES_CACHE_PREFIX
			+ ":instructor:%s:page:%d:size:%d:filters:%s";

	/** Pattern for categories list cache Format: categories:list */
	public static final String CATEGORIES_LIST_PATTERN = CATEGORIES_CACHE_PREFIX + ":list";

	/** Pattern for category detail cache Format: categories:detail:category-id */
	public static final String CATEGORY_DETAIL_PATTERN = CATEGORIES_CACHE_PREFIX + ":detail:%s";

	/**
	 * Pattern for course review statistics Format: reviews:course:course-id:stats
	 */
	public static final String COURSE_REVIEW_STATS_PATTERN = REVIEWS_CACHE_PREFIX + ":course:%s:stats";

	/**
	 * Pattern for instructor courses statistics Format:
	 * instructor_statistics:instructor:instructor-id:courses
	 */
	public static final String INSTRUCTOR_COURSES_STATISTICS_PATTERN = INSTRUCTOR_STATISTICS_CACHE_PREFIX
			+ ":instructor:%s:courses";

	/**
	 * Pattern for instructor earnings statistics Format:
	 * instructor_statistics:instructor:instructor-id:earnings
	 */
	public static final String INSTRUCTOR_EARNINGS_STATISTICS_PATTERN = INSTRUCTOR_STATISTICS_CACHE_PREFIX
			+ ":instructor:%s:earnings";

	/**
	 * Pattern for instructor students statistics Format:
	 * instructor_statistics:instructor:instructor-id:students
	 */
	public static final String INSTRUCTOR_STUDENTS_STATISTICS_PATTERN = INSTRUCTOR_STATISTICS_CACHE_PREFIX
			+ ":instructor:%s:students";

	/**
	 * Pattern for instructor reviews statistics Format:
	 * instructor_statistics:instructor:instructor-id:reviews
	 */
	public static final String INSTRUCTOR_REVIEWS_STATISTICS_PATTERN = INSTRUCTOR_STATISTICS_CACHE_PREFIX
			+ ":instructor:%s:reviews";

	/**
	 * Pattern for user enrollment cache Format:
	 * enrollments:user:user-id:page:0:size:10
	 */
	public static final String USER_ENROLLMENTS_PATTERN = ENROLLMENTS_CACHE_PREFIX + ":user:%s:page:%d:size:%d";

	/**
	 * Pattern for course reviews cache Format:
	 * reviews:course:course-id:page:0:size:10:sort:sort-params
	 */
	public static final String COURSE_REVIEWS_PATTERN = REVIEWS_CACHE_PREFIX + ":course:%s:page:%d:size:%d:sort:%s";

	/**
	 * Pattern for instructor statistics cache Format:
	 * instructor_statistics:instructor:instructor-id
	 */
	public static final String INSTRUCTOR_STATISTICS_PATTERN = INSTRUCTOR_STATISTICS_CACHE_PREFIX + ":instructor:%s";

	/**
	 * Pattern for instructor courses base info cache Format:
	 * instructor_courses:instructor:id:base:page:0:size:10:filters:search_status_categories_price_rating_level_published
	 */
	public static final String INSTRUCTOR_COURSES_BASE_PATTERN = INSTRUCTOR_COURSES_CACHE_PREFIX
			+ ":instructor:%s:base:page:%d:size:%d:filters:%s";

	/**
	 * Pattern for course dynamic info cache Format:
	 * instructor_courses:course:course-id:dynamic
	 */
	public static final String COURSE_DYNAMIC_PATTERN = INSTRUCTOR_COURSES_CACHE_PREFIX + ":course:%s:dynamic";

	// ==================== Cache Invalidation Patterns ====================

	/** Pattern to match all course-related cache entries for invalidation */
	public static final String COURSES_INVALIDATION_PATTERN = COURSES_CACHE_PREFIX + ":*";

	/** Pattern to match user-specific enrollments for invalidation */
	public static final String USER_ENROLLMENTS_INVALIDATION_PATTERN = ENROLLMENTS_CACHE_PREFIX + ":user:%s:*";

	/** Pattern to match course-specific reviews for invalidation */
	public static final String COURSE_REVIEWS_INVALIDATION_PATTERN = REVIEWS_CACHE_PREFIX + ":course:%s:*";

	/** Pattern to match instructor statistics for invalidation */
	public static final String INSTRUCTOR_STATISTICS_INVALIDATION_PATTERN = INSTRUCTOR_STATISTICS_CACHE_PREFIX
			+ ":instructor:%s";

	/** Pattern to match instructor courses for invalidation */
	public static final String INSTRUCTOR_COURSES_INVALIDATION_PATTERN = INSTRUCTOR_COURSES_CACHE_PREFIX
			+ ":instructor:%s:*";

	// ==================== Redis Configuration Constants ====================

	/** Default timeout for Redis operations (5 seconds) */
	public static final Duration REDIS_OPERATION_TIMEOUT = Duration.ofSeconds(5);

	/** Maximum number of keys to process in batch operations */
	public static final int MAX_BATCH_SIZE = 1000;

	/** Prefix for application-wide cache namespace */
	public static final String APP_CACHE_PREFIX = "ktc-cache";
}
