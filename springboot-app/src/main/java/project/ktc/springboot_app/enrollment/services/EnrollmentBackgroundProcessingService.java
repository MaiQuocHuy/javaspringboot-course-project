package project.ktc.springboot_app.enrollment.services;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.cache.services.domain.CoursesCacheService;
import project.ktc.springboot_app.cache.services.infrastructure.CacheInvalidationService;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.enrollment.entity.Enrollment;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;
import project.ktc.springboot_app.notification.utils.NotificationHelper;

/**
 * Service for handling enrollment background processing tasks asynchronously.
 * This service is
 * responsible for non-essential operations that can be performed after the main
 * enrollment creation
 * to improve performance and reduce timeout risks.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EnrollmentBackgroundProcessingService {

	private final EnrollmentRepository enrollmentRepository;
	private final CoursesCacheService coursesCacheService;
	private final CacheInvalidationService cacheInvalidationService;
	private final NotificationHelper notificationHelper;

	/**
	 * Process all background tasks for a newly created enrollment asynchronously.
	 *
	 * @param enrollment
	 *            The newly created enrollment
	 * @param courseId
	 *            The course ID
	 * @param courseSlug
	 *            The course slug
	 * @param instructorId
	 *            The instructor ID
	 */
	@Async("taskExecutor")
	public CompletableFuture<Void> processEnrollmentBackgroundTasks(
			Enrollment enrollment, String courseId, String courseSlug, String instructorId) {

		long startTime = System.currentTimeMillis();
		log.info(
				"Starting background processing for enrollment: {} in course: {}",
				enrollment.getId(),
				courseId);

		try {
			// Process cache invalidation
			processCacheInvalidationAsync(courseId, courseSlug, instructorId);

			// Process notifications
			processNotificationsAsync(enrollment);

			long duration = System.currentTimeMillis() - startTime;
			log.info(
					"Background processing completed for enrollment: {} in {}ms",
					enrollment.getId(),
					duration);

		} catch (Exception e) {
			log.error(
					"Error in background processing for enrollment: {} - {}",
					enrollment.getId(),
					e.getMessage(),
					e);
			// Don't rethrow - background processing failures shouldn't affect main flow
		}

		return CompletableFuture.completedFuture(null);
	}

	/** Handle cache invalidation operations asynchronously. */
	@Async("taskExecutor")
	@Transactional
	public CompletableFuture<Void> processCacheInvalidationAsync(
			String courseId, String courseSlug, String instructorId) {

		long startTime = System.currentTimeMillis();
		log.debug("Starting cache invalidation for course: {}", courseId);

		try {
			// Get current enrollment count for logging purposes
			long currentEnrollmentCount = enrollmentRepository.countByCourseId(courseId);
			log.info(
					"Processing enrollment change for course: {} with current count: {}",
					courseId,
					currentEnrollmentCount);

			// Invalidate all course-related cache after enrollment change
			coursesCacheService.invalidateCacheForEnrollmentChange(courseId, courseSlug);

			// Invalidate instructor statistics cache
			cacheInvalidationService.invalidateInstructorStatisticsOnEnrollment(instructorId);

			// Note: Comprehensive course cache invalidation is handled by
			// invalidateCacheForEnrollmentChange

			long duration = System.currentTimeMillis() - startTime;
			log.debug("Cache invalidation completed for course: {} in {}ms", courseId, duration);

		} catch (Exception e) {
			log.error("Error in cache invalidation for course: {} - {}", courseId, e.getMessage(), e);
			// Continue processing even if cache invalidation fails
		}

		return CompletableFuture.completedFuture(null);
	}

	/** Handle notification creation asynchronously. */
	@Async("taskExecutor")
	@Transactional
	public CompletableFuture<Void> processNotificationsAsync(Enrollment enrollment) {
		long startTime = System.currentTimeMillis();
		log.debug("Starting notification processing for enrollment: {}", enrollment.getId());

		try {
			Course course = enrollment.getCourse();

			// Create instructor notification for new student enrollment
			notificationHelper.createInstructorNewStudentEnrollmentNotification(
					course.getInstructor().getId(),
					course.getId(),
					course.getTitle(),
					enrollment.getUser().getName(),
					enrollment.getUser().getId(),
					enrollment.getId());

			long duration = System.currentTimeMillis() - startTime;
			log.debug(
					"Notification processing completed for enrollment: {} in {}ms",
					enrollment.getId(),
					duration);

		} catch (Exception e) {
			log.error(
					"Error in notification processing for enrollment: {} - {}",
					enrollment.getId(),
					e.getMessage(),
					e);
			// Continue processing even if notification creation fails
		}

		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Process enrollment statistics updates asynchronously. This can be used for
	 * analytics and
	 * reporting purposes.
	 */
	@Async("taskExecutor")
	@Transactional
	public CompletableFuture<Void> processEnrollmentStatisticsAsync(String courseId, String userId) {
		long startTime = System.currentTimeMillis();
		log.debug("Starting statistics processing for course: {} and user: {}", courseId, userId);

		try {
			// Update enrollment statistics
			long totalEnrollments = enrollmentRepository.countByCourseId(courseId);
			log.debug("Updated course {} total enrollments: {}", courseId, totalEnrollments);

			// Additional statistics processing can be added here
			// For example: user engagement metrics, course popularity metrics, etc.

			long duration = System.currentTimeMillis() - startTime;
			log.debug("Statistics processing completed for course: {} in {}ms", courseId, duration);

		} catch (Exception e) {
			log.error("Error in statistics processing for course: {} - {}", courseId, e.getMessage(), e);
			// Continue processing even if statistics update fails
		}

		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Comprehensive background processing for webhook-triggered enrollments. This
	 * method handles all
	 * non-essential operations that can be performed asynchronously.
	 */
	@Async("taskExecutor")
	public CompletableFuture<Void> processWebhookEnrollmentBackground(
			String enrollmentId, String courseId, String courseSlug, String instructorId, String userId) {

		long startTime = System.currentTimeMillis();
		log.info(
				"Starting comprehensive background processing for webhook enrollment: {}", enrollmentId);

		try {
			// Process cache invalidation
			processCacheInvalidationAsync(courseId, courseSlug, instructorId).get();

			// Process statistics
			processEnrollmentStatisticsAsync(courseId, userId).get();

			long duration = System.currentTimeMillis() - startTime;
			log.info(
					"Comprehensive background processing completed for enrollment: {} in {}ms",
					enrollmentId,
					duration);

		} catch (Exception e) {
			log.error(
					"Error in comprehensive background processing for enrollment: {} - {}",
					enrollmentId,
					e.getMessage(),
					e);
		}

		return CompletableFuture.completedFuture(null);
	}
}
