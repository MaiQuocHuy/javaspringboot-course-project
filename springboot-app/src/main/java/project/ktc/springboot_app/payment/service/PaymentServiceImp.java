package project.ktc.springboot_app.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.entity.Payment;
import project.ktc.springboot_app.payment.repositories.PaymentRepository;
import project.ktc.springboot_app.log.services.SystemLogHelper;
import project.ktc.springboot_app.log.mapper.PaymentLogMapper;
import project.ktc.springboot_app.utils.SecurityUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of PaymentService for handling payment operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImp implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final SystemLogHelper systemLogHelper;

    @Override
    public void updatePaymentStatusFromWebhook(String paymentId, String status, String stripeSessionId) {
        log.info("Updating payment {} status to {} from Stripe session {}", paymentId, status, stripeSessionId);

        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);

            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();

                // Capture old values for logging
                var oldPaymentLog = PaymentLogMapper.toLogDto(payment);

                // Update payment status
                Payment.PaymentStatus oldStatus = payment.getStatus();
                payment.setStatus(Payment.PaymentStatus.valueOf(status));

                if ("COMPLETED".equals(status)) {
                    payment.setPaidAt(LocalDateTime.now());
                }

                Payment updatedPayment = paymentRepository.save(payment);

                // Capture new values for logging
                var newPaymentLog = PaymentLogMapper.toLogDto(updatedPayment);

                // Log the status update operation
                try {
                    User currentUser = getCurrentUserForLogging();
                    systemLogHelper.logUpdate(currentUser, "PAYMENT", paymentId, oldPaymentLog, newPaymentLog);
                    log.info("Payment status update logged for payment {}: {} -> {}", paymentId, oldStatus, status);
                } catch (Exception logError) {
                    log.warn("Failed to log payment status update for payment {}: {}", paymentId,
                            logError.getMessage());
                }

                log.info("Payment {} status updated to {}", paymentId, status);
            } else {
                log.error("Payment with ID {} not found", paymentId);
            }
        } catch (Exception e) {
            log.error("Error updating payment status for payment {}: {}", paymentId, e.getMessage(), e);
            throw new RuntimeException("Failed to update payment status", e);
        }
    }

    @Override
    public String createPayment(String userId, String courseId, Double amount, String stripeSessionId) {
        log.info("Creating payment for user {} and course {} with amount {}", userId, courseId, amount);

        try {
            // Fetch user and course
            Optional<User> userOpt = userRepository.findById(userId);
            Optional<Course> courseOpt = courseRepository.findById(courseId);

            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found: " + userId);
            }

            if (courseOpt.isEmpty()) {
                throw new RuntimeException("Course not found: " + courseId);
            }

            User user = userOpt.get();
            Course course = courseOpt.get();

            // Create payment entity
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID().toString());
            payment.setUser(user);
            payment.setCourse(course);
            payment.setAmount(BigDecimal.valueOf(amount));
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setPaymentMethod("stripe");

            // Set session ID if provided
            if (stripeSessionId != null) {
                payment.setSessionId(stripeSessionId);
            }

            Payment savedPayment = paymentRepository.save(payment);

            // Log the payment creation
            try {
                User currentUser = getCurrentUserForLogging();
                var paymentLog = PaymentLogMapper.toLogDto(savedPayment);
                systemLogHelper.logCreate(currentUser, "PAYMENT", savedPayment.getId(), paymentLog);
                log.info("Payment creation logged for payment {}", savedPayment.getId());
            } catch (Exception logError) {
                log.warn("Failed to log payment creation for payment {}: {}", savedPayment.getId(),
                        logError.getMessage());
            }

            log.info("Payment created with ID: {}", savedPayment.getId());
            return savedPayment.getId();

        } catch (Exception e) {
            log.error("Error creating payment for user {} and course {}: {}", userId, courseId, e.getMessage(), e);
            throw new RuntimeException("Failed to create payment", e);
        }
    }

    @Override
    public void updatePaymentSessionId(String paymentId, String stripeSessionId) {
        log.info("Updating payment {} with session ID {}", paymentId, stripeSessionId);

        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);

            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();

                // Capture old values for logging
                var oldPaymentLog = PaymentLogMapper.toLogDto(payment);

                // Update session ID
                payment.setSessionId(stripeSessionId);
                Payment updatedPayment = paymentRepository.save(payment);

                // Capture new values for logging
                var newPaymentLog = PaymentLogMapper.toLogDto(updatedPayment);

                // Log the session ID update
                try {
                    User currentUser = getCurrentUserForLogging();
                    systemLogHelper.logUpdate(currentUser, "PAYMENT", paymentId, oldPaymentLog, newPaymentLog);
                    log.info("Payment session ID update logged for payment {}", paymentId);
                } catch (Exception logError) {
                    log.warn("Failed to log payment session ID update for payment {}: {}", paymentId,
                            logError.getMessage());
                }

                log.info("Payment {} updated with session ID {}", paymentId, stripeSessionId);
            } else {
                log.error("Payment with ID {} not found", paymentId);
                throw new RuntimeException("Payment not found: " + paymentId);
            }
        } catch (Exception e) {
            log.error("Error updating payment session ID for payment {}: {}", paymentId, e.getMessage(), e);
            throw new RuntimeException("Failed to update payment session ID", e);
        }
    }

    @Override
    public Optional<Payment> findPaymentBySessionIdAndVerifyAmount(String stripeSessionId, Double paidAmount) {
        log.info("Finding payment by session ID {} and verifying amount {}", stripeSessionId, paidAmount);

        try {
            Optional<Payment> paymentOpt = paymentRepository.findBySessionId(stripeSessionId);

            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                Double expectedAmount = payment.getAmount().doubleValue();

                // Compare amounts with small tolerance for floating point precision
                double tolerance = 0.01; // 1 cent tolerance
                if (Math.abs(expectedAmount - paidAmount) <= tolerance) {
                    log.info("Payment amount verified successfully for session {}: expected={}, paid={}",
                            stripeSessionId, expectedAmount, paidAmount);
                    return paymentOpt;
                } else {
                    log.error("Payment amount mismatch for session {}: expected={}, paid={}",
                            stripeSessionId, expectedAmount, paidAmount);
                    return Optional.empty();
                }
            } else {
                log.error("Payment not found for session ID {}", stripeSessionId);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error finding payment by session ID {}: {}", stripeSessionId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Helper method to get current user for logging purposes
     * Returns a system user if no authenticated user is available (e.g., webhook
     * operations)
     */
    private User getCurrentUserForLogging() {
        try {
            String currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId != null) {
                Optional<User> userOpt = userRepository.findById(currentUserId);
                if (userOpt.isPresent()) {
                    return userOpt.get();
                }
            }
        } catch (Exception e) {
            log.debug("No authenticated user found for logging, using system user: {}", e.getMessage());
        }

        // Return system user for webhook operations or when no authenticated user is
        // available
        return getSystemUser();
    }

    /**
     * Returns a system user for logging when no authenticated user is available
     */
    private User getSystemUser() {
        // Fetch the SYSTEM user from database for logging purposes when no
        // authenticated user is available
        return userRepository.findById("SYSTEM")
                .orElseThrow(() -> new RuntimeException(
                        "SYSTEM user not found in database. Please run database migrations."));
    }
}
