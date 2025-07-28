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
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Override
    public void updatePaymentStatusFromWebhook(String paymentId, String status, String stripeSessionId) {
        log.info("Updating payment {} status to {} from Stripe session {}", paymentId, status, stripeSessionId);

        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);

            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                payment.setStatus(status);

                if ("COMPLETED".equals(status)) {
                    payment.setPaidAt(LocalDateTime.now());
                }

                paymentRepository.save(payment);
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
            payment.setStatus("PENDING");
            payment.setPaymentMethod("stripe");

            Payment savedPayment = paymentRepository.save(payment);

            log.info("Payment created with ID: {}", savedPayment.getId());
            return savedPayment.getId();

        } catch (Exception e) {
            log.error("Error creating payment for user {} and course {}: {}", userId, courseId, e.getMessage(), e);
            throw new RuntimeException("Failed to create payment", e);
        }
    }
}
