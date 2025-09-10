package project.ktc.springboot_app.notification.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.notification.dto.CreateNotificationDto;
import project.ktc.springboot_app.notification.dto.NotificationResponseDto;
import project.ktc.springboot_app.notification.entity.NotificationPriority;
import project.ktc.springboot_app.notification.interfaces.NotificationService;
import project.ktc.springboot_app.permission.services.AuthorizationService;
import project.ktc.springboot_app.user.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Utility class for creating notifications from other services
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationHelper {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final AuthorizationService authorizationService;

    /**
     * Create a payment success notification
     */
    public CompletableFuture<NotificationResponseDto> createPaymentSuccessNotification(
            String userId, String paymentId, String courseName, String courseUrl) {

        CreateNotificationDto notificationDto = CreateNotificationDto.builder()
                .user_id(userId)
                .resource_id("res-payment-001")
                .entity_id(paymentId)
                .message("Thanh toán thành công cho khóa học '" + courseName + "'")
                .action_url(courseUrl)
                .priority(NotificationPriority.HIGH)
                .expired_at(LocalDateTime.now().plusDays(30))
                .build();

        return notificationService.createNotification(notificationDto);
    }

    /**
     * Create a course enrollment notification
     */
    public CompletableFuture<NotificationResponseDto> createEnrollmentNotification(
            String userId, String enrollmentId, String courseName, String courseUrl) {

        CreateNotificationDto notificationDto = CreateNotificationDto.builder()
                .user_id(userId)
                .resource_id("res-enrollment-001")
                .entity_id(enrollmentId)
                .message("Bạn đã đăng ký thành công khóa học '" + courseName + "'")
                .action_url(courseUrl)
                .priority(NotificationPriority.MEDIUM)
                .expired_at(LocalDateTime.now().plusDays(30))
                .build();

        return notificationService.createNotification(notificationDto);
    }

    /**
     * Create a certificate notification
     */
    public CompletableFuture<NotificationResponseDto> createCertificateNotification(
            String userId, String certificateId, String courseName, String certificateUrl) {

        CreateNotificationDto notificationDto = CreateNotificationDto.builder()
                .user_id(userId)
                .resource_id("res-course-001")
                .entity_id(certificateId)
                .message("Chúc mừng! Bạn đã hoàn thành khóa học '" + courseName + "' và nhận được chứng chỉ")
                .action_url(certificateUrl)
                .priority(NotificationPriority.HIGH)
                .expired_at(LocalDateTime.now().plusDays(90))
                .build();

        return notificationService.createNotification(notificationDto);
    }

    /**
     * Create a course approval notification for instructors
     */
    public CompletableFuture<NotificationResponseDto> createCourseApprovalNotification(
            String instructorId, String courseId, String courseName, String courseUrl, boolean approved) {

        String message = approved
                ? "Khóa học '" + courseName + "' của bạn đã được phê duyệt"
                : "Khóa học '" + courseName + "' của bạn cần chỉnh sửa thêm";

        CreateNotificationDto notificationDto = CreateNotificationDto.builder()
                .user_id(instructorId)
                .resource_id("res-course-001")
                .entity_id(courseId)
                .message(message)
                .action_url(courseUrl)
                .priority(NotificationPriority.HIGH)
                .expired_at(LocalDateTime.now().plusDays(30))
                .build();

        return notificationService.createNotification(notificationDto);
    }

    /**
     * Create a refund notification
     */
    public CompletableFuture<NotificationResponseDto> createRefundNotification(
            String userId, String refundId, String courseName, String refundUrl, String status) {

        String message = switch (status.toLowerCase()) {
            case "approved" -> "Yêu cầu hoàn tiền cho khóa học '" + courseName + "' đã được chấp nhận";
            case "rejected" -> "Yêu cầu hoàn tiền cho khóa học '" + courseName + "' đã bị từ chối";
            default -> "Trạng thái yêu cầu hoàn tiền cho khóa học '" + courseName + "' đã được cập nhật";
        };

        CreateNotificationDto notificationDto = CreateNotificationDto.builder()
                .user_id(userId)
                .resource_id("res-refund-001")
                .entity_id(refundId)
                .message(message)
                .action_url(refundUrl)
                .priority(NotificationPriority.MEDIUM)
                .expired_at(LocalDateTime.now().plusDays(30))
                .build();

        return notificationService.createNotification(notificationDto);
    }

    // ===================== ADMIN NOTIFICATION METHODS =====================

    /**
     * Get all users who have a specific permission for notification targeting
     */
    private List<User> getUsersWithPermission(String permissionKey) {
        try {
            Pageable pageable = PageRequest.of(0, 1000); // Get all users (max 1000)
            Page<User> usersPage = userRepository.findUsersWithFilters(
                    null, // search (no filter)
                    null, // role filter (no role restriction)
                    true, // isActive = true only
                    pageable);

            // Filter users by permission using AuthorizationService
            List<User> usersWithPermission = usersPage.getContent().stream()
                    .filter(user -> authorizationService.hasPermission(user, permissionKey))
                    .collect(Collectors.toList());

            log.debug("Found {} users with permission '{}' out of {} total users",
                    usersWithPermission.size(), permissionKey, usersPage.getContent().size());

            return usersWithPermission;
        } catch (Exception e) {
            log.error("Error fetching users with permission '{}': {}", permissionKey, e.getMessage(), e);
            return List.of(); // Return empty list on error
        }
    }

    /**
     * Create notifications for users with specific permission
     */
    private void createNotificationsForUsersWithPermission(String permissionKey, String resourceId,
            String entityId, String message, String actionUrl,
            NotificationPriority priority) {
        List<User> authorizedUsers = getUsersWithPermission(permissionKey);

        if (authorizedUsers.isEmpty()) {
            log.warn("No users found with permission '{}' for notification: {}", permissionKey, message);
            return;
        }

        log.info("Creating notifications for {} users with permission '{}': {}",
                authorizedUsers.size(), permissionKey, message);

        for (User user : authorizedUsers) {
            try {
                CreateNotificationDto notificationDto = CreateNotificationDto.builder()
                        .user_id(user.getId())
                        .resource_id(resourceId)
                        .entity_id(entityId)
                        .message(message)
                        .action_url(actionUrl)
                        .priority(priority)
                        .expired_at(LocalDateTime.now().plusDays(30))
                        .build();

                // Create notification asynchronously
                notificationService.createNotification(notificationDto)
                        .thenAccept(result -> log.debug("Notification created for user {} ({}): {}",
                                user.getId(), permissionKey, result.getId()))
                        .exceptionally(ex -> {
                            log.error("Failed to create notification for user {} ({}): {}",
                                    user.getId(), permissionKey, ex.getMessage(), ex);
                            return null;
                        });
            } catch (Exception e) {
                log.error("Error creating notification for user {} ({}): {}",
                        user.getId(), permissionKey, e.getMessage(), e);
            }
        }
    }

    /**
     * Admin Notification 1: Student thanh toán thành công (MEDIUM priority)
     * Notify users with payment:READ permission when a student makes a successful
     * payment
     */
    public void createAdminStudentPaymentNotification(String paymentId, String studentName,
            String courseName, String paymentAmount) {
        // Validate inputs
        if (paymentId == null || paymentId.trim().isEmpty()) {
            log.error("Cannot create admin payment notification: paymentId is null or empty");
            return;
        }
        if (studentName == null || studentName.trim().isEmpty()) {
            studentName = "Unknown Student";
        }
        if (courseName == null || courseName.trim().isEmpty()) {
            courseName = "Unknown Course";
        }
        if (paymentAmount == null || paymentAmount.trim().isEmpty()) {
            paymentAmount = "Unknown Amount";
        }

        String message = String.format("Sinh viên %s đã thanh toán thành công %s cho khóa học '%s'",
                studentName, paymentAmount, courseName);
        String actionUrl = "/admin/payments/" + paymentId;

        createNotificationsForUsersWithPermission(
                "payment:READ",
                "res-payment-001",
                paymentId,
                message,
                actionUrl,
                NotificationPriority.MEDIUM);

        log.info("Payment notification created for payment: {} - sent to users with payment:READ permission",
                paymentId);
    }

    /**
     * Admin Notification 2: Payment thay đổi trạng thái từ PENDING → COMPLETED
     * (HIGH priority)
     * Notify users with payment:READ permission when payment status changes
     */
    public void createAdminPaymentStatusChangeNotification(String paymentId, String studentName,
            String courseName, String oldStatus,
            String newStatus) {
        // Validate inputs
        if (paymentId == null || paymentId.trim().isEmpty()) {
            log.error("Cannot create admin payment status change notification: paymentId is null or empty");
            return;
        }
        if (studentName == null || studentName.trim().isEmpty()) {
            studentName = "Unknown Student";
        }
        if (courseName == null || courseName.trim().isEmpty()) {
            courseName = "Unknown Course";
        }
        if (oldStatus == null || oldStatus.trim().isEmpty()) {
            oldStatus = "Unknown";
        }
        if (newStatus == null || newStatus.trim().isEmpty()) {
            newStatus = "Unknown";
        }

        String message = String.format("Thanh toán của sinh viên %s cho khóa học '%s' đã thay đổi từ %s thành %s",
                studentName, courseName, oldStatus, newStatus);
        String actionUrl = "/admin/payments/" + paymentId;

        createNotificationsForUsersWithPermission(
                "payment:READ",
                "res-payment-001",
                paymentId,
                message,
                actionUrl,
                NotificationPriority.HIGH);

        log.info(
                "Payment status change notification created for payment: {} - sent to users with payment:READ permission",
                paymentId);
    }

    /**
     * Admin Notification 3: Có khóa học mới cần duyệt (HIGH priority)
     * Notify users with course:APPROVE permission when a new course needs approval
     */
    public void createAdminCourseApprovalNeededNotification(String courseId, String courseName,
            String instructorName) {
        // Validate inputs
        if (courseId == null || courseId.trim().isEmpty()) {
            log.error("Cannot create admin course approval notification: courseId is null or empty");
            return;
        }
        if (courseName == null || courseName.trim().isEmpty()) {
            courseName = "Unknown Course";
        }
        if (instructorName == null || instructorName.trim().isEmpty()) {
            instructorName = "Unknown Instructor";
        }

        String message = String.format("Khóa học mới '%s' của giảng viên %s cần được duyệt",
                courseName, instructorName);
        String actionUrl = "/admin/courses/review-course/" + courseId;

        createNotificationsForUsersWithPermission(
                "course:APPROVE",
                "res-course-001",
                courseId,
                message,
                actionUrl,
                NotificationPriority.HIGH);

        log.info("Course approval notification created for course: {} - sent to users with course:APPROVE permission",
                courseId);
    }

    /**
     * Admin Notification 4: Có đơn đăng ký instructor cần duyệt (HIGH priority)
     * Notify users with user:READ permission when a new instructor application
     * needs approval
     */
    public void createAdminInstructorApplicationNotification(String applicationId, String applicantName,
            String applicantEmail) {
        // Validate inputs
        if (applicationId == null || applicationId.trim().isEmpty()) {
            log.error("Cannot create admin instructor application notification: applicationId is null or empty");
            return;
        }
        if (applicantName == null || applicantName.trim().isEmpty()) {
            applicantName = "Unknown Applicant";
        }
        if (applicantEmail == null || applicantEmail.trim().isEmpty()) {
            applicantEmail = "unknown@email.com";
        }

        String message = String.format("Đơn đăng ký giảng viên mới từ %s (%s) cần được duyệt",
                applicantName, applicantEmail);
        String actionUrl = "/admin/instructors/applications/" + applicationId;

        createNotificationsForUsersWithPermission(
                "instructor_application:READ",
                "res-instructor-application-001",
                applicationId,
                message,
                actionUrl,
                NotificationPriority.HIGH);

        log.info(
                "Instructor application notification created for application: {} - sent to users with user:READ permission",
                applicationId);
    }
}
