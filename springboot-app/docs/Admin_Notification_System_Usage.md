# Permission-Based Notification System Usage Guide

## Overview

The enhanced `NotificationHelper` class now supports creating notifications for users based on **permissions** rather than hardcoded roles. This approach is more flexible and follows the existing RBAC (Role-Based Access Control) architecture.

## Permission-Based Notification Methods

### 1. Student Payment Success Notification (MEDIUM Priority)

**Required Permission:** `payment:READ`

```java
@Autowired
private NotificationHelper notificationHelper;

// When a student makes a successful payment
notificationHelper.createAdminStudentPaymentNotification(
    paymentId,        // "payment-uuid-123"
    studentName,      // "Nguyễn Văn A"
    courseName,       // "Spring Boot Fundamentals"
    paymentAmount     // "1,200,000 VND"
);
```

**Generated Message:**

```
"Sinh viên Nguyễn Văn A đã thanh toán thành công 1,200,000 VND cho khóa học 'Spring Boot Fundamentals'"
```

**Target Users:** All users with `payment:READ` permission  
**Action URL:** `/admin/payments/payment-uuid-123`

### 2. Payment Status Change Notification (HIGH Priority)

**Required Permission:** `payment:READ`

```java
// When payment status changes from PENDING to COMPLETED
notificationHelper.createAdminPaymentStatusChangeNotification(
    paymentId,        // "payment-uuid-456"
    studentName,      // "Trần Thị B"
    courseName,       // "React Advanced"
    oldStatus,        // "PENDING"
    newStatus         // "COMPLETED"
);
```

**Generated Message:**

```
"Thanh toán của sinh viên Trần Thị B cho khóa học 'React Advanced' đã thay đổi từ PENDING thành COMPLETED"
```

**Target Users:** All users with `payment:READ` permission  
**Action URL:** `/admin/payments/payment-uuid-456`

### 3. Course Approval Needed Notification (HIGH Priority)

**Required Permission:** `course:APPROVE`

```java
// When a course needs approval
notificationHelper.createAdminCourseApprovalNeededNotification(
    courseId,         // "course-uuid-789"
    courseName,       // "Java Programming Basics"
    instructorName    // "Lê Văn C"
);
```

**Generated Message:**

```
"Khóa học mới 'Java Programming Basics' của giảng viên Lê Văn C cần được duyệt"
```

**Target Users:** All users with `course:APPROVE` permission  
**Action URL:** `/admin/courses/review-course/course-uuid-789`

### 4. Instructor Application Notification (HIGH Priority)

**Required Permission:** `user:READ`

```java
// When an instructor application needs approval
notificationHelper.createAdminInstructorApplicationNotification(
    applicationId,    // "application-uuid-012"
    applicantName,    // "Phạm Thị D"
    applicantEmail    // "pham.d@example.com"
);
```

**Generated Message:**

```
"Đơn đăng ký giảng viên mới từ Phạm Thị D (pham.d@example.com) cần được duyệt"
```

**Target Users:** All users with `user:READ` permission  
**Action URL:** `/admin/instructors/applications/application-uuid-012`

## Implementation Examples

### PaymentService Integration

```java
@Service
public class PaymentService {

    @Autowired
    private NotificationHelper notificationHelper;

    public void processPayment(PaymentDto paymentDto) {
        // ... payment processing logic

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            // Notify users with payment:READ permission about successful payment
            notificationHelper.createAdminStudentPaymentNotification(
                payment.getId(),
                payment.getUser().getName(),
                payment.getCourse().getTitle(),
                formatCurrency(payment.getAmount())
            );
        }
    }

    public void updatePaymentStatus(String paymentId, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findById(paymentId);
        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(newStatus);

        // Notify users with payment:READ permission about status change
        if (oldStatus == PaymentStatus.PENDING && newStatus == PaymentStatus.COMPLETED) {
            notificationHelper.createAdminPaymentStatusChangeNotification(
                payment.getId(),
                payment.getUser().getName(),
                payment.getCourse().getTitle(),
                oldStatus.toString(),
                newStatus.toString()
            );
        }
    }
}
```

### CourseService Integration

```java
@Service
public class CourseService {

    @Autowired
    private NotificationHelper notificationHelper;

    public void submitCourseForApproval(String courseId) {
        Course course = courseRepository.findById(courseId);
        course.setStatus(CourseStatus.PENDING_APPROVAL);

        // Notify users with course:APPROVE permission about course needing approval
        notificationHelper.createAdminCourseApprovalNeededNotification(
            course.getId(),
            course.getTitle(),
            course.getInstructor().getName()
        );
    }
}
```

### InstructorApplicationService Integration

