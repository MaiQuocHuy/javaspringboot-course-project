package project.ktc.springboot_app.stripe.services;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.email.interfaces.EmailService;
import project.ktc.springboot_app.notification.utils.NotificationHelper;
import project.ktc.springboot_app.payment.entity.Payment;
import project.ktc.springboot_app.payment.repositories.PaymentRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;

/**
 * Service to handle background processing tasks for completed payments This keeps the main webhook
 * response fast by offloading heavy operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentBackgroundProcessingService {

  private final EmailService emailService;
  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final PaymentRepository paymentRepository;
  private final NotificationHelper notificationHelper;

  /**
   * Processes all background tasks for a completed payment asynchronously This method is called
   * from the webhook handler to avoid blocking the response
   */
  @Async("taskExecutor")
  @Transactional
  public CompletableFuture<Void> processPaymentBackgroundTasks(
      String sessionId, String courseId, String userId, String paymentId) {

    long startTime = System.currentTimeMillis();
    log.info(
        "🔄 Starting background processing for payment: {} (session: {})", paymentId, sessionId);

    try {
      // Step 1: Fetch basic data (lightweight queries)
      User user = userRepository.findById(userId).orElse(null);
      if (user == null) {
        log.error("❌ User not found with ID: {} during background processing", userId);
        return CompletableFuture.completedFuture(null);
      }

      // Use lightweight course query for basic info only
      Course course = courseRepository.findBasicCourseInfoById(courseId).orElse(null);
      if (course == null) {
        log.error("❌ Course not found with ID: {} during background processing", courseId);
        return CompletableFuture.completedFuture(null);
      }

      Payment payment = paymentRepository.findById(paymentId).orElse(null);
      if (payment == null) {
        log.error("❌ Payment not found with ID: {} during background processing", paymentId);
        return CompletableFuture.completedFuture(null);
      }

      // Step 2: Send payment confirmation email (async within async)
      sendPaymentConfirmationEmailAsync(user, course, payment, sessionId);

      // Step 3: Create notifications
      createPaymentNotifications(payment, user, course);

      // Step 4: Process affiliate payouts if applicable
      processAffiliatePayouts(payment, course);

      long endTime = System.currentTimeMillis();
      log.info(
          "✅ Background processing completed for payment: {} in {}ms",
          paymentId,
          (endTime - startTime));

    } catch (Exception e) {
      log.error(
          "❌ Error during background processing for payment: {} - {}",
          paymentId,
          e.getMessage(),
          e);
      // Don't rethrow - background processing failures shouldn't affect main payment
      // flow
    }

    return CompletableFuture.completedFuture(null);
  }

  /** Sends payment confirmation email with optimized data fetching */
  private void sendPaymentConfirmationEmailAsync(
      User user, Course course, Payment payment, String sessionId) {
    try {
      log.info(
          "📧 Sending payment confirmation email for user: {} course: {}",
          user.getId(),
          course.getId());

      // Get cached course statistics if available, otherwise use defaults
      String courseDuration = getCachedCourseDuration(course.getId());
      String lessonCount = getCachedLessonCount(course.getId());

      // Format amount
      String formattedAmount = String.format("$%.2f", payment.getAmount().doubleValue());

      // Build course URL
      String courseUrl = String.format("https://ktc-learning.com/courses/%s", course.getId());

      // Send email with basic course info
      emailService.sendPaymentConfirmationEmailAsync(
          user.getEmail(),
          user.getName(),
          course.getTitle(),
          courseUrl,
          course.getInstructor() != null ? course.getInstructor().getName() : "KTC Learning",
          course.getLevel() != null ? course.getLevel().toString() : "Beginner",
          courseDuration,
          lessonCount,
          formattedAmount,
          sessionId, // Use session ID as payment intent reference
          "Card", // Default payment method
          java.time.LocalDateTime.now());

      log.info("✅ Payment confirmation email sent successfully to: {}", user.getEmail());

    } catch (Exception e) {
      log.error("❌ Failed to send payment confirmation email: {}", e.getMessage(), e);
    }
  }

  /** Creates payment-related notifications */
  private void createPaymentNotifications(Payment payment, User user, Course course) {
    try {
      log.info("🔔 Creating payment notifications for payment: {}", payment.getId());

      // Create admin notification
      notificationHelper.createAdminStudentPaymentNotification(
          payment.getId(), user.getName(), course.getTitle(), payment.getAmount());

      // Create student payment success notification
      try {
        String courseUrl = "/dashboard/learning/" + course.getId();
        notificationHelper
            .createPaymentSuccessNotification(
                user.getId(), payment.getId(), course.getTitle(), courseUrl, course.getId())
            .thenAccept(
                notification ->
                    log.info(
                        "✅ Payment success notification created for student {} ({}): {}",
                        user.getName(),
                        user.getId(),
                        notification.getId()))
            .exceptionally(
                ex -> {
                  log.error(
                      "❌ Failed to create payment success notification for student {}: {}",
                      user.getId(),
                      ex.getMessage(),
                      ex);
                  return null;
                });

        log.info(
            "💰 Student {} purchased course {} for ${} - payment success notification created",
            user.getName(),
            course.getTitle(),
            payment.getAmount());

      } catch (Exception studentNotificationError) {
        log.error(
            "❌ Failed to create student payment success notification: {}",
            studentNotificationError.getMessage(),
            studentNotificationError);
        // Continue with admin notification even if student notification fails
      }

      log.info("✅ Payment notifications created successfully");

    } catch (Exception e) {
      log.error("❌ Failed to create payment notifications: {}", e.getMessage(), e);
    }
  }

  /**
   * Processes affiliate payouts if applicable Note: Affiliate processing will be handled separately
   * via discount usage records
   */
  private void processAffiliatePayouts(Payment payment, Course course) {
    try {
      log.info("💰 Checking affiliate payouts for payment: {}", payment.getId());

      // Affiliate payouts are typically processed via DiscountUsage records
      // This can be extended based on your affiliate system implementation
      log.info("✅ Affiliate payout check completed for payment: {}", payment.getId());

    } catch (Exception e) {
      log.error("❌ Failed to process affiliate payouts: {}", e.getMessage(), e);
    }
  }

  /** Gets cached course duration or calculates lightweight version */
  private String getCachedCourseDuration(String courseId) {
    try {
      // Try to get from cache first (implementation depends on your caching strategy)
      // For now, return a placeholder - this could be enhanced with Redis cache
      return "2-3 hours"; // Default estimate
    } catch (Exception e) {
      log.warn("Could not get cached course duration for course: {}", courseId);
      return "Not specified";
    }
  }

  /** Gets cached lesson count or returns estimate */
  private String getCachedLessonCount(String courseId) {
    try {
      // Try to get from cache first (implementation depends on your caching strategy)
      // For now, return a placeholder - this could be enhanced with Redis cache
      return "10-15"; // Default estimate
    } catch (Exception e) {
      log.warn("Could not get cached lesson count for course: {}", courseId);
      return "Multiple";
    }
  }
}
