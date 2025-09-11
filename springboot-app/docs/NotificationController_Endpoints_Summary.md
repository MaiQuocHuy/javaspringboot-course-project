# NotificationController API Endpoints Summary

## Updated Endpoints List

| Method | Endpoint                            | Description                    | Authentication | Permission Required |
| ------ | ----------------------------------- | ------------------------------ | -------------- | ------------------- |
| POST   | `/api/admin/notifications`          | Create notification (Admin)    | Required       | ADMIN role          |
| POST   | `/api/admin/notifications/internal` | Create notification (Internal) | None           | System internal     |
| POST   | `/api/admin/notifications/test`     | Basic test notification        | None           | None                |
| GET    | `/api/admin/notifications/test`     | Simple health check            | None           | None                |

## New Test Endpoints for NotificationHelper

### Student Notification Tests

| Method | Endpoint                         | Description                       | Target Users        | Priority |
| ------ | -------------------------------- | --------------------------------- | ------------------- | -------- |
| POST   | `/test/payment-success`          | Test payment success notification | Specific user       | HIGH     |
| POST   | `/test/enrollment`               | Test enrollment notification      | Specific user       | MEDIUM   |
| POST   | `/test/certificate`              | Test certificate notification     | Specific user       | HIGH     |
| POST   | `/test/course-approval-approved` | Test course approval (approved)   | Specific instructor | HIGH     |
| POST   | `/test/course-approval-rejected` | Test course approval (rejected)   | Specific instructor | HIGH     |
| POST   | `/test/refund`                   | Test refund notification          | Specific user       | MEDIUM   |

### Admin Notification Tests

| Method | Endpoint                             | Description                     | Target Users          | Permission Required           |
| ------ | ------------------------------------ | ------------------------------- | --------------------- | ----------------------------- |
| POST   | `/test/admin/student-payment`        | Test admin payment notification | Users with permission | `payment:READ`                |
| POST   | `/test/admin/payment-status-change`  | Test payment status change      | Users with permission | `payment:READ`                |
| POST   | `/test/admin/course-approval-needed` | Test course approval needed     | Users with permission | `course:APPROVE`              |
| POST   | `/test/admin/instructor-application` | Test instructor application     | Users with permission | `instructor_application:READ` |

### Bulk Testing

| Method | Endpoint    | Description                 | Target Users | Notes                             |
| ------ | ----------- | --------------------------- | ------------ | --------------------------------- |
| POST   | `/test/all` | Test all notification types | Mixed        | Creates 9 different notifications |

## Test Data Used

### Student Notifications

- **Payment Success**: User `user-001`, Course "Advanced Spring Boot"
- **Enrollment**: User `user-002`, Course "React Development"
- **Certificate**: User `user-003`, Course "Java Programming"
- **Course Approval (Approved)**: Instructor `instructor-001`, Course "Node.js Development"
- **Course Approval (Rejected)**: Instructor `instructor-002`, Course "Python Basics"
- **Refund**: User `user-004`, Course "Database Management", Status "approved"

### Admin Notifications

- **Student Payment**: Payment by "Nguyễn Văn A" for "Web Development" - 1,500,000 VND
- **Payment Status Change**: "Trần Thị B" payment for "Mobile App Development" (PENDING → COMPLETED)
- **Course Approval Needed**: "AI and Machine Learning" by "Dr. Lê Văn C"
- **Instructor Application**: "Phạm Thị D" application (phamthid@example.com)

## Resource ID Mapping

| Notification Type                    | Resource ID                      | Database Resource      |
| ------------------------------------ | -------------------------------- | ---------------------- |
| Payment notifications                | `res-payment-001`                | payment                |
| Enrollment notifications             | `res-enrollment-001`             | enrollment             |
| Certificate notifications            | `res-lesson-001`                 | lesson                 |
| Course approval notifications        | `res-course-001`                 | course                 |
| Refund notifications                 | `res-refund-001`                 | refund                 |
| Instructor application notifications | `res-instructor-application-001` | instructor_application |

## Testing Flow

### 1. Quick Test (Single Notification)

```bash
# Test basic functionality
curl -X POST http://localhost:8080/api/admin/notifications/test/payment-success
```

### 2. Permission-Based Test (Admin Notifications)

```bash
# Test admin notifications (requires users with permissions)
curl -X POST http://localhost:8080/api/admin/notifications/test/admin/student-payment
```

### 3. Comprehensive Test (All Notifications)

```bash
# Test everything at once
curl -X POST http://localhost:8080/api/admin/notifications/test/all
```

## Expected Outcomes

### Student Notifications

- Creates individual notifications for specific users
- Returns NotificationResponseDto with created notification details
- Asynchronous processing with CompletableFuture

### Admin Notifications

- Creates notifications for all users with required permissions
- Returns success message indicating how many users were notified
- Synchronous processing for immediate feedback

### Error Handling

- Validates all input parameters with null checks
- Provides detailed error messages in logs
- Returns appropriate HTTP status codes (201, 500)

## Monitoring and Verification

### Application Logs

```
INFO - Testing payment success notification
INFO - Payment success notification created for user: user-001
INFO - Found 3 users with permission 'payment:READ' out of 15 total users
INFO - Creating notifications for 3 users with permission 'payment:READ'
```

### Database Verification

```sql
-- Check created notifications
SELECT COUNT(*) FROM notification WHERE created_at > NOW() - INTERVAL 1 HOUR;

-- Check by resource type
SELECT resource_id, COUNT(*) FROM notification GROUP BY resource_id;
```

### Permission System Check

- Verify AuthorizationService.hasPermission() works correctly
- Confirm users have appropriate permissions in database
- Check permission-based filtering is working

## Integration Points

### Dependencies

- **NotificationService**: Core notification creation
- **NotificationHelper**: Business logic wrapper
- **AuthorizationService**: Permission checking
- **UserRepository**: User data access

### External Systems

- Email service (for notification delivery)
- WebSocket (for real-time notifications)
- Database (for persistence)
- Permission system (for access control)
