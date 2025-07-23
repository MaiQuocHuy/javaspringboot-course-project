---
applyTo: "**"
---

-- Users table
CREATE TABLE `USER` (
`id` varchar(36) PRIMARY KEY,
`name` varchar(255) NOT NULL,
`email` varchar(255) UNIQUE NOT NULL,
`password` varchar(255) NOT NULL COMMENT 'Hashed password',
`is_active` boolean DEFAULT true,
`thumbnail_url` varchar(255) DEFAULT NULL COMMENT 'URL to user profile picture',
`thumbnail_id` varchar(255) DEFAULT NULL COMMENT 'ID of the thumbnail image',
`bio` text DEFAULT NULL COMMENT 'Short biography or description',
`created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- User roles table
CREATE TABLE `USER_ROLE` (
`id` int PRIMARY KEY,
`user_id` varchar(36) NOT NULL,
`role` enum('STUDENT', 'INSTRUCTOR', 'ADMIN') NOT NULL,
UNIQUE KEY `unique_user_role` (`user_id`, `role`),
FOREIGN KEY (`user_id`) REFERENCES `USER`(`id`) ON DELETE CASCADE
);

-- Refresh tokens for JWT authentication
CREATE TABLE `REFRESH_TOKEN` (
`id` int PRIMARY KEY,
`user_id` varchar(36) NOT NULL,
`token` varchar(512) NOT NULL,
`expires_at` timestamp NOT NULL,
`is_revoked` boolean DEFAULT false,
`created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
INDEX `idx_user_id` (`user_id`),
UNIQUE INDEX `idx_token` (`token`),
FOREIGN KEY (`user_id`) REFERENCES `USER`(`id`) ON DELETE CASCADE
);

-- Course Management
-- Categories table
CREATE TABLE `CATEGORY` (
`id` varchar(36) PRIMARY KEY,
`name` varchar(255) UNIQUE NOT NULL,
`created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Courses table
CREATE TABLE `COURSE` (
`id` varchar(36) PRIMARY KEY,
`title` varchar(255) NOT NULL,
`description` text,
`instructor_id` varchar(36),
`price` decimal(10,2) DEFAULT 0.00,
`is_published` boolean DEFAULT false,
`is_approved` boolean DEFAULT false,
`is_deleted` boolean DEFAULT false,
`created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
INDEX `idx_instructor` (`instructor_id`),
INDEX `idx_title` (`title`),
FOREIGN KEY (`instructor_id`) REFERENCES `USER`(`id`) ON DELETE SET NULL
);

-- Course categories junction table
CREATE TABLE `COURSE_CATEGORY` (
`course_id` varchar(36) NOT NULL,
`category_id` varchar(36) NOT NULL,
PRIMARY KEY (`course_id`, `category_id`),
FOREIGN KEY (`course_id`) REFERENCES `COURSE`(`id`) ON DELETE CASCADE,
FOREIGN KEY (`category_id`) REFERENCES `CATEGORY`(`id`) ON DELETE CASCADE
);

-- Course Content
-- Sections table
CREATE TABLE `SECTION` (
`id` varchar(36) PRIMARY KEY,
`course_id` varchar(36) NOT NULL,
`title` varchar(255) NOT NULL,
`order_index` int DEFAULT 0,
`created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
INDEX `idx_course` (`course_id`),
UNIQUE KEY `unique_section_order` (`course_id`, `order_index`),
FOREIGN KEY (`course_id`) REFERENCES `COURSE`(`id`) ON DELETE CASCADE
);

-- Video content table
CREATE TABLE `VIDEO_CONTENT` (
`id` varchar(36) PRIMARY KEY,
`url` text NOT NULL COMMENT 'Video file URL or streaming link',
`duration` int COMMENT 'Duration in seconds',
`uploaded_by` varchar(36) NOT NULL,
`created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (`uploaded_by`) REFERENCES `USER`(`id`) ON DELETE CASCADE
);

