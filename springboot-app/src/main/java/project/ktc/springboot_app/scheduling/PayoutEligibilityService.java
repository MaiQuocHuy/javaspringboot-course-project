package project.ktc.springboot_app.scheduling;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.config.PayoutSchedulingProperties;
import project.ktc.springboot_app.earning.entity.InstructorEarning;
import project.ktc.springboot_app.earning.repositories.InstructorEarningRepository;
import project.ktc.springboot_app.payment.entity.Payment;
import project.ktc.springboot_app.payment.repositories.AdminPaymentRepository;
import project.ktc.springboot_app.refund.entity.Refund;

/**
 * Service for checking payout eligibility of payments Implements the same business logic as the
 * manual payout API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayoutEligibilityService {

  private final AdminPaymentRepository adminPaymentRepository;
  private final InstructorEarningRepository instructorEarningRepository;
  private final PayoutSchedulingProperties payoutProperties;

  /**
   * Find all payments eligible for automatic payout
   *
   * @return List of payments that meet all payout criteria
   */
  @Transactional(readOnly = true)
  public List<Payment> findEligiblePayments() {
    log.info("Searching for payments eligible for automatic payout");

    // Get all completed payments with refunds loaded to avoid LazyInitializationException
    List<Payment> completedPayments =
        adminPaymentRepository
            .findPaymentsByStatusWithRefunds(
                Payment.PaymentStatus.COMPLETED,
                PageRequest.of(
                    0, payoutProperties.getBatchSize() * 3) // Get more to filter properly
                )
            .getContent();

    // Pre-filter to exclude already paid out payments at query level
    List<Payment> unpaidPayments =
        completedPayments.stream().filter(payment -> payment.getPaidOutAt() == null).toList();

    log.debug(
        "Found {} completed payments, {} not yet paid out",
        completedPayments.size(),
        unpaidPayments.size());

    // Filter payments based on eligibility criteria using unpaid payments
    List<Payment> eligiblePayments =
        unpaidPayments.stream()
            .filter(this::isPaymentEligibleForPayout)
            .limit(payoutProperties.getBatchSize())
            .collect(Collectors.toList());

    log.info(
        "Found {} eligible payments out of {} unpaid payments (from {} total completed)",
        eligiblePayments.size(),
        unpaidPayments.size(),
        completedPayments.size());

    return eligiblePayments;
  }

  /**
   * Check if a specific payment is eligible for payout
   *
   * @param payment The payment to check
   * @return true if payment meets all payout criteria
   */
  @Transactional(readOnly = true)
  public boolean isPaymentEligibleForPayout(Payment payment) {
    try {
      // 1. Check if payment status is COMPLETED
      if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
        log.debug("Payment {} not eligible: status is {}", payment.getId(), payment.getStatus());
        return false;
      }

      // 2. Check if payment is already paid out
      if (payment.getPaidOutAt() != null) {
        log.debug(
            "Payment {} not eligible: already paid out at {}",
            payment.getId(),
            payment.getPaidOutAt());
        return false;
      }

      // 3. Check if 3-day waiting period has passed since payment completion
      LocalDateTime paymentCompletedAt = payment.getUpdatedAt(); // When status changed to COMPLETED
      LocalDateTime now = LocalDateTime.now();
      Duration timeSinceCompletion = Duration.between(paymentCompletedAt, now);

      if (timeSinceCompletion.toDays() < payoutProperties.getWaiting().getPeriod().getDays()) {
        long hoursRemaining =
            (payoutProperties.getWaiting().getPeriod().getDays() * 24)
                - timeSinceCompletion.toHours();
        log.debug(
            "Payment {} not eligible: within waiting period. {} hours remaining",
            payment.getId(),
            hoursRemaining);
        return false;
      }

      // 4. Check refund status - no pending or completed refunds allowed
      try {
        if (payment.getRefunds() != null && !payment.getRefunds().isEmpty()) {
          boolean hasPendingRefund =
              payment.getRefunds().stream()
                  .anyMatch(
                      refund ->
                          refund != null && refund.getStatus() == Refund.RefundStatus.PENDING);
          boolean hasCompletedRefund =
              payment.getRefunds().stream()
                  .anyMatch(
                      refund ->
                          refund != null && refund.getStatus() == Refund.RefundStatus.COMPLETED);

          if (hasPendingRefund || hasCompletedRefund) {
            log.debug("Payment {} not eligible: has pending or completed refunds", payment.getId());
            return false;
          }
        }
      } catch (Exception e) {
        log.warn(
            "Error checking refunds for payment {}: {} - assuming no blocking refunds",
            payment.getId(),
            e.getMessage());
        // Continue processing - if we can't check refunds, don't block the payout
      }

      // 5. Check if instructor earning already exists
      Optional<InstructorEarning> existingEarning =
          instructorEarningRepository.findByPaymentId(payment.getId());
      if (existingEarning.isPresent()) {
        log.debug("Payment {} not eligible: instructor earning already exists", payment.getId());
        return false;
      }

      // 6. Check if payment has a valid course and instructor
      if (payment.getCourse() == null || payment.getCourse().getInstructor() == null) {
        log.debug("Payment {} not eligible: missing course or instructor", payment.getId());
        return false;
      }

      log.debug("Payment {} is eligible for automatic payout", payment.getId());
      return true;

    } catch (Exception e) {
      log.error(
          "Error checking payout eligibility for payment {}: {}",
          payment.getId(),
          e.getMessage(),
          e);
      return false;
    }
  }

  /**
   * Get summary of payout eligibility status
   *
   * @return PayoutEligibilitySummary with counts and details
   */
  @Transactional(readOnly = true)
  public PayoutEligibilitySummary getEligibilitySummary() {
    log.info("Generating payout eligibility summary");

    try {
      // Get all completed payments for analysis
      Pageable pageable = PageRequest.of(0, 1000); // Reasonable limit for analysis
      List<Payment> completedPayments =
          adminPaymentRepository
              .findPaymentsByStatus(Payment.PaymentStatus.COMPLETED, pageable)
              .getContent();

      long totalCompleted = completedPayments.size();
      long alreadyPaidOut =
          completedPayments.stream().filter(p -> p.getPaidOutAt() != null).count();

      long withinWaitingPeriod =
          completedPayments.stream()
              .filter(p -> p.getPaidOutAt() == null)
              .filter(this::isWithinWaitingPeriod)
              .count();

      long withRefunds =
          completedPayments.stream()
              .filter(p -> p.getPaidOutAt() == null)
              .filter(this::hasBlockingRefunds)
              .count();

      long eligible = completedPayments.stream().filter(this::isPaymentEligibleForPayout).count();

      return PayoutEligibilitySummary.builder()
          .totalCompletedPayments(totalCompleted)
          .alreadyPaidOut(alreadyPaidOut)
          .withinWaitingPeriod(withinWaitingPeriod)
          .withBlockingRefunds(withRefunds)
          .eligibleForPayout(eligible)
          .build();

    } catch (Exception e) {
      log.error("Error generating payout eligibility summary: {}", e.getMessage(), e);
      return PayoutEligibilitySummary.builder().build();
    }
  }

  private boolean isWithinWaitingPeriod(Payment payment) {
    LocalDateTime paymentCompletedAt = payment.getUpdatedAt();
    LocalDateTime now = LocalDateTime.now();
    Duration timeSinceCompletion = Duration.between(paymentCompletedAt, now);
    return timeSinceCompletion.toDays() < payoutProperties.getWaiting().getPeriod().getDays();
  }

  private boolean hasBlockingRefunds(Payment payment) {
    if (payment.getRefunds() == null || payment.getRefunds().isEmpty()) {
      return false;
    }
    return payment.getRefunds().stream()
        .anyMatch(
            refund ->
                refund.getStatus() == Refund.RefundStatus.PENDING
                    || refund.getStatus() == Refund.RefundStatus.COMPLETED);
  }

  /** Summary of payout eligibility analysis */
  @lombok.Builder
  @lombok.Data
  public static class PayoutEligibilitySummary {
    private long totalCompletedPayments;
    private long alreadyPaidOut;
    private long withinWaitingPeriod;
    private long withBlockingRefunds;
    private long eligibleForPayout;
  }
}
