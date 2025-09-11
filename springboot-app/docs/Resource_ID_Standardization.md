# Database Resource ID Mapping - CORRECTED

## Overview

This document outlines the **ACTUAL** `resource_id` values used in the NotificationHelper system, matching the database schema exactly.

## Database Resource Schema

### Core Resources (From Database)

```sql
-- Base resources from database schema
INSERT INTO resources VALUES ('res-course-001', 'course', 'Course management and access', '/courses', true);
INSERT INTO resources VALUES ('res-payment-001', 'payment', 'Payment processing and management', '/payments', true);
INSERT INTO resources VALUES ('res-user-001', 'user', 'User account management', '/users', true);
INSERT INTO resources VALUES ('res-enrollment-001', 'enrollment', 'Course enrollment management', '/enrollments', true);
INSERT INTO resources VALUES ('res-lesson-001', 'lesson', 'Lesson content and management', '/lessons', true);
INSERT INTO resources VALUES ('res-refund-001', 'refund', 'Payment refund management', '/refunds', true);
INSERT INTO resources VALUES ('res-instructor-application-001', 'instructor_application', 'Instructor application management', '/instructor-applications', true);
```

## NotificationHelper Resource ID Mapping

| Method                                         | Resource ID                      | Database Resource      | Purpose                                     |
| ---------------------------------------------- | -------------------------------- | ---------------------- | ------------------------------------------- |
| `createPaymentSuccessNotification`             | `res-payment-001`                | payment                | Student payment success                     |
| `createEnrollmentNotification`                 | `res-enrollment-001`             | enrollment             | Course enrollment confirmation              |
| `createCertificateNotification`                | `res-lesson-001`                 | lesson                 | Certificate completion (related to lessons) |
| `createCourseApprovalNotification`             | `res-course-001`                 | course                 | Course approval status                      |
| `createRefundNotification`                     | `res-refund-001`                 | refund                 | Refund status updates                       |
| `createAdminStudentPaymentNotification`        | `res-payment-001`                | payment                | Admin payment monitoring                    |
| `createAdminPaymentStatusChangeNotification`   | `res-payment-001`                | payment                | Admin payment status tracking               |
| `createAdminCourseApprovalNeededNotification`  | `res-course-001`                 | course                 | Admin course approval workflow              |
| `createAdminInstructorApplicationNotification` | `res-instructor-application-001` | instructor_application | Admin instructor application review         |

## Resource Hierarchy (From Database)

### Parent-Child Relationships

```
res-course-001 (course)
├── res-section-001 (section)
│   └── res-lesson-001 (lesson)
│       ├── res-comment-001 (comment)
│       ├── res-video-content-001 (video_content)
│       ├── res-lesson-completion-001 (lesson_completion)
│       ├── res-quiz-question-001 (quiz_question)
│       │   └── res-quiz-result-001 (quiz_result)

res-payment-001 (payment)
└── res-refund-001 (refund)

res-user-001 (user)
├── res-user-role-001 (user_role)
├── res-refresh-token-001 (refresh_token)
└── res-password-reset-token-001 (password_reset_token)
```

## Permission-Resource Mapping

| Permission Key   | Used With Resource               | Description                  |
| ---------------- | -------------------------------- | ---------------------------- |
| `payment:READ`   | `res-payment-001`                | View payment information     |
| `course:APPROVE` | `res-course-001`                 | Approve course submissions   |
| `user:READ`      | `res-instructor-application-001` | View instructor applications |

## Usage Examples

### Student Payment Notification

```java
notificationHelper.createPaymentSuccessNotification(
    userId, paymentId, courseName, courseUrl
);
// Uses: res-student-payment
```

### Admin Payment Monitoring

```java
notificationHelper.createAdminStudentPaymentNotification(
    paymentId, studentName, courseName, paymentAmount
);
// Uses: res-admin-payment + payment:READ permission
```

### Course Approval Workflow

```java
notificationHelper.createAdminCourseApprovalNeededNotification(
    courseId, courseName, instructorName
);
// Uses: res-admin-course-approval + course:APPROVE permission
```

## Benefits of Standardization

1. **Consistency**: Clear naming pattern across all notification types
2. **Clarity**: Easy to identify target audience and purpose
3. **Maintainability**: Simple to add new notification types following the pattern
4. **Filtering**: Easy to filter notifications by resource type in frontend
5. **Analytics**: Clear categorization for notification analytics

## Input Validation

All admin notification methods now include comprehensive input validation:

```java
// Null/empty validation for required fields
if (paymentId == null || paymentId.trim().isEmpty()) {
    log.error("Cannot create notification: paymentId is null or empty");
    return;
}

// Default values for optional display fields
if (studentName == null || studentName.trim().isEmpty()) {
    studentName = "Unknown Student";
}
```

### Validation Rules

| Field Type              | Validation Rule     | Default Value        |
| ----------------------- | ------------------- | -------------------- |
| Entity IDs (required)   | Not null, not empty | Error + early return |
| Names (display)         | Not null, not empty | "Unknown {Type}"     |
| Emails (display)        | Not null, not empty | "unknown@email.com"  |
| Status values (display) | Not null, not empty | "Unknown"            |

## Migration Notes

### Old vs New Resource IDs

| Old Resource ID      | New Resource ID                  | Migration Impact                 |
| -------------------- | -------------------------------- | -------------------------------- |
| `res-payment-001`    | `res-student-payment`            | Frontend filters may need update |
| `res-enrollment-001` | `res-student-enrollment`         | Frontend filters may need update |
| `res-lesson-001`     | `res-student-certificate`        | Frontend filters may need update |
| `res-course-001`     | `res-instructor-course-approval` | Frontend filters may need update |
| `res-payment-admin`  | `res-admin-payment`              | New admin notifications          |
| `res-payment-status` | `res-admin-payment-status`       | New admin notifications          |

### Database Impact

- No database migration required
- New notifications will use new resource IDs
- Existing notifications retain old resource IDs
- Frontend should handle both patterns during transition period

## Future Considerations

### New Notification Types

When adding new notification types, follow the pattern:

```
res-{audience}-{type}
```

### Examples of Future Resource IDs

- `res-admin-user-registration` - New user registrations
- `res-admin-system-error` - System error notifications
- `res-instructor-student-question` - Student questions to instructors
- `res-student-course-update` - Course content updates
