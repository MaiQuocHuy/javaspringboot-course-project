package project.ktc.springboot_app.cache.services.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.cache.services.domain.InstructorStatisticsCacheService;

/**
 * Service for managing cache invalidation across the application Provides centralized invalidation
 * logic for various domain events
 *
 * @author KTC Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CacheInvalidationService {

  private final InstructorStatisticsCacheService instructorStatisticsCacheService;

  /**
   * Invalidates instructor statistics cache when enrollment changes This affects student count
   * statistics
   *
   * @param instructorId the instructor whose statistics should be invalidated
   */
  public void invalidateInstructorStatisticsOnEnrollment(String instructorId) {
    if (instructorId != null) {
      log.debug(
          "Invalidating instructor statistics cache due to enrollment change for instructor: {}",
          instructorId);
      instructorStatisticsCacheService.invalidateInstructorStatistics(instructorId);
    }
  }

  /**
   * Invalidates instructor statistics cache when payment/earning changes This affects revenue
   * statistics
   *
   * @param instructorId the instructor whose statistics should be invalidated
   */
  public void invalidateInstructorStatisticsOnPayment(String instructorId) {
    if (instructorId != null) {
      log.debug(
          "Invalidating instructor statistics cache due to payment/earning change for instructor: {}",
          instructorId);
      instructorStatisticsCacheService.invalidateInstructorStatistics(instructorId);
    }
  }

  /**
   * Invalidates instructor statistics cache when course changes This affects course count and
   * potentially rating statistics
   *
   * @param instructorId the instructor whose statistics should be invalidated
   */
  public void invalidateInstructorStatisticsOnCourse(String instructorId) {
    if (instructorId != null) {
      log.debug(
          "Invalidating instructor statistics cache due to course change for instructor: {}",
          instructorId);
      instructorStatisticsCacheService.invalidateInstructorStatistics(instructorId);
    }
  }

  /**
   * Invalidates instructor statistics cache when review changes This affects rating statistics
   *
   * @param instructorId the instructor whose statistics should be invalidated
   */
  public void invalidateInstructorStatisticsOnReview(String instructorId) {
    if (instructorId != null) {
      log.debug(
          "Invalidating instructor statistics cache due to review change for instructor: {}",
          instructorId);
      instructorStatisticsCacheService.invalidateInstructorStatistics(instructorId);
    }
  }

  /**
   * Invalidates instructor statistics cache when refund is processed This affects both revenue and
   * student count statistics
   *
   * @param instructorId the instructor whose statistics should be invalidated
   */
  public void invalidateInstructorStatisticsOnRefund(String instructorId) {
    if (instructorId != null) {
      log.debug(
          "Invalidating instructor statistics cache due to refund for instructor: {}",
          instructorId);
      instructorStatisticsCacheService.invalidateInstructorStatistics(instructorId);
    }
  }
}
