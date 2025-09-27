package project.ktc.springboot_app.refund.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.earning.entity.InstructorEarning;
import project.ktc.springboot_app.earning.repositories.InstructorEarningRepository;
import project.ktc.springboot_app.payment.entity.Payment;
import project.ktc.springboot_app.payment.repositories.PaymentRepository;
import project.ktc.springboot_app.refund.dto.RefundRequestDto;
import project.ktc.springboot_app.refund.dto.RefundResponseDto;
import project.ktc.springboot_app.refund.entity.Refund;
import project.ktc.springboot_app.refund.interfaces.StudentRefundService;
import project.ktc.springboot_app.refund.repositories.RefundRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.utils.SecurityUtil;
import project.ktc.springboot_app.notification.utils.NotificationHelper;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentRefundServiceImp implements StudentRefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final InstructorEarningRepository instructorEarningRepository;
    private final NotificationHelper notificationHelper;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<RefundResponseDto>> requestRefund(
            String courseId,
            RefundRequestDto refundRequestDto) {

        String userEmail = SecurityUtil.getCurrentUserEmail();
        log.info("Processing refund request for course: {} by user: {}", courseId, userEmail);

        try {
            // 1. Find the user
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                log.warn("User not found with email: {}", userEmail);
                return ApiResponseUtil.notFound("User not found");
            }
            User user = userOpt.get();

            // 2. Find the course
            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (courseOpt.isEmpty()) {
                log.warn("Course not found with ID: {}", courseId);
                return ApiResponseUtil.notFound("Course not found");
            }
            Course course = courseOpt.get();

            // 3. Find the completed payment for this user and course
            Optional<Payment> paymentOpt = paymentRepository.findCompletedPaymentByUserAndCourse(user.getId(),
                    courseId);
            if (paymentOpt.isEmpty()) {
                log.warn("No completed payment found for user: {} and course: {}", user.getId(), courseId);
                return ApiResponseUtil.badRequest("No completed payment found for this course");
            }
            Payment payment = paymentOpt.get();

            // 4. Check if payment was made within 3 days
            LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
            if (payment.getUpdatedAt().isBefore(threeDaysAgo)) {
                log.warn("Payment is older than 3 days. Payment date: {}, Current time: {}",
                        payment.getUpdatedAt(), LocalDateTime.now());
                return ApiResponseUtil.badRequest("Refund request is only allowed within 3 days of payment completion");
            }

            // 5. Check if refund already exists for this payment
            Optional<Refund> existingRefundOpt = refundRepository.findByPaymentId(payment.getId());
            if (existingRefundOpt.isPresent()) {
                log.warn("Refund already exists for payment: {}", payment.getId());
                return ApiResponseUtil.conflict("A refund request already exists for this payment");
            }

            // 6. Check if payment has been paid out to instructor
            Optional<InstructorEarning> earningOpt = instructorEarningRepository.findByPaymentId(payment.getId());
            if (earningOpt.isPresent()) {
                InstructorEarning earning = earningOpt.get();
                if (InstructorEarning.EarningStatus.PAID.equals(earning.getStatus())) {
                    log.warn("Payment has already been paid out to instructor. Earning ID: {}", earning.getId());
                    return ApiResponseUtil
                            .badRequest("Refund is not allowed as payment has already been paid out to instructor");
                }
            }

            // 7. Create refund request
            Refund refund = new Refund();
            refund.setPayment(payment);
            refund.setAmount(payment.getAmount());
            refund.setStatus(Refund.RefundStatus.PENDING);
            refund.setReason(refundRequestDto.getReason());

            Refund savedRefund = refundRepository.save(refund);

            // 8. Notify instructor about new refund request
            try {
                String instructorId = course.getInstructor().getId();
                String studentName = user.getName();
                String courseName = course.getTitle();
                String refundReason = refundRequestDto.getReason();

                notificationHelper.createInstructorNewRefundRequestNotification(
                        instructorId,
                        savedRefund.getId(),
                        courseName,
                        studentName,
                        refundReason)
                        .thenAccept(notification -> 
                            log.info("✅ Refund request notification created for instructor {}: {}", 
                                instructorId, notification.getId()))
                        .exceptionally(ex -> {
                            log.error("❌ Failed to create refund request notification for instructor {}: {}", 
                                instructorId, ex.getMessage(), ex);
                            return null;
                        });
            } catch (Exception notificationError) {
                log.error("❌ Failed to create refund request notification: {}", 
                    notificationError.getMessage(), notificationError);
                // Continue execution even if notification fails
            }

            // 9. Build response
            RefundResponseDto responseDto = RefundResponseDto.builder()
                    .id(savedRefund.getId())
                    .course(RefundResponseDto.CourseInfo.builder()
                            .id(course.getId())
                            .title(course.getTitle())
                            .build())
                    .reason(savedRefund.getReason())
                    .status(savedRefund.getStatus())
                    .amount(savedRefund.getAmount())
                    .requestedAt(savedRefund.getRequestedAt())
                    .build();

            log.info("Refund request created successfully. Refund ID: {}, Course: {}, User: {}",
                    savedRefund.getId(), courseId, userEmail);

            return ApiResponseUtil.created(responseDto, "Refund request submitted successfully");

        } catch (Exception e) {
            log.error("Error processing refund request for course: {} by user: {}", courseId, userEmail, e);
            return ApiResponseUtil.internalServerError("Failed to process refund request. Please try again later.");
        }
    }
}
