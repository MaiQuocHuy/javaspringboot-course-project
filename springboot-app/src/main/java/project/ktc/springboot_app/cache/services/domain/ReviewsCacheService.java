package project.ktc.springboot_app.cache.services.domain;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.cache.interfaces.CacheService;
import project.ktc.springboot_app.cache.keys.CacheConstants;
import project.ktc.springboot_app.cache.keys.CacheKeyBuilder;
import project.ktc.springboot_app.cache.mappers.ReviewCacheMapper;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.review.dto.ReviewResponseDto;
import project.ktc.springboot_app.review.dto.cache.ReviewCacheDto;

/**
 * Review-specific cache service that provides high-level caching operations for
 * course reviews.
 * Handles review listing and invalidation using the underlying cache service.
 *
 * @author KTC Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewsCacheService {

	private final CacheService cacheService;
	private final CacheKeyBuilder cacheKeyBuilder;

	/**
	 * Stores paginated review list response in cache
	 *
	 * @param courseId
	 *            the course identifier
	 * @param pageable
	 *            pagination parameters
	 * @param paginatedResponse
	 *            the paginated review response to cache
	 */
	public void storeCourseReviews(
			String courseId, Pageable pageable, PaginatedResponse<ReviewResponseDto> paginatedResponse) {
		try {
			String sortParam = pageable.getSort().toString();
			String cacheKey = cacheKeyBuilder.buildCourseReviewsKey(
					courseId, pageable.getPageNumber(), pageable.getPageSize(), sortParam);

			log.debug("Caching course reviews with key: {}", cacheKey);

			// Convert to cache DTO using the mapper
			PaginatedResponse<ReviewCacheDto> cacheDto = ReviewCacheMapper.toCachePaginatedDto(paginatedResponse);
			cacheService.store(cacheKey, cacheDto, CacheConstants.REVIEWS_DEFAULT_TTL);

			log.debug(
					"Successfully cached {} reviews for course: {}",
					paginatedResponse.getContent().size(),
					courseId);

		} catch (Exception e) {
			log.error("Failed to cache course reviews for course: {}", courseId, e);
		}
	}

	/**
	 * Retrieves paginated review list response from cache
	 *
	 * @param courseId
	 *            the course identifier
	 * @param pageable
	 *            pagination parameters
	 * @return cached paginated review response or null if not found
	 */
	@SuppressWarnings("unchecked")
	public PaginatedResponse<ReviewResponseDto> getCourseReviews(String courseId, Pageable pageable) {
		try {
			String sortParam = pageable.getSort().toString();
			String cacheKey = cacheKeyBuilder.buildCourseReviewsKey(
					courseId, pageable.getPageNumber(), pageable.getPageSize(), sortParam);

			log.debug("Retrieving course reviews from cache with key: {}", cacheKey);

			PaginatedResponse<ReviewCacheDto> cacheDto = (PaginatedResponse<ReviewCacheDto>) cacheService.get(cacheKey);

			if (cacheDto != null) {
				log.debug(
						"Cache hit for course reviews - course: {}, page: {}",
						courseId,
						pageable.getPageNumber());
				return ReviewCacheMapper.fromCachePaginatedDto(cacheDto);
			}

			log.debug(
					"Cache miss for course reviews - course: {}, page: {}",
					courseId,
					pageable.getPageNumber());
			return null;

		} catch (Exception e) {
			log.error("Failed to retrieve course reviews from cache for course: {}", courseId, e);
			return null;
		}
	}

	/**
	 * Invalidates all review cache entries for a specific course
	 *
	 * @param courseId
	 *            the course identifier
	 */
	public void invalidateCourseReviews(String courseId) {
		try {
			log.debug("Invalidating course reviews cache for course: {}", courseId);

			String pattern = String.format(CacheConstants.COURSE_REVIEWS_INVALIDATION_PATTERN, courseId);
			Set<String> keys = cacheService.getKeys(pattern);

			if (!keys.isEmpty()) {
				cacheService.remove(keys);
				log.debug("Invalidated {} review cache entries for course: {}", keys.size(), courseId);
			} else {
				log.debug("No review cache entries found to invalidate for course: {}", courseId);
			}

		} catch (Exception e) {
			log.error("Failed to invalidate course reviews cache for course: {}", courseId, e);
		}
	}

	/**
	 * Checks if course reviews are cached for specific pagination
	 *
	 * @param courseId
	 *            the course identifier
	 * @param pageable
	 *            pagination parameters
	 * @return true if cached, false otherwise
	 */
	public boolean areReviewsCached(String courseId, Pageable pageable) {
		try {
			String sortParam = pageable.getSort().toString();
			String cacheKey = cacheKeyBuilder.buildCourseReviewsKey(
					courseId, pageable.getPageNumber(), pageable.getPageSize(), sortParam);

			return cacheService.exists(cacheKey);

		} catch (Exception e) {
			log.error("Error checking review cache existence for course: {}", courseId, e);
			return false;
		}
	}

	/** Get cache statistics for reviews */
	public void logCacheStatistics() {
		try {
			Set<String> keys = cacheService.getKeys(CacheConstants.REVIEWS_CACHE_PREFIX + ":*");
			log.info("Reviews cache contains {} entries", keys.size());

		} catch (Exception e) {
			log.error("Failed to get reviews cache statistics", e);
		}
	}
}
