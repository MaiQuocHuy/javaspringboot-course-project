package project.ktc.springboot_app.notification.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.notification.dto.CreateNotificationDto;
import project.ktc.springboot_app.notification.dto.NotificationResponseDto;
import project.ktc.springboot_app.notification.entity.NotificationPriority;
import project.ktc.springboot_app.notification.interfaces.NotificationService;
import project.ktc.springboot_app.permission.services.AuthorizationService;
import project.ktc.springboot_app.user.repositories.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationHelperAdminTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private NotificationHelper notificationHelper;

    private User adminUser1;
    private User adminUser2;
    private NotificationResponseDto mockResponse;

    @BeforeEach
    void setUp() {
        adminUser1 = new User();
        adminUser1.setId("admin-001");
        adminUser1.setName("Admin One");
        adminUser1.setEmail("admin1@ktc.com");

        adminUser2 = new User();
        adminUser2.setId("admin-002");
        adminUser2.setName("Admin Two");
        adminUser2.setEmail("admin2@ktc.com");

        mockResponse = new NotificationResponseDto();
        mockResponse.setId("notification-123");
    }

    @Test
    void testCreateAdminStudentPaymentNotification() {
        // Arrange
        List<User> usersWithPermission = Arrays.asList(adminUser1, adminUser2);
        Page<User> usersPage = new PageImpl<>(usersWithPermission);

        when(userRepository.findUsersWithFilters(
                isNull(), isNull(), eq(true), any(Pageable.class)))
                .thenReturn(usersPage);

        when(authorizationService.hasPermission(any(User.class), eq("payment:READ")))
                .thenReturn(true);

        when(notificationService.createNotification(any(CreateNotificationDto.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act
        notificationHelper.createAdminStudentPaymentNotification(
                "payment-123",
                "Nguyễn Văn A",
                "Spring Boot Course",
                "1,200,000 VND");

        // Assert
        verify(userRepository).findUsersWithFilters(
                isNull(), isNull(), eq(true), any(Pageable.class));
        verify(authorizationService, times(2)).hasPermission(any(User.class), eq("payment:READ"));
        verify(notificationService, times(2)).createNotification(any(CreateNotificationDto.class));
    }

    @Test
    void testCreateAdminPaymentStatusChangeNotification() {
        // Arrange
        List<User> usersWithPermission = Arrays.asList(adminUser1);
        Page<User> usersPage = new PageImpl<>(usersWithPermission);

        when(userRepository.findUsersWithFilters(
                isNull(), isNull(), eq(true), any(Pageable.class)))
                .thenReturn(usersPage);

        when(authorizationService.hasPermission(any(User.class), eq("payment:READ")))
                .thenReturn(true);

        when(notificationService.createNotification(any(CreateNotificationDto.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act
        notificationHelper.createAdminPaymentStatusChangeNotification(
                "payment-456",
                "Trần Thị B",
                "React Course",
                "PENDING",
                "COMPLETED");

        // Assert
        verify(userRepository).findUsersWithFilters(
                isNull(), isNull(), eq(true), any(Pageable.class));
        verify(authorizationService).hasPermission(any(User.class), eq("payment:READ"));
        verify(notificationService).createNotification(argThat(dto -> dto.getPriority() == NotificationPriority.HIGH &&
                dto.getMessage().contains("PENDING thành COMPLETED")));
    }

    @Test
    void testCreateAdminCourseApprovalNeededNotification() {
        // Arrange
        List<User> usersWithPermission = Arrays.asList(adminUser1, adminUser2);
        Page<User> usersPage = new PageImpl<>(usersWithPermission);

        when(userRepository.findUsersWithFilters(
                isNull(), isNull(), eq(true), any(Pageable.class)))
                .thenReturn(usersPage);

        when(authorizationService.hasPermission(any(User.class), eq("course:APPROVE")))
                .thenReturn(true);

        when(notificationService.createNotification(any(CreateNotificationDto.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act
        notificationHelper.createAdminCourseApprovalNeededNotification(
                "course-789",
                "Java Programming",
                "Lê Văn C");

        // Assert
        verify(userRepository).findUsersWithFilters(
                isNull(), isNull(), eq(true), any(Pageable.class));
        verify(authorizationService, times(2)).hasPermission(any(User.class), eq("course:APPROVE"));
        verify(notificationService, times(2))
                .createNotification(argThat(dto -> dto.getPriority() == NotificationPriority.HIGH &&
                        dto.getAction_url().equals("/admin/courses/review-course/course-789") &&
                        dto.getResource_id().equals("res-course-001")));
    }

    @Test
    void testCreateAdminInstructorApplicationNotification() {
        // Arrange
        List<User> usersWithPermission = Arrays.asList(adminUser1);
        Page<User> usersPage = new PageImpl<>(usersWithPermission);

        when(userRepository.findUsersWithFilters(
                isNull(), isNull(), eq(true), any(Pageable.class)))
                .thenReturn(usersPage);

        when(authorizationService.hasPermission(any(User.class), eq("user:READ")))
                .thenReturn(true);

        when(notificationService.createNotification(any(CreateNotificationDto.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act
        notificationHelper.createAdminInstructorApplicationNotification(
                "app-012",
                "Phạm Thị D",
                "pham.d@example.com");

        // Assert
        verify(userRepository).findUsersWithFilters(
                isNull(), isNull(), eq(true), any(Pageable.class));
        verify(authorizationService).hasPermission(any(User.class), eq("user:READ"));
        verify(notificationService).createNotification(argThat(dto -> dto.getPriority() == NotificationPriority.HIGH &&
                dto.getAction_url().equals("/admin/instructors/applications/app-012") &&
                dto.getMessage().contains("Phạm Thị D") &&
                dto.getMessage().contains("pham.d@example.com") &&
                dto.getResource_id().equals("res-instructor-application-001")));
    }

    @Test
    void testNoUsersWithPermissionFound() {
        // Arrange
        Page<User> emptyPage = new PageImpl<>(List.of());

        when(userRepository.findUsersWithFilters(
                isNull(), isNull(), eq(true), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        notificationHelper.createAdminStudentPaymentNotification(
                "payment-123",
                "Student Name",
                "Course Name",
                "100,000 VND");

        // Assert
        verify(userRepository).findUsersWithFilters(
                isNull(), isNull(), eq(true), any(Pageable.class));
        verify(notificationService, never()).createNotification(any(CreateNotificationDto.class));
    }

    @Test
    void testUsersWithoutPermissionFiltered() {
        // Arrange
        List<User> allUsers = Arrays.asList(adminUser1, adminUser2);
        Page<User> usersPage = new PageImpl<>(allUsers);

        when(userRepository.findUsersWithFilters(
                isNull(), isNull(), eq(true), any(Pageable.class)))
                .thenReturn(usersPage);

        // Only adminUser1 has permission
        when(authorizationService.hasPermission(eq(adminUser1), eq("payment:READ")))
                .thenReturn(true);
        when(authorizationService.hasPermission(eq(adminUser2), eq("payment:READ")))
                .thenReturn(false);

        when(notificationService.createNotification(any(CreateNotificationDto.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act
        notificationHelper.createAdminStudentPaymentNotification(
                "payment-123",
                "Student Name",
                "Course Name",
                "100,000 VND");

        // Assert
        verify(userRepository).findUsersWithFilters(
                isNull(), isNull(), eq(true), any(Pageable.class));
        verify(authorizationService, times(2)).hasPermission(any(User.class), eq("payment:READ"));
        verify(notificationService, times(1)).createNotification(any(CreateNotificationDto.class)); // Only 1 user has
                                                                                                    // permission
    }

    @Test
    void testRepositoryErrorHandling() {
        // Arrange
        when(userRepository.findUsersWithFilters(
                isNull(), isNull(), eq(true), any(Pageable.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert - Should not throw exception
        notificationHelper.createAdminStudentPaymentNotification(
                "payment-123",
                "Student Name",
                "Course Name",
                "100,000 VND");

        verify(notificationService, never()).createNotification(any(CreateNotificationDto.class));
    }

    @Test
    void testValidationWithNullInputs() {
        // Test null paymentId - should return early
        notificationHelper.createAdminStudentPaymentNotification(
                null, "Student", "Course", "100 VND");

        // Test empty paymentId - should return early
        notificationHelper.createAdminStudentPaymentNotification(
                "", "Student", "Course", "100 VND");

        // Test null courseId - should return early
        notificationHelper.createAdminCourseApprovalNeededNotification(
                null, "Course", "Instructor");

        // Verify no notifications were created
        verify(userRepository, never()).findUsersWithFilters(any(), any(), any(), any());
        verify(notificationService, never()).createNotification(any(CreateNotificationDto.class));
    }

    @Test
    void testValidationWithDefaultValues() {
        // Arrange
        List<User> usersWithPermission = Arrays.asList(adminUser1);
        Page<User> usersPage = new PageImpl<>(usersWithPermission);

        when(userRepository.findUsersWithFilters(
                isNull(), isNull(), eq(true), any(Pageable.class)))
                .thenReturn(usersPage);

        when(authorizationService.hasPermission(any(User.class), eq("payment:READ")))
                .thenReturn(true);

        when(notificationService.createNotification(any(CreateNotificationDto.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act - Test with null/empty non-required fields
        notificationHelper.createAdminStudentPaymentNotification(
                "payment-123",
                null, // null studentName
                "", // empty courseName
                null // null paymentAmount
        );

        // Assert - Should use default values and create notification
        verify(notificationService).createNotification(argThat(dto -> dto.getMessage().contains("Unknown Student") &&
                dto.getMessage().contains("Unknown Course") &&
                dto.getMessage().contains("Unknown Amount")));
    }
}
