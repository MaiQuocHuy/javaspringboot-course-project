# Notification Test Endpoints Guide

## Overview

Tài liệu này hướng dẫn cách sử dụng các test endpoints trong NotificationController để test tất cả các loại notification trong hệ thống.

## Base URL

```
http://localhost:8080/api/admin/notifications
```

## Test Endpoints

### 1. Student Notification Tests

#### 1.1 Payment Success Notification

- **Endpoint:** `POST /test/payment-success`
- **Description:** Test notification thanh toán thành công
- **Sample Data:**
  - User ID: `user-001`
  - Payment ID: `payment-001`
  - Course: `Advanced Spring Boot`
  - Priority: `HIGH`

```bash
curl -X POST http://localhost:8080/api/admin/notifications/test/payment-success
```

#### 1.2 Course Enrollment Notification

- **Endpoint:** `POST /test/enrollment`
- **Description:** Test notification đăng ký khóa học
- **Sample Data:**
  - User ID: `user-002`
  - Enrollment ID: `enrollment-001`
  - Course: `React Development`
  - Priority: `MEDIUM`

```bash
curl -X POST http://localhost:8080/api/admin/notifications/test/enrollment
```

#### 1.3 Certificate Notification

- **Endpoint:** `POST /test/certificate`
- **Description:** Test notification nhận chứng chỉ
- **Sample Data:**
  - User ID: `user-003`
  - Certificate ID: `certificate-001`
  - Course: `Java Programming`
  - Priority: `HIGH`

```bash
curl -X POST http://localhost:8080/api/admin/notifications/test/certificate
```

#### 1.4 Course Approval Notification (Approved)

- **Endpoint:** `POST /test/course-approval-approved`
- **Description:** Test notification khóa học được duyệt
- **Sample Data:**
  - Instructor ID: `instructor-001`
  - Course ID: `course-001`
  - Course: `Node.js Development`
  - Status: `Approved`

```bash
curl -X POST http://localhost:8080/api/admin/notifications/test/course-approval-approved
```

#### 1.5 Course Approval Notification (Rejected)

- **Endpoint:** `POST /test/course-approval-rejected`
- **Description:** Test notification khóa học bị từ chối
- **Sample Data:**
  - Instructor ID: `instructor-002`
  - Course ID: `course-002`
  - Course: `Python Basics`
  - Status: `Rejected`

```bash
curl -X POST http://localhost:8080/api/admin/notifications/test/course-approval-rejected
```

#### 1.6 Refund Notification

- **Endpoint:** `POST /test/refund`
- **Description:** Test notification hoàn tiền
- **Sample Data:**
  - User ID: `user-004`
  - Refund ID: `refund-001`
  - Course: `Database Management`
  - Status: `Approved`

```bash
curl -X POST http://localhost:8080/api/admin/notifications/test/refund
```

### 2. Admin Notification Tests

#### 2.1 Admin Student Payment Notification

- **Endpoint:** `POST /test/admin/student-payment`
- **Description:** Test notification admin về thanh toán của sinh viên
- **Permission Required:** `payment:READ`
- **Priority:** `MEDIUM`

```bash
curl -X POST http://localhost:8080/api/admin/notifications/test/admin/student-payment
```

#### 2.2 Admin Payment Status Change Notification

- **Endpoint:** `POST /test/admin/payment-status-change`
- **Description:** Test notification admin về thay đổi trạng thái thanh toán
- **Permission Required:** `payment:READ`
- **Priority:** `HIGH`

```bash
curl -X POST http://localhost:8080/api/admin/notifications/test/admin/payment-status-change
```

#### 2.3 Admin Course Approval Needed Notification

- **Endpoint:** `POST /test/admin/course-approval-needed`
- **Description:** Test notification admin về khóa học cần duyệt
- **Permission Required:** `course:APPROVE`
- **Priority:** `HIGH`

```bash
curl -X POST http://localhost:8080/api/admin/notifications/test/admin/course-approval-needed
```

#### 2.4 Admin Instructor Application Notification

- **Endpoint:** `POST /test/admin/instructor-application`
- **Description:** Test notification admin về đơn đăng ký instructor
- **Permission Required:** `instructor_application:READ`
- **Priority:** `HIGH`

```bash
curl -X POST http://localhost:8080/api/admin/notifications/test/admin/instructor-application
```