-- Lessons table
CREATE TABLE `LESSON` (
`id` varchar(36) PRIMARY KEY,
`section_id` varchar(36) NOT NULL,
`title` varchar(255) NOT NULL,
`type` enum('VIDEO', 'QUIZ') DEFAULT 'VIDEO',
`content_id` varchar(36) COMMENT 'Refers to VIDEO/PDF/... content',
`order_index` int DEFAULT 0,
`created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
INDEX `idx_section` (`section_id`),
UNIQUE KEY `unique_lesson_order` (`section_id`, `order_index`),
FOREIGN KEY (`section_id`) REFERENCES `SECTION`(`id`) ON DELETE CASCADE,
FOREIGN KEY (`content_id`) REFERENCES `VIDEO_CONTENT`(`id`) ON DELETE SET NULL
);

-- Enrollment and Progress
-- Enrollments table
CREATE TABLE `ENROLLMENT` (
`id` varchar(36) PRIMARY KEY,
`user_id` varchar(36) NOT NULL,
`course_id` varchar(36) NOT NULL,
`enrolled_at` timestamp DEFAULT CURRENT_TIMESTAMP,
`completion_status` enum('IN_PROGRESS', 'COMPLETED') DEFAULT 'IN_PROGRESS' COMMENT 'Derived or updated based on lesson completions',
UNIQUE KEY `unique_enrollment` (`user_id`, `course_id`),
FOREIGN KEY (`user_id`) REFERENCES `USER`(`id`) ON DELETE CASCADE,
FOREIGN KEY (`course_id`) REFERENCES `COURSE`(`id`) ON DELETE CASCADE
);

-- Lesson completions table
CREATE TABLE `LESSON_COMPLETION` (
`id` varchar(36) PRIMARY KEY,
`user_id` varchar(36) NOT NULL,
`lesson_id` varchar(36) NOT NULL,
`completed_at` timestamp DEFAULT CURRENT_TIMESTAMP,
UNIQUE KEY `unique_completion` (`user_id`, `lesson_id`),
FOREIGN KEY (`user_id`) REFERENCES `USER`(`id`) ON DELETE CASCADE,
FOREIGN KEY (`lesson_id`) REFERENCES `LESSON`(`id`) ON DELETE CASCADE
);

-- Reviews and Ratings
-- Reviews table
CREATE TABLE `REVIEW` (
`id` varchar(36) PRIMARY KEY,
`user_id` varchar(36) NOT NULL,
`course_id` varchar(36) NOT NULL,
`rating` int DEFAULT 5 COMMENT '1 to 5',
`review_text` text,
`reviewed_at` timestamp DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
UNIQUE KEY `unique_review` (`user_id`, `course_id`),
INDEX `idx_course` (`course_id`),
FOREIGN KEY (`user_id`) REFERENCES `USER`(`id`) ON DELETE CASCADE,
FOREIGN KEY (`course_id`) REFERENCES `COURSE`(`id`) ON DELETE CASCADE,
CHECK (rating >= 1 AND rating <= 5)
);

-- Payment System
-- Payments table
CREATE TABLE `PAYMENT` (
`id` varchar(36) PRIMARY KEY,
`user_id` varchar(36) NOT NULL,
`course_id` varchar(36) NOT NULL,
`amount` decimal(10,2) NOT NULL,
`status` enum('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
`payment_method` varchar(50),
`paid_at` timestamp NULL,
`created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
INDEX `idx_user` (`user_id`),
INDEX `idx_course` (`course_id`),
INDEX `idx_status` (`status`),
FOREIGN KEY (`user_id`) REFERENCES `USER`(`id`) ON DELETE CASCADE,
FOREIGN KEY (`course_id`) REFERENCES `COURSE`(`id`) ON DELETE CASCADE
);

