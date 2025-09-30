package project.ktc.springboot_app.scheduling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.config.PayoutSchedulingProperties;

/**
 * Controller for managing and monitoring automatic payout scheduling Admin-only endpoints for
 * controlling the payout system
 */
@RestController
@RequestMapping("/api/admin/payout-scheduling")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    value = "app.payout.scheduling.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class PayoutSchedulingController {

  private final AutomaticPayoutService automaticPayoutService;
  private final PayoutEligibilityService payoutEligibilityService;
  private final PayoutSchedulingProperties payoutProperties;
  private final PayoutNotificationService payoutNotificationService;
  private final project.ktc.springboot_app.config.PayoutEmailConfigProcessor emailConfigProcessor;
  private final project.ktc.springboot_app.earning.repositories.InstructorEarningRepository
      instructorEarningRepository;

  /** Manually trigger automatic payout processing Useful for testing or emergency processing */
  @PostMapping("/trigger")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<String>> triggerPayoutProcessing() {
    log.info("Admin manually triggering automatic payout processing");

    try {
      automaticPayoutService.processAutomaticPayouts();
      return ApiResponseUtil.success(
          "Automatic payout processing completed successfully",
          "Payout processing triggered manually");
    } catch (Exception e) {
      log.error("Error during manual payout processing trigger: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError(
          "Failed to trigger payout processing: " + e.getMessage());
    }
  }

  /** Get current payout eligibility status Shows how many payments are eligible, waiting, etc. */
  @GetMapping("/status")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<PayoutEligibilityService.PayoutEligibilitySummary>>
      getPayoutStatus() {
    log.info("Admin requesting payout eligibility status");

    try {
      PayoutEligibilityService.PayoutEligibilitySummary summary =
          payoutEligibilityService.getEligibilitySummary();

      return ApiResponseUtil.success(summary, "Payout eligibility status retrieved successfully");
    } catch (Exception e) {
      log.error("Error retrieving payout status: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError(
          "Failed to retrieve payout status: " + e.getMessage());
    }
  }

  /**
   * Get scheduling configuration and health status Shows current configuration and system health
   */
  @GetMapping("/config")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Map<String, Object>>> getSchedulingConfig() {
    log.info("Admin requesting payout scheduling configuration");

    try {
      Map<String, Object> config = new HashMap<>();
      config.put("schedulingEnabled", payoutProperties.getScheduling().isEnabled());
      config.put("waitingPeriodDays", payoutProperties.getWaiting().getPeriod().getDays());
      config.put(
          "instructorEarningPercentage",
          payoutProperties.getInstructor().getEarning().getPercentage());
      config.put("batchSize", payoutProperties.getBatchSize());
      config.put("maxRetryAttempts", payoutProperties.getRetry().getMax().getAttempts());
      config.put("notificationEnabled", payoutProperties.getNotification().isEnabled());
      config.put("adminEmails", payoutProperties.getNotification().getAdmin().getEmails());
      config.put("systemHealthy", automaticPayoutService.isHealthy());

      return ApiResponseUtil.success(
          config, "Payout scheduling configuration retrieved successfully");
    } catch (Exception e) {
      log.error("Error retrieving payout configuration: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError(
          "Failed to retrieve payout configuration: " + e.getMessage());
    }
  }

  /** Generate daily summary manually Useful for getting current statistics */
  @PostMapping("/generate-summary")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<String>> generateDailySummary() {
    log.info("Admin manually triggering daily payout summary generation");

    try {
      automaticPayoutService.generateDailySummary();
      return ApiResponseUtil.success(
          "Daily summary generated successfully",
          "Summary has been generated and notifications sent if enabled");
    } catch (Exception e) {
      log.error("Error during manual summary generation: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError(
          "Failed to generate daily summary: " + e.getMessage());
    }
  }

  /**
   * Health check endpoint for the scheduling system Returns the health status of the automatic
   * payout system
   */
  @GetMapping("/health")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth() {
    log.info("Admin checking payout scheduling system health");

    try {
      Map<String, Object> health = new HashMap<>();
      health.put("healthy", automaticPayoutService.isHealthy());
      health.put("schedulingEnabled", payoutProperties.getScheduling().isEnabled());
      health.put("timestamp", java.time.LocalDateTime.now());

      if (automaticPayoutService.isHealthy()) {
        return ApiResponseUtil.success(health, "Payout scheduling system is healthy");
      } else {
        return ApiResponseUtil.internalServerError("Payout scheduling system is not healthy");
      }
    } catch (Exception e) {
      log.error("Error checking system health: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError(
          "Failed to check system health: " + e.getMessage());
    }
  }

  /**
   * Send test email notification to verify email configuration Useful for testing email
   * notification system
   */
  @PostMapping("/test-email")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<String>> sendTestEmail() {
    log.info("Admin manually triggering test email notification");

    try {
      payoutNotificationService.sendTestNotification();

      return ApiResponseUtil.success(
          "Test email notification sent successfully",
          "Check admin email inboxes for the test notification");
    } catch (Exception e) {
      log.error("Error sending test email notification: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError("Failed to send test email: " + e.getMessage());
    }
  }

  /** Get email configuration status and details Shows current email notification settings */
  @GetMapping("/email-config")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Map<String, Object>>> getEmailConfig() {
    log.info("Admin requesting email notification configuration");

    try {
      List<String> adminEmails = payoutProperties.getNotification().getAdmin().getEmails();

      // Debug logging
      log.info("🔍 Raw email configuration:");
      log.info("  - Admin emails list: {}", adminEmails);
      log.info("  - Admin emails count: {}", adminEmails.size());
      log.info("  - Notification enabled: {}", payoutProperties.getNotification().isEnabled());

      Map<String, Object> emailConfig = new HashMap<>();
      emailConfig.put("notificationEnabled", payoutProperties.getNotification().isEnabled());
      emailConfig.put("adminEmails", adminEmails);
      emailConfig.put("adminEmailCount", adminEmails.size());
      emailConfig.put("timestamp", java.time.LocalDateTime.now());

      // Add detailed debug info
      emailConfig.put(
          "debugInfo",
          Map.of(
              "rawEmailsList", adminEmails.toString(),
              "rawEmailConfiguration", emailConfigProcessor.getRawEmailConfiguration(),
              "currentConfigEmails", emailConfigProcessor.getCurrentAdminEmails(),
              "individualEmails",
                  adminEmails.stream()
                      .map(email -> Map.of("email", email, "length", email.length()))
                      .toList()));

      return ApiResponseUtil.success(emailConfig, "Email configuration retrieved successfully");
    } catch (Exception e) {
      log.error("Error retrieving email configuration: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError(
          "Failed to retrieve email configuration: " + e.getMessage());
    }
  }

  /**
   * Force refresh email configuration Useful for reloading email settings without restarting the
   * application
   */
  @PostMapping("/refresh-email-config")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Map<String, Object>>> refreshEmailConfig() {
    log.info("Admin manually refreshing email configuration");

    try {
      // Force refresh the email configuration
      emailConfigProcessor.processEmailConfiguration();

      // Get updated configuration
      List<String> adminEmails = payoutProperties.getNotification().getAdmin().getEmails();

      Map<String, Object> result = new HashMap<>();
      result.put("message", "Email configuration refreshed successfully");
      result.put("adminEmails", adminEmails);
      result.put("adminEmailCount", adminEmails.size());
      result.put("rawEmailConfiguration", emailConfigProcessor.getRawEmailConfiguration());
      result.put("timestamp", java.time.LocalDateTime.now());

      return ApiResponseUtil.success(result, "Email configuration refreshed successfully");
    } catch (Exception e) {
      log.error("Error refreshing email configuration: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError(
          "Failed to refresh email configuration: " + e.getMessage());
    }
  }

  /**
   * Debug endpoint to check payment states and eligibility Useful for troubleshooting duplicate
   * processing issues
   */
  @GetMapping("/debug-payments")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Map<String, Object>>> debugPayments() {
    log.info("Admin requesting payment debug information");

    try {
      // Get eligibility summary
      PayoutEligibilityService.PayoutEligibilitySummary summary =
          payoutEligibilityService.getEligibilitySummary();

      // Get recent eligible payments
      List<project.ktc.springboot_app.payment.entity.Payment> eligiblePayments =
          payoutEligibilityService.findEligiblePayments();

      Map<String, Object> debugInfo = new HashMap<>();
      debugInfo.put("eligibilitySummary", summary);
      debugInfo.put("currentEligiblePaymentsCount", eligiblePayments.size());

      // Add details of recent eligible payments
      List<Map<String, Object>> paymentDetails =
          eligiblePayments.stream()
              .limit(10) // Show only first 10 for debugging
              .map(
                  payment -> {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("paymentId", payment.getId());
                    detail.put("amount", payment.getAmount());
                    detail.put("status", payment.getStatus());
                    detail.put("paidOutAt", payment.getPaidOutAt());
                    detail.put("createdAt", payment.getCreatedAt());
                    detail.put(
                        "courseId",
                        payment.getCourse() != null ? payment.getCourse().getId() : null);
                    detail.put(
                        "instructorId",
                        payment.getCourse() != null && payment.getCourse().getInstructor() != null
                            ? payment.getCourse().getInstructor().getId()
                            : null);

                    // Check if instructor earning already exists
                    var existingEarning =
                        instructorEarningRepository.findByPaymentId(payment.getId());
                    detail.put("hasInstructorEarning", existingEarning.isPresent());
                    if (existingEarning.isPresent()) {
                      detail.put("earningId", existingEarning.get().getId());
                      detail.put("earningStatus", existingEarning.get().getStatus());
                      detail.put("earningAmount", existingEarning.get().getAmount());
                    }

                    return detail;
                  })
              .toList();

      debugInfo.put("eligiblePaymentDetails", paymentDetails);
      debugInfo.put("timestamp", java.time.LocalDateTime.now());

      return ApiResponseUtil.success(debugInfo, "Payment debug information retrieved successfully");
    } catch (Exception e) {
      log.error("Error retrieving payment debug information: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError(
          "Failed to retrieve debug information: " + e.getMessage());
    }
  }
}