### 3. Test All Notifications

- **Endpoint:** `POST /test/all`
- **Description:** Test tất cả các loại notification cùng lúc
- **Sample Data:** Tạo 9 notification với data khác nhau

```bash
curl -X POST http://localhost:8080/api/admin/notifications/test/all
```

## Response Format

### Success Response (Student Notifications)

```json
{
  "statusCode": 201,
  "message": "Payment success notification created successfully",
  "data": {
    "id": "notification-uuid",
    "user_id": "user-001",
    "resource_id": "res-payment-001",
    "entity_id": "payment-001",
    "message": "Thanh toán thành công cho khóa học 'Advanced Spring Boot'",
    "action_url": "/courses/advanced-spring-boot",
    "priority": "HIGH",
    "is_read": false,
    "created_at": "2025-09-10T12:30:00Z",
    "expired_at": "2025-10-10T12:30:00Z"
  },
  "timeStamp": "2025-09-10T12:30:00Z"
}
```

### Success Response (Admin Notifications)

```json
{
  "statusCode": 200,
  "message": "Admin student payment notification sent successfully",
  "data": "Notification sent to all users with payment:READ permission",
  "timeStamp": "2025-09-10T12:30:00Z"
}
```

### Error Response

```json
{
  "statusCode": 500,
  "message": "Error: Some error message",
  "data": null,
  "timeStamp": "2025-09-10T12:30:00Z"
}
```

## Testing Strategy

### 1. Individual Testing

Test từng endpoint một để kiểm tra functionality cụ thể:

```bash
# Test payment notification
curl -X POST http://localhost:8080/api/admin/notifications/test/payment-success

# Test enrollment notification
curl -X POST http://localhost:8080/api/admin/notifications/test/enrollment

# Test admin notifications
curl -X POST http://localhost:8080/api/admin/notifications/test/admin/student-payment
```

### 2. Comprehensive Testing

Test tất cả notifications cùng lúc:

```bash
curl -X POST http://localhost:8080/api/admin/notifications/test/all
```

### 3. Permission Testing

- Admin notifications sẽ chỉ được gửi cho users có permission tương ứng
- Kiểm tra log để xác nhận số lượng users nhận được notification
- Verify permission system hoạt động đúng

### 4. Database Verification

Sau khi test, kiểm tra database:

```sql
-- Check created notifications
SELECT * FROM notification ORDER BY created_at DESC LIMIT 20;

-- Check notifications by resource type
SELECT resource_id, COUNT(*) FROM notification GROUP BY resource_id;

-- Check notifications by priority
SELECT priority, COUNT(*) FROM notification GROUP BY priority;
```

## Log Monitoring

Monitor application logs để tracking:

```bash
# Tail application logs
tail -f logs/application.log | grep -i notification

# Filter specific notification types
tail -f logs/application.log | grep -i "payment.*notification"
tail -f logs/application.log | grep -i "course.*notification"
```

## Expected Log Output

### Student Notifications

```
2025-09-10 12:30:00 INFO  - Testing payment success notification
2025-09-10 12:30:01 INFO  - Payment success notification created for user: user-001
```

### Admin Notifications

```
2025-09-10 12:30:00 INFO  - Testing admin student payment notification
2025-09-10 12:30:01 INFO  - Found 3 users with permission 'payment:READ' out of 15 total users
2025-09-10 12:30:01 INFO  - Creating notifications for 3 users with permission 'payment:READ'
2025-09-10 12:30:02 INFO  - Payment notification created for payment: payment-002 - sent to users with payment:READ permission
```

## Troubleshooting

### Common Issues

1. **No users with permission found**

   - Verify permission system is configured
   - Check user roles and permissions in database
   - Ensure AuthorizationService is working

2. **Notification creation fails**

   - Check user_id exists in database
   - Verify NotificationService is configured
   - Check database connectivity

3. **Permission check fails**
   - Verify AuthorizationService integration
   - Check permission keys match database
   - Ensure proper Spring Security configuration

### Debug Commands

```bash
# Check if users exist
curl -X GET http://localhost:8080/api/admin/users

# Check notification service health
curl -X GET http://localhost:8080/api/admin/notifications/test

# Test basic notification creation
curl -X POST http://localhost:8080/api/admin/notifications/test
```
