package project.ktc.springboot_app.notification.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.notification.dto.CreateNotificationDto;
import project.ktc.springboot_app.notification.dto.NotificationResponseDto;
import project.ktc.springboot_app.notification.interfaces.NotificationService;
import project.ktc.springboot_app.notification.utils.NotificationHelper;

/** Controller for notification operations */
@RestController
@RequestMapping("/api/admin/notifications")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification API", description = "API for managing notifications")
public class NotificationController {

  private final NotificationService notificationService;
  private final NotificationHelper notificationHelper;

  @PostMapping
  @Operation(
      summary = "Create a new notification",
      description =
          "Creates a new notification for a given user. Only users with the ADMIN role are authorized to call this API.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Notification created successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request - Missing or invalid fields"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Caller is not ADMIN"),
        @ApiResponse(responseCode = "404", description = "Not Found - User not found")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto>>
      createNotification(@Valid @RequestBody CreateNotificationDto createNotificationDto) {

    log.info(
        "Received request to create notification for user: {}", createNotificationDto.getUser_id());

    try {
      NotificationResponseDto notification =
          notificationService.createNotification(createNotificationDto).get(); // Convert
      // async
      // to
      // sync

      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> response =
          project.ktc.springboot_app.common.dto.ApiResponse.created(
              notification, "Notification created successfully");
      return ResponseEntity.status(201).body(response);

    } catch (Exception e) {
      log.error("Error creating notification: {}", e.getMessage(), e);

      // Handle specific exception types
      if (e.getCause() != null) {
        Throwable cause = e.getCause();

        if (cause
            instanceof project.ktc.springboot_app.common.exception.ResourceNotFoundException) {
          project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> errorResponse =
              project.ktc.springboot_app.common.dto.ApiResponse.error(404, cause.getMessage());
          return ResponseEntity.status(404).body(errorResponse);
        }
      }

      // Default server error
      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> errorResponse =
          project.ktc.springboot_app.common.dto.ApiResponse.error(
              500, "Internal server error: " + e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  /**
   * Create notification synchronously for internal system use This endpoint can be used by other
   * services within the system
   */
  @PostMapping("/internal")
  @Operation(
      summary = "Create notification internally",
      description = "Creates a notification synchronously for internal system use")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto>>
      createNotificationInternal(@Valid @RequestBody CreateNotificationDto createNotificationDto) {

    log.info(
        "Received internal request to create notification for user: {}",
        createNotificationDto.getUser_id());

    try {
      NotificationResponseDto notification =
          notificationService.createNotificationSync(createNotificationDto);

      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> response =
          project.ktc.springboot_app.common.dto.ApiResponse.created(
              notification, "Notification created successfully");

      return ResponseEntity.status(201).body(response);

    } catch (project.ktc.springboot_app.common.exception.ResourceNotFoundException e) {
      log.error("User not found: {}", e.getMessage());
      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> errorResponse =
          project.ktc.springboot_app.common.dto.ApiResponse.error(404, e.getMessage());
      return ResponseEntity.status(404).body(errorResponse);

    } catch (Exception e) {
      log.error("Error creating notification internally: {}", e.getMessage(), e);
      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> errorResponse =
          project.ktc.springboot_app.common.dto.ApiResponse.error(500, "Internal server error");
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  /** Test endpoint to create a sample notification (for development/testing) */
  @PostMapping("/test")
  @Operation(
      summary = "Create test notification",
      description = "Creates a test notification for development purposes")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto>>
      createTestNotification() {

    log.info("Creating test notification");

    try {
      CreateNotificationDto testNotification =
          CreateNotificationDto.builder()
              .user_id("user-001")
              .resource_id("res-payment-001")
              .entity_id("payment-001")
              .message(
                  "Test notification: Thanh toán thành công cho khóa học 'Advanced React Development'")
              .action_url("/courses/advanced-react-development")
              .priority(project.ktc.springboot_app.notification.entity.NotificationPriority.HIGH)
              .expired_at(java.time.LocalDateTime.now().plusDays(30))
              .build();

      NotificationResponseDto notification =
          notificationService.createNotificationSync(testNotification);

      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> response =
          project.ktc.springboot_app.common.dto.ApiResponse.created(
              notification, "Test notification created successfully");

      return ResponseEntity.status(201).body(response);

    } catch (Exception e) {
      log.error("Error creating test notification: {}", e.getMessage(), e);
      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> errorResponse =
          project.ktc.springboot_app.common.dto.ApiResponse.error(
              500, "Internal server error: " + e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  /** Simple GET test endpoint (no authentication required) */
  @GetMapping("/test")
  @Operation(
      summary = "Simple test endpoint",
      description = "GET endpoint for quick testing without authentication")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<String>> simpleTest() {
    return ResponseEntity.ok(
        project.ktc.springboot_app.common.dto.ApiResponse.success(
            "Test passed - Notification system is functional",
            "Notification API is working correctly"));
  }

  // ===================== NOTIFICATION HELPER TEST ENDPOINTS
  // =====================

  /** Test payment success notification */
  @PostMapping("/test/payment-success")
  @Operation(
      summary = "Test payment success notification",
      description = "Creates a test payment success notification")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto>>
      testPaymentSuccess() {
    log.info("Testing payment success notification");

    try {
      NotificationResponseDto notification =
          notificationHelper
              .createPaymentSuccessNotification(
                  "user-001", // userId
                  "payment-001", // paymentId
                  "Advanced Spring Boot", // courseName
                  "/courses/advanced-spring-boot",
                  "course-001" // courseId
                  )
              .get(); // Convert async to sync

      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> response =
          project.ktc.springboot_app.common.dto.ApiResponse.created(
              notification, "Payment success notification created successfully");
      return ResponseEntity.status(201).body(response);
    } catch (Exception e) {
      log.error("Error creating payment success notification: {}", e.getMessage(), e);
      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> errorResponse =
          project.ktc.springboot_app.common.dto.ApiResponse.error(500, "Error: " + e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  /** Test enrollment notification */
  @PostMapping("/test/enrollment")
  @Operation(
      summary = "Test enrollment notification",
      description = "Creates a test enrollment notification")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto>>
      testEnrollment() {
    log.info("Testing enrollment notification");

    try {
      NotificationResponseDto notification =
          notificationHelper
              .createEnrollmentNotification(
                  "user-002", // userId
                  "enrollment-001", // enrollmentId
                  "React Development", // courseName
                  "/courses/react-development" // courseUrl
                  )
              .get(); // Convert async to sync

      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> response =
          project.ktc.springboot_app.common.dto.ApiResponse.created(
              notification, "Enrollment notification created successfully");
      return ResponseEntity.status(201).body(response);
    } catch (Exception e) {
      log.error("Error creating enrollment notification: {}", e.getMessage(), e);
      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> errorResponse =
          project.ktc.springboot_app.common.dto.ApiResponse.error(500, "Error: " + e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  /** Test certificate notification */
  @PostMapping("/test/certificate")
  @Operation(
      summary = "Test certificate notification",
      description = "Creates a test certificate notification")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto>>
      testCertificate() {
    log.info("Testing certificate notification");

    try {
      NotificationResponseDto notification =
          notificationHelper
              .createCertificateNotification(
                  "user-003", // userId
                  "certificate-001", // certificateId
                  "Java Programming", // courseName
                  "/certificates/java-programming" // certificateUrl
                  )
              .get(); // Convert async to sync

      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> response =
          project.ktc.springboot_app.common.dto.ApiResponse.created(
              notification, "Certificate notification created successfully");
      return ResponseEntity.status(201).body(response);
    } catch (Exception e) {
      log.error("Error creating certificate notification: {}", e.getMessage(), e);
      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> errorResponse =
          project.ktc.springboot_app.common.dto.ApiResponse.error(500, "Error: " + e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  /** Test refund notification */
  @PostMapping("/test/refund")
  @Operation(
      summary = "Test refund notification",
      description = "Creates a test refund notification")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto>>
      testRefund() {
    log.info("Testing refund notification");

    try {
      NotificationResponseDto notification =
          notificationHelper
              .createRefundNotification(
                  "user-004", // userId
                  "refund-001", // refundId
                  "Database Management", // courseName
                  "/refunds/refund-001", // refundUrl
                  "approved" // status
                  )
              .get(); // Convert async to sync

      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> response =
          project.ktc.springboot_app.common.dto.ApiResponse.created(
              notification, "Refund notification created successfully");
      return ResponseEntity.status(201).body(response);
    } catch (Exception e) {
      log.error("Error creating refund notification: {}", e.getMessage(), e);
      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> errorResponse =
          project.ktc.springboot_app.common.dto.ApiResponse.error(500, "Error: " + e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  // ===================== ADMIN NOTIFICATION TEST ENDPOINTS =====================

  /** Test admin student payment notification */
  @PostMapping("/test/admin/student-payment")
  @Operation(
      summary = "Test admin student payment notification",
      description = "Creates a test admin student payment notification")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<String>>
      testAdminStudentPayment() {
    log.info("Testing admin student payment notification");

    try {
      notificationHelper.createAdminStudentPaymentNotification(
          "payment-002", // paymentId
          "Nguyễn Văn A", // studentName
          "Web Development", // courseName
          new BigDecimal("199.99") // amount
          );

      return ResponseEntity.ok(
          project.ktc.springboot_app.common.dto.ApiResponse.success(
              "Admin student payment notification sent successfully",
              "Notification sent to all users with payment:READ permission"));

    } catch (Exception e) {
      log.error("Error creating admin student payment notification: {}", e.getMessage(), e);
      return ResponseEntity.status(500)
          .body(
              project.ktc.springboot_app.common.dto.ApiResponse.error(
                  500, "Error: " + e.getMessage()));
    }
  }

  /** Test admin payment status change notification */
  @PostMapping("/test/admin/payment-status-change")
  @Operation(
      summary = "Test admin payment status change notification",
      description = "Creates a test admin payment status change notification")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<String>>
      testAdminPaymentStatusChange() {
    log.info("Testing admin payment status change notification");

    try {
      notificationHelper.createAdminPaymentStatusChangeNotification(
          "payment-003", // paymentId
          "Trần Thị B", // studentName
          "Mobile App Development", // courseName
          "PENDING", // oldStatus
          "COMPLETED" // newStatus
          );

      return ResponseEntity.ok(
          project.ktc.springboot_app.common.dto.ApiResponse.success(
              "Admin payment status change notification sent successfully",
              "Notification sent to all users with payment:READ permission"));

    } catch (Exception e) {
      log.error("Error creating admin payment status change notification: {}", e.getMessage(), e);
      return ResponseEntity.status(500)
          .body(
              project.ktc.springboot_app.common.dto.ApiResponse.error(
                  500, "Error: " + e.getMessage()));
    }
  }

  /** Test admin course approval needed notification */
  @PostMapping("/test/admin/course-approval-needed")
  @Operation(
      summary = "Test admin course approval needed notification",
      description = "Creates a test admin course approval needed notification")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<String>>
      testAdminCourseApprovalNeeded() {
    log.info("Testing admin course approval needed notification");

    try {
      notificationHelper.createAdminCourseApprovalNeededNotification(
          "course-003", // courseId
          "AI and Machine Learning", // courseName
          "Dr. Lê Văn C" // instructorName
          );

      return ResponseEntity.ok(
          project.ktc.springboot_app.common.dto.ApiResponse.success(
              "Admin course approval needed notification sent successfully",
              "Notification sent to all users with course:APPROVE permission"));

    } catch (Exception e) {
      log.error("Error creating admin course approval needed notification: {}", e.getMessage(), e);
      return ResponseEntity.status(500)
          .body(
              project.ktc.springboot_app.common.dto.ApiResponse.error(
                  500, "Error: " + e.getMessage()));
    }
  }

  /** Test admin instructor application notification */
  @PostMapping("/test/admin/instructor-application")
  @Operation(
      summary = "Test admin instructor application notification",
      description = "Creates a test admin instructor application notification")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<String>>
      testAdminInstructorApplication() {
    log.info("Testing admin instructor application notification");

    try {
      notificationHelper.createAdminInstructorApplicationNotification(
          "application-001", // applicationId
          "Phạm Thị D", // applicantName
          "phamthid@example.com" // applicantEmail
          );

      return ResponseEntity.ok(
          project.ktc.springboot_app.common.dto.ApiResponse.success(
              "Admin instructor application notification sent successfully",
              "Notification sent to all users with instructor_application:READ permission"));

    } catch (Exception e) {
      log.error("Error creating admin instructor application notification: {}", e.getMessage(), e);
      return ResponseEntity.status(500)
          .body(
              project.ktc.springboot_app.common.dto.ApiResponse.error(
                  500, "Error: " + e.getMessage()));
    }
  }

  /** Test all notifications at once */
  @PostMapping("/test/all")
  @Operation(
      summary = "Test all notifications",
      description = "Creates all types of test notifications")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<String>>
      testAllNotifications() {
    log.info("Testing all notification types");

    try {
      // Test student notifications
      notificationHelper
          .createPaymentSuccessNotification(
              "user-100",
              "payment-100",
              "Complete Web Development",
              "/courses/complete-web-dev",
              "course-001")
          .get();

      notificationHelper
          .createEnrollmentNotification(
              "user-101", "enrollment-100", "Full Stack Development", "/courses/full-stack-dev")
          .get();

      notificationHelper
          .createCertificateNotification(
              "user-102", "certificate-100", "DevOps Essentials", "/certificates/devops-essentials")
          .get();

      notificationHelper
          .createRefundNotification(
              "user-103", "refund-100", "Data Science", "/refunds/refund-100", "approved")
          .get();

      // Test admin notifications
      notificationHelper.createAdminStudentPaymentNotification(
          "payment-101", "Student Test", "Test Course", new BigDecimal("99.99"));

      notificationHelper.createAdminPaymentStatusChangeNotification(
          "payment-102", "Another Student", "Another Course", "PENDING", "COMPLETED");

      notificationHelper.createAdminCourseApprovalNeededNotification(
          "course-101", "New Test Course", "Test Instructor");

      notificationHelper.createAdminInstructorApplicationNotification(
          "application-100", "Test Applicant", "test@example.com");

      return ResponseEntity.ok(
          project.ktc.springboot_app.common.dto.ApiResponse.success(
              "All notification types created successfully",
              "Created student notifications and admin notifications for users with appropriate permissions"));

    } catch (Exception e) {
      log.error("Error creating all notification types: {}", e.getMessage(), e);
      return ResponseEntity.status(500)
          .body(
              project.ktc.springboot_app.common.dto.ApiResponse.error(
                  500, "Error: " + e.getMessage()));
    }
  }

  // ===================== INSTRUCTOR NOTIFICATION TEST ENDPOINTS
  // =====================

  /** Test instructor course approved notification */
  @PostMapping("/test/instructor/course-approved")
  @Operation(
      summary = "Test instructor course approved notification",
      description = "Creates a test instructor course approved notification")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto>>
      testInstructorCourseApproved() {
    log.info("Testing instructor course approved notification");

    try {
      NotificationResponseDto notification =
          notificationHelper
              .createInstructorCourseApprovedNotification(
                  "user-003", // instructorId
                  "course-001", // courseId
                  "Advanced Java Programming", // courseName
                  "/instructor/courses/course-001" // courseUrl
                  )
              .get(); // Convert async to sync

      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> response =
          project.ktc.springboot_app.common.dto.ApiResponse.created(
              notification, "Instructor course approved notification created successfully");
      return ResponseEntity.status(201).body(response);
    } catch (Exception e) {
      log.error("Error creating instructor course approved notification: {}", e.getMessage(), e);
      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> errorResponse =
          project.ktc.springboot_app.common.dto.ApiResponse.error(500, "Error: " + e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  /** Test instructor course rejected notification */
  @PostMapping("/test/instructor/course-rejected")
  @Operation(
      summary = "Test instructor course rejected notification",
      description = "Creates a test instructor course rejected notification")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto>>
      testInstructorCourseRejected() {
    log.info("Testing instructor course rejected notification");

    try {
      NotificationResponseDto notification =
          notificationHelper
              .createInstructorCourseRejectedNotification(
                  "user-003", // instructorId
                  "course-002", // courseId
                  "Python for Beginners", // courseName
                  "/instructor/courses/course-002", // courseUrl
                  "Nội dung chưa đầy đủ, cần bổ sung thêm video demo" // rejectionReason
                  )
              .get(); // Convert async to sync

      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> response =
          project.ktc.springboot_app.common.dto.ApiResponse.created(
              notification, "Instructor course rejected notification created successfully");
      return ResponseEntity.status(201).body(response);
    } catch (Exception e) {
      log.error("Error creating instructor course rejected notification: {}", e.getMessage(), e);
      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> errorResponse =
          project.ktc.springboot_app.common.dto.ApiResponse.error(500, "Error: " + e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  /** Test instructor new student enrollment notification */
  @PostMapping("/test/instructor/new-student")
  @Operation(
      summary = "Test instructor new student notification",
      description = "Creates a test instructor new student enrollment notification")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto>>
      testInstructorNewStudent() {
    log.info("Testing instructor new student enrollment notification");

    try {
      NotificationResponseDto notification =
          notificationHelper
              .createInstructorNewStudentEnrollmentNotification(
                  "user-003", // instructorId
                  "course-003", // courseId
                  "React Native Development", // courseName
                  "Nguyễn Văn Minh", // studentName
                  "106c78bb-bf04-44a8-a3ba-2fa7f9eb5e54",
                  "enrollment-003" // enrollmentId
                  )
              .get(); // Convert async to sync

      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> response =
          project.ktc.springboot_app.common.dto.ApiResponse.created(
              notification, "Instructor new student notification created successfully");
      return ResponseEntity.status(201).body(response);
    } catch (Exception e) {
      log.error("Error creating instructor new student notification: {}", e.getMessage(), e);
      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> errorResponse =
          project.ktc.springboot_app.common.dto.ApiResponse.error(500, "Error: " + e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  /** Test all instructor notifications at once */
  @PostMapping("/test/instructor/all")
  @Operation(
      summary = "Test all instructor notifications",
      description = "Creates all types of instructor test notifications")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<String>>
      testAllInstructorNotifications() {
    log.info("Testing all instructor notification types");

    try {
      // Test instructor notifications
      notificationHelper
          .createInstructorCourseApprovedNotification(
              "instructor-100",
              "course-100",
              "Full Stack Development",
              "/instructor/courses/course-100")
          .get();

      notificationHelper
          .createInstructorCourseRejectedNotification(
              "instructor-101",
              "course-101",
              "Mobile App Development",
              "/instructor/courses/course-101",
              "Cần bổ sung thêm tài liệu và video thực hành")
          .get();

      notificationHelper
          .createInstructorNewStudentEnrollmentNotification(
              "instructor-102",
              "course-102",
              "DevOps Essentials",
              "Phạm Văn Hòa",
              "student-102",
              "enrollment-102")
          .get();

      return ResponseEntity.ok(
          project.ktc.springboot_app.common.dto.ApiResponse.success(
              "All instructor notifications created successfully",
              "Created course approved, course rejected, new student, and new review notifications"));

    } catch (Exception e) {
      log.error("Error creating all instructor notification types: {}", e.getMessage(), e);
      return ResponseEntity.status(500)
          .body(
              project.ktc.springboot_app.common.dto.ApiResponse.error(
                  500, "Error: " + e.getMessage()));
    }
  }

  // ===================== STUDENT NOTIFICATION TEST ENDPOINTS
  // =====================

  /** Test student instructor application approved notification */
  @PostMapping("/test/student/instructor-approved")
  @Operation(
      summary = "Test student instructor application approved notification",
      description = "Creates a test student instructor application approved notification")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto>>
      testStudentInstructorApproved() {
    log.info("Testing student instructor application approved notification");

    try {
      NotificationResponseDto notification =
          notificationHelper
              .createStudentInstructorApplicationApprovedNotification(
                  "user-006", // studentId
                  "app-002" // applicationId
                  )
              .get(); // Convert async to sync

      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> response =
          project.ktc.springboot_app.common.dto.ApiResponse.created(
              notification,
              "Student instructor application approved notification created successfully");
      return ResponseEntity.status(201).body(response);
    } catch (Exception e) {
      log.error(
          "Error creating student instructor application approved notification: {}",
          e.getMessage(),
          e);
      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> errorResponse =
          project.ktc.springboot_app.common.dto.ApiResponse.error(500, "Error: " + e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  /** Test student instructor application rejected notification */
  @PostMapping("/test/student/instructor-rejected")
  @Operation(
      summary = "Test student instructor application rejected notification",
      description = "Creates a test student instructor application rejected notification")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto>>
      testStudentInstructorRejected() {
    log.info("Testing student instructor application rejected notification");

    try {
      NotificationResponseDto notification =
          notificationHelper
              .createStudentInstructorApplicationRejectedNotification(
                  "user-008", // studentId
                  "app-005", // applicationId
                  "Tài liệu chưa đầy đủ, cần bổ sung chứng chỉ và kinh nghiệm giảng dạy" // rejectionReason
                  )
              .get(); // Convert async to sync

      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> response =
          project.ktc.springboot_app.common.dto.ApiResponse.created(
              notification,
              "Student instructor application rejected notification created successfully");
      return ResponseEntity.status(201).body(response);
    } catch (Exception e) {
      log.error(
          "Error creating student instructor application rejected notification: {}",
          e.getMessage(),
          e);
      project.ktc.springboot_app.common.dto.ApiResponse<NotificationResponseDto> errorResponse =
          project.ktc.springboot_app.common.dto.ApiResponse.error(500, "Error: " + e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  /** Test all student notifications at once */
  @PostMapping("/test/student/all")
  @Operation(
      summary = "Test all student notifications",
      description = "Creates all types of student test notifications")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<String>>
      testAllStudentNotifications() {
    log.info("Testing all student notification types");

    try {
      // Test student notifications
      notificationHelper
          .createStudentInstructorApplicationApprovedNotification("student-100", "application-100")
          .get();

      notificationHelper
          .createStudentInstructorApplicationRejectedNotification(
              "student-101",
              "application-101",
              "Hồ sơ chưa đạt yêu cầu, vui lòng cập nhật CV và portfolio")
          .get();

      return ResponseEntity.ok(
          project.ktc.springboot_app.common.dto.ApiResponse.success(
              "All student notifications created successfully",
              "Created instructor application approved and rejected notifications"));

    } catch (Exception e) {
      log.error("Error creating all student notification types: {}", e.getMessage(), e);
      return ResponseEntity.status(500)
          .body(
              project.ktc.springboot_app.common.dto.ApiResponse.error(
                  500, "Error: " + e.getMessage()));
    }
  }

  /** Test admin notification filtering with filter-type-001 (ALL access) */
  @PostMapping("/test/admin/filter-type-001")
  @Operation(
      summary = "Test admin notification filtering with filter-type-001",
      description = "Tests admin notification filtering to only users with ALL access permissions")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<String>>
      testAdminNotificationFiltering() {
    log.info("Testing admin notification filtering with filter-type-001 (ALL access)");

    try {
      // Test admin payment notification (uses admin filtering with filter-type-001)
      notificationHelper.createAdminStudentPaymentNotification(
          "payment-test-001", // paymentId
          "Nguyễn Văn A", // studentName
          "Spring Boot Advanced", // courseName
          new BigDecimal("1500000") // amount
          );

      return ResponseEntity.ok(
          project.ktc.springboot_app.common.dto.ApiResponse.success(
              "Admin notification filtering test completed",
              "Admin payment notification created and sent only to users with filter-type-001 (ALL access) permissions"));

    } catch (Exception e) {
      log.error("Error testing admin notification filtering: {}", e.getMessage(), e);
      return ResponseEntity.status(500)
          .body(
              project.ktc.springboot_app.common.dto.ApiResponse.error(
                  500, "Error: " + e.getMessage()));
    }
  }
}
