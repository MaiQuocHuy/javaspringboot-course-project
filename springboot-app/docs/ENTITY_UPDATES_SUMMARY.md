# Entity Updates Summary

## Overview

All Java Spring Boot entity classes have been updated to match the provided MySQL schema. The changes include:

## Major Changes Made

### 1. Table Name Updates

- All table names changed from UPPER_CASE to lowercase_with_underscores format:
  - `USER` → `users`
  - `USER_ROLE` → `user_roles`
  - `CATEGORY` → `categories`
  - `COURSE` → `courses`
  - `SECTION` → `sections`
  - `LESSON` → `lessons`
  - `ENROLLMENT` → `enrollments`
  - `LESSON_COMPLETION` → `lesson_completions`
  - `REVIEW` → `reviews`
  - `PAYMENT` → `payments`
  - `REFUND` → `refunds`
  - `INSTRUCTOR_EARNING` → `instructor_earnings`
  - `INSTRUCTOR_APPLICATION` → `instructor_applications`
  - `REFRESH_TOKEN` → `refresh_tokens`
  - `QUIZ_QUESTION` → `quiz_questions`
  - `QUIZ_RESULT` → `quiz_results`
  - `VIDEO_CONTENT` → `video_contents`

### 2. Column Type Updates

- Changed `text` to `TEXT` for better MySQL compatibility
- Changed `json` to `JSON` for MySQL JSON columns
- Added proper precision and scale for decimal columns

### 3. Enum Implementation

All string-based status fields converted to proper enums:

- **Enrollment**: `CompletionStatus` (IN_PROGRESS, COMPLETED)
- **Payment**: `PaymentStatus` (PENDING, COMPLETED, FAILED, REFUNDED)
- **Refund**: `RefundStatus` (PENDING, COMPLETED, FAILED)
- **InstructorEarning**: `EarningStatus` (PENDING, AVAILABLE, PAID)
- **InstructorApplication**: `ApplicationStatus` (PENDING, APPROVED, REJECTED)
- **SystemLog**: `Action` (CREATE, UPDATE, DELETE, READ, LOGIN, LOGOUT, EXPORT, IMPORT)

### 4. Relationship Updates

- **Lesson**: Now properly references `LessonType` entity and `VideoContent` entity
- **User**: Maintained Many-to-One relationship with `UserRole`
- **RefreshToken**: Changed ID from String UUID to Integer with auto-increment

### 5. Index Updates

Updated all index names to match the schema:

- `idx_pay_user`, `idx_pay_course`, `idx_pay_status`, `idx_pay_session_id`
- `idx_earn_instructor`, `idx_earn_status`
- `idx_app_status`, `idx_app_user`
- `idx_rt_user`
- `idx_qq_lesson`
- And many more...

### 6. Constraint Updates

- Added proper unique constraints with schema-matching names
- Added foreign key constraint names
- Added CHECK constraint for Review rating (1-5)

### 7. New Entities Created

- **LessonType**: New entity for lesson types
- **SystemLog**: New entity for system logging

### 8. Column Additions

- **Category**: Added `description` field
- **Section**: Added `description` field
- **Payment**: Added `session_id` field for Stripe integration
- **User**: Table name updated to match schema

### 9. Timestamp Handling

- Standardized timestamp handling using `@CreationTimestamp` and `@UpdateTimestamp`
- Removed manual timestamp management in favor of JPA annotations

### 10. Default Values

- Added proper default values matching the schema
- Used enum defaults instead of string defaults

## Benefits of These Updates

1. **Schema Consistency**: All entities now perfectly match the MySQL schema
2. **Type Safety**: Enums provide compile-time safety instead of string constants
3. **Performance**: Proper indexing matches the database schema
4. **Maintainability**: Standardized naming conventions and structure
5. **Database Constraints**: All constraints are properly reflected in the entities
6. **JSON Support**: Proper MySQL JSON column support

## Notes

- All entities extend `BaseEntity` except `RefreshToken` and `SystemLog` which have specific ID requirements
- Foreign key relationships are properly maintained
- Cascade types and orphan removal are appropriately configured
- Lazy loading is used for performance optimization

The entities are now fully compliant with the provided MySQL schema and should work seamlessly with the database structure.
