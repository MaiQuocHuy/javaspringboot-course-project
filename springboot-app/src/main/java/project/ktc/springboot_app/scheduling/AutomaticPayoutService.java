package project.ktc.springboot_app.scheduling;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.cache.services.infrastructure.CacheInvalidationService;
import project.ktc.springboot_app.config.PayoutSchedulingProperties;
import project.ktc.springboot_app.earning.entity.InstructorEarning;
import project.ktc.springboot_app.earning.repositories.InstructorEarningRepository;
import project.ktc.springboot_app.payment.entity.Payment;
import project.ktc.springboot_app.payment.repositories.AdminPaymentRepository;
import project.ktc.springboot_app.cache.services.infrastructure.CacheInvalidationService;
import project.ktc.springboot_app.payment.services.AdminPaymentServiceImp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Automated payout processing service Runs scheduled jobs to process eligible
 * payments for
 * instructor payouts
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "app.payout.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class AutomaticPayoutService {

	private final PayoutEligibilityService payoutEligibilityService;
	private final AdminPaymentRepository adminPaymentRepository;
	private final InstructorEarningRepository instructorEarningRepository;
	private final CacheInvalidationService cacheInvalidationService;
	private final PayoutSchedulingProperties payoutProperties;
	private final PayoutNotificationService payoutNotificationService;
	private final AdminPaymentServiceImp adminPaymentService;

	/**
	 * Main scheduled job for processing automatic payouts
	 * Runs every 4 hours during business hours (8 AM to 8 PM)
	 * Test mode: runs every 5 minutes for debugging
	 */
	// @Scheduled(cron = "0 */5 * * * *") // Every 5 minutes for testing
	@Scheduled(cron = "0 0 8,12,16,20 * * *") // Production: 4 times a day
	public void processAutomaticPayouts() {
		if (!payoutProperties.getScheduling().isEnabled()) {
			log.debug("Automatic payout processing is disabled");
			return;
		}

		log.info("🔄 Starting automatic payout processing job");

		try {
			// Get eligible payments
			List<Payment> eligiblePayments = payoutEligibilityService.findEligiblePayments();

			if (eligiblePayments.isEmpty()) {
				log.info("✅ No payments eligible for automatic payout at this time");
				return;
			}

			log.info("💰 Processing {} eligible payments for automatic payout", eligiblePayments.size());

			// Log payment IDs for debugging
			if (log.isDebugEnabled()) {
				eligiblePayments.forEach(
						payment -> log.debug(
								"🎯 Eligible payment: {} (Amount: ${}, Status: {}, PaidOutAt: {})",
								payment.getId(),
								payment.getAmount(),
								payment.getStatus(),
								payment.getPaidOutAt()));
			}

			AtomicInteger successCount = new AtomicInteger(0);
			AtomicInteger failureCount = new AtomicInteger(0);
			BigDecimal totalAmount = BigDecimal.ZERO;

			// Process each eligible payment
			for (Payment payment : eligiblePayments) {
				try {
					boolean success = processPaymentPayout(payment);
					if (success) {
						successCount.incrementAndGet();
						totalAmount = totalAmount.add(payment.getAmount());
					} else {
						failureCount.incrementAndGet();
					}
				} catch (Exception e) {
					log.error(
							"Error processing payout for payment {}: {}", payment.getId(), e.getMessage(), e);
					failureCount.incrementAndGet();
				}
			}

			// Log summary
			log.info(
					"🎯 Automatic payout processing completed - Success: {}, Failed: {}, Total Amount: ${}",
					successCount.get(),
					failureCount.get(),
					totalAmount);

			// Send notification if enabled
			if (payoutProperties.getNotification().isEnabled()) {
				payoutNotificationService.sendPayoutProcessingSummary(
						successCount.get(), failureCount.get(), totalAmount);
			}

		} catch (Exception e) {
			log.error("❌ Error during automatic payout processing: {}", e.getMessage(), e);

			// Send error notification
			if (payoutProperties.getNotification().isEnabled()) {
				payoutNotificationService.sendPayoutProcessingError(e);
			}
		}
	}

	/**
	 * Process payout for a single payment Implements the same logic as the manual
	 * payout API
	 *
	 * @param payment
	 *            The payment to process
	 * @return true if payout was successful, false otherwise
	 */
	@Transactional
	public boolean processPaymentPayout(Payment payment) {
		log.info(
				"💳 Processing automatic payout for payment: {} (Amount: ${})",
				payment.getId(),
				payment.getAmount());

		try {
			// Refresh payment from database to get latest state with refunds
			Payment freshPayment = adminPaymentRepository.findPaymentByIdWithRefunds(payment.getId()).orElse(null);
			if (freshPayment == null) {
				log.warn("Payment {} not found in database", payment.getId());
				return false;
			}

			// Check if payment was already paid out (by another process/thread)
			if (freshPayment.getPaidOutAt() != null) {
				log.warn(
						"Payment {} was already paid out at {} - skipping duplicate processing",
						freshPayment.getId(),
						freshPayment.getPaidOutAt());
				return false;
			}

			// Double-check eligibility with fresh payment data
			if (!payoutEligibilityService.isPaymentEligibleForPayout(freshPayment)) {
				log.warn("Payment {} is no longer eligible for payout", freshPayment.getId());
				return false;
			}

			// Check if instructor earning already exists (additional safety check)
			var existingEarning = instructorEarningRepository.findByPaymentId(freshPayment.getId());
			if (existingEarning.isPresent()) {
				log.warn(
						"Instructor earning already exists for payment {} - skipping duplicate processing",
						freshPayment.getId());
				return false;
			}

			LocalDateTime now = LocalDateTime.now();

			// Calculate instructor earning amount (70% of payment amount)
			BigDecimal instructorEarningAmount = freshPayment
					.getAmount()
					.multiply(payoutProperties.getInstructor().getEarning().getPercentage())
					.setScale(2, RoundingMode.HALF_UP);

			// Create instructor earning record
			InstructorEarning instructorEarning = InstructorEarning.builder()
					.instructor(freshPayment.getCourse().getInstructor())
					.payment(freshPayment)
					.course(freshPayment.getCourse())
					.amount(instructorEarningAmount)
					.status(InstructorEarning.EarningStatus.AVAILABLE)
					.paidAt(null) // Will be set when actually paid to instructor
					.build();

			InstructorEarning savedEarning = instructorEarningRepository.save(instructorEarning);

			// Update payment paid out timestamp using fresh payment
			freshPayment.setPaidOutAt(now);
			Payment updatedPayment = adminPaymentRepository.save(freshPayment);

			adminPaymentService.createAffiliatePayoutForReferralDiscount(freshPayment);

			// Invalidate relevant caches
			cacheInvalidationService.invalidateInstructorStatisticsOnPayment(
					freshPayment.getCourse().getInstructor().getId());

			log.info(
					"✅ Successfully processed automatic payout for payment: {} -> Earning: {} (${}) for instructor: {} at {}",
					updatedPayment.getId(),
					savedEarning.getId(),
					instructorEarningAmount,
					freshPayment.getCourse().getInstructor().getName(),
					updatedPayment.getPaidOutAt());

			return true;

		} catch (Exception e) {
			log.error(
					"❌ Failed to process automatic payout for payment {}: {}",
					payment.getId(),
					e.getMessage(),
					e);
			return false;
		}
	}

	/**
	 * Daily summary job - runs every day at 9 AM Provides daily statistics about
	 * payout processing
	 */
	@Scheduled(cron = "0 0 9 * * *")
	public void generateDailySummary() {
		if (!payoutProperties.getScheduling().isEnabled()) {
			return;
		}

		log.info("📊 Generating daily payout summary");

		try {
			PayoutEligibilityService.PayoutEligibilitySummary summary = payoutEligibilityService
					.getEligibilitySummary();

			log.info("📈 Daily Payout Summary:");
			log.info("  Total Completed Payments: {}", summary.getTotalCompletedPayments());
			log.info("  Already Paid Out: {}", summary.getAlreadyPaidOut());
			log.info("  Within Waiting Period: {}", summary.getWithinWaitingPeriod());
			log.info("  With Blocking Refunds: {}", summary.getWithBlockingRefunds());
			log.info("  Eligible for Payout: {}", summary.getEligibleForPayout());

			// Send daily summary notification if enabled
			if (payoutProperties.getNotification().isEnabled()) {
				payoutNotificationService.sendDailySummary(summary);
			}

		} catch (Exception e) {
			log.error("Error generating daily payout summary: {}", e.getMessage(), e);
		}
	}

	/**
	 * Weekly cleanup job - runs every Sunday at 2 AM Performs maintenance tasks for
	 * payout processing
	 */
	@Scheduled(cron = "0 0 2 * * SUN")
	public void performWeeklyMaintenance() {
		if (!payoutProperties.getScheduling().isEnabled()) {
			return;
		}

		log.info("🧹 Performing weekly payout maintenance");

		try {
			// Log weekly statistics
			long totalEarnings = instructorEarningRepository.count();
			log.info("📊 Weekly maintenance - Total instructor earnings: {}", totalEarnings);

			// Additional maintenance tasks can be added here
			// e.g., cleanup old logs, update metrics, etc.

		} catch (Exception e) {
			log.error("Error during weekly payout maintenance: {}", e.getMessage(), e);
		}
	}

	/**
	 * Health check method to verify the service is working Can be called by
	 * monitoring systems
	 */
	public boolean isHealthy() {
		try {
			// Verify essential dependencies are available
			payoutEligibilityService.getEligibilitySummary();
			return true;
		} catch (Exception e) {
			log.error("Automatic payout service health check failed: {}", e.getMessage(), e);
			return false;
		}
	}
}