```java
@Service
public class InstructorApplicationService {

    @Autowired
    private NotificationHelper notificationHelper;

    public void submitApplication(InstructorApplicationDto applicationDto) {
        // ... save application logic

        // Notify users with user:READ permission about new instructor application
        notificationHelper.createAdminInstructorApplicationNotification(
            application.getId(),
            application.getApplicant().getName(),
            application.getApplicant().getEmail()
        );
    }
}
```

## Technical Details

### Permission-Based User Discovery

The system automatically finds users with specific permissions using:

```java
private List<User> getUsersWithPermission(String permissionKey) {
    // Get all active users
    Pageable pageable = PageRequest.of(0, 1000);
    Page<User> usersPage = userRepository.findUsersWithFilters(
        null,        // no search filter
        null,        // no role filter (check all roles)
        true,        // active users only
        pageable
    );

    // Filter by permission using AuthorizationService
    return usersPage.getContent().stream()
        .filter(user -> authorizationService.hasPermission(user, permissionKey))
        .collect(Collectors.toList());
}
```

### Permission Mapping

| Notification Type       | Required Permission | Purpose                   |
| ----------------------- | ------------------- | ------------------------- |
| Student Payment Success | `payment:READ`      | View payment information  |
| Payment Status Change   | `payment:READ`      | View payment information  |
| Course Approval Needed  | `course:APPROVE`    | Approve courses           |
| Instructor Application  | `user:READ`         | View user/instructor data |

### Advantages of Permission-Based Approach

1. **Flexible Role Management**: Any role can be granted specific permissions
2. **Future-Proof**: New roles can receive notifications without code changes
3. **Fine-Grained Control**: Different permissions for different notification types
4. **RBAC Compliance**: Follows existing authorization architecture
5. **Multi-Role Support**: Users with multiple roles get appropriate notifications

### Notification Creation

- **Asynchronous Processing**: All notifications are created using `CompletableFuture` for non-blocking performance
- **Error Handling**: Individual user notification failures don't affect others
- **Logging**: Comprehensive logging for monitoring and debugging
- **Expiration**: All notifications expire after 30 days

### Priority Levels

- **MEDIUM**: Student payment success (informational)
- **HIGH**: Payment status changes, course approvals, instructor applications (action required)

## Error Handling

The system includes robust error handling:

1. **No Users with Permission Found**: Logs warning and continues
2. **Individual Notification Failures**: Logs error but continues with other users
3. **Database Errors**: Logs error and returns empty user list
4. **Permission Check Failures**: Skips user and continues with others
5. **Async Failures**: Each notification handles its own exceptions

## Monitoring

Use application logs to monitor notification delivery:

```
INFO  NotificationHelper - Creating notifications for 3 users with permission 'payment:READ': Sinh viên Nguyễn Văn A đã thanh toán thành công...
DEBUG NotificationHelper - Found 5 users with permission 'payment:READ' out of 12 total users
DEBUG NotificationHelper - Notification created for user user-001 (payment:READ): notification-uuid-123
ERROR NotificationHelper - Failed to create notification for user user-002 (payment:READ): Connection timeout
```

## Permission Management

### Adding New Notification Types

To add a new notification type:

1. **Choose Appropriate Permission**: Select existing permission or create new one
2. **Add Method**: Create new method in NotificationHelper following the pattern
3. **Use Permission**: Call `createNotificationsForUsersWithPermission(permissionKey, ...)`
4. **Test**: Verify users with permission receive notifications

### Example: New Refund Notification

```java
/**
 * Notify users with payment:WRITE permission about refund requests
 */
public void createAdminRefundRequestNotification(String refundId, String studentName,
                                               String courseName, String amount) {
    String message = String.format("Yêu cầu hoàn tiền %s từ sinh viên %s cho khóa học '%s'",
                                  amount, studentName, courseName);
    String actionUrl = "/admin/refunds/" + refundId;

    createNotificationsForUsersWithPermission(
        "payment:WRITE",  // Users who can process payments
        "res-refund-request",
        refundId,
        message,
        actionUrl,
        NotificationPriority.HIGH
    );

    log.info("Refund request notification created for refund: {} - sent to users with payment:WRITE permission", refundId);
}
```

## Best Practices

1. **Use Specific Permissions**: Choose the most appropriate permission for each notification type
2. **Monitor Permission Assignment**: Ensure users have required permissions to receive notifications
3. **Handle Permission Changes**: Users losing permissions will automatically stop receiving notifications
4. **Test Permission Scenarios**: Verify notifications reach the right users
5. **Log Permission Checks**: Monitor which users have/don't have required permissions
6. **Performance Considerations**: For large user bases, consider pagination or async permission checking