-- Refunds table
CREATE TABLE `REFUND` (
`id` varchar(36) PRIMARY KEY,
`payment_id` varchar(36) NOT NULL,
`amount` decimal(10,2) NOT NULL,
`status` enum('PENDING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
`reason` text,
`requested_at` timestamp DEFAULT CURRENT_TIMESTAMP,
`processed_at` timestamp NULL,
UNIQUE KEY `unique_payment` (`payment_id`),
INDEX `idx_status` (`status`),
FOREIGN KEY (`payment_id`) REFERENCES `PAYMENT`(`id`) ON DELETE CASCADE
);

-- Instructor earnings table
CREATE TABLE `INSTRUCTOR_EARNING` (
`id` varchar(36) PRIMARY KEY,
`instructor_id` varchar(36) NOT NULL,
`payment_id` varchar(36) NOT NULL,
`course_id` varchar(36) NOT NULL,
`amount` decimal(10,2) NOT NULL COMMENT 'Instructor''s share after platform cut',
`status` enum('PENDING', 'AVAILABLE', 'PAID') DEFAULT 'PENDING',
`available_at` timestamp NULL,
`paid_at` timestamp NULL,
`created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
INDEX `idx_instructor` (`instructor_id`),
UNIQUE KEY `unique_payment` (`payment_id`),
INDEX `idx_status` (`status`),
FOREIGN KEY (`instructor_id`) REFERENCES `USER`(`id`) ON DELETE CASCADE,
FOREIGN KEY (`payment_id`) REFERENCES `PAYMENT`(`id`) ON DELETE CASCADE,
FOREIGN KEY (`course_id`) REFERENCES `COURSE`(`id`) ON DELETE CASCADE
);

-- Instructor Management
-- Instructor applications table
CREATE TABLE `INSTRUCTOR_APPLICATION` (
`id` varchar(36) PRIMARY KEY,
`user_id` varchar(36) NOT NULL,
`reviewed_by` varchar(36),
`status` enum('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
`documents` json COMMENT 'Array of document URLs or details',
`submitted_at` timestamp DEFAULT CURRENT_TIMESTAMP,
`reviewed_at` timestamp NULL,
`rejection_reason` text,
UNIQUE KEY `unique_application` (`user_id`),
INDEX `idx_status` (`status`),
FOREIGN KEY (`user_id`) REFERENCES `USER`(`id`) ON DELETE CASCADE,
FOREIGN KEY (`reviewed_by`) REFERENCES `USER`(`id`) ON DELETE SET NULL
);

-- Quiz System
-- Quiz questions table
CREATE TABLE `QUIZ_QUESTION` (
`id` varchar(36) PRIMARY KEY,
`lesson_id` varchar(36) NOT NULL COMMENT 'Belongs to a lesson',
`question_text` text NOT NULL,
`options` json NOT NULL COMMENT 'Array of options: e.g. ["A", "B", "C", "D"]',
`correct_answer` varchar(255) NOT NULL COMMENT 'Could be the correct option key/value',
`explanation` text COMMENT 'Optional explanation for the answer',
`created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
INDEX `idx_lesson` (`lesson_id`),
FOREIGN KEY (`lesson_id`) REFERENCES `LESSON`(`id`) ON DELETE CASCADE
);

-- Quiz results table
CREATE TABLE `QUIZ_RESULT` (
`id` varchar(36) PRIMARY KEY,
`user_id` varchar(36) NOT NULL,
`lesson_id` varchar(36) NOT NULL COMMENT 'Should be a QUIZ lesson',
`score` decimal(5,2) COMMENT 'Percentage score',
`answers` json COMMENT 'User answers: e.g. {"question_id_1": "B", "question_id_2": "D"}',
`completed_at` timestamp DEFAULT CURRENT_TIMESTAMP,
UNIQUE KEY `unique_result` (`user_id`, `lesson_id`),
FOREIGN KEY (`user_id`) REFERENCES `USER`(`id`) ON DELETE CASCADE,
FOREIGN KEY (`lesson_id`) REFERENCES `LESSON`(`id`) ON DELETE CASCADE
);
