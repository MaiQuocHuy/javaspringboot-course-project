---
trigger: manual
---

CREATE TABLE IF NOT EXISTS `users` (
`id` VARCHAR(36) PRIMARY KEY,
`name` VARCHAR(255) NOT NULL,
`email` VARCHAR(255) NOT NULL UNIQUE,
`password` VARCHAR(255) NOT NULL COMMENT 'Hashed password',
`is_active` BOOLEAN DEFAULT TRUE,
`thumbnail_url` VARCHAR(255) DEFAULT NULL,
`thumbnail_id` VARCHAR(255) DEFAULT NULL,
`bio` TEXT DEFAULT NULL,
`role_id` VARCHAR(36) NOT NULL,
`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
`updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
constraint `fk_user_roles`
FOREIGN KEY (`role_id`) REFERENCES `user_roles`(`id`) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE `user_roles` (
`id` VARCHAR(36) PRIMARY KEY,
`role` ENUM('STUDENT','INSTRUCTOR','ADMIN') NOT NULL,
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `categories` (
`id` VARCHAR(36) PRIMARY KEY,
`name` VARCHAR(255) NOT NULL UNIQUE,
`description` TEXT,
`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
`updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `courses` (
`id` VARCHAR(36) PRIMARY KEY,
`title` VARCHAR(255) NOT NULL,
`description` TEXT,
`instructor_id` VARCHAR(36),
`price` DECIMAL(10,2) DEFAULT 0.00,
`is_published` BOOLEAN DEFAULT FALSE,
`is_approved` BOOLEAN DEFAULT FALSE,
`thumbnail_url` VARCHAR(255),
`thumbnail_id` VARCHAR(255),
`level` ENUM('BEGINNER','INTERMEDIATE','ADVANCED') DEFAULT 'BEGINNER',
`is_deleted` BOOLEAN DEFAULT FALSE,
`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
`updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
INDEX `idx_course_instructor` (`instructor_id`),
CONSTRAINT `fk_course_instructor`
FOREIGN KEY (`instructor_id`) REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `course_categories` (
`course_id` VARCHAR(36) NOT NULL,
`category_id` VARCHAR(36) NOT NULL,
PRIMARY KEY (`course_id`, `category_id`),
CONSTRAINT `fk_cc_course`
FOREIGN KEY (`course_id`) REFERENCES `courses`(`id`) ON DELETE CASCADE,
CONSTRAINT `fk_cc_category`
FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `sections` (
`id` VARCHAR(36) PRIMARY KEY,
`course_id` VARCHAR(36) NOT NULL,
`title` VARCHAR(255) NOT NULL,
`order_index` INT DEFAULT 0,
`description` TEXT,
`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
`updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
UNIQUE KEY `unique_section_order` (`course_id`, `order_index`),
CONSTRAINT `fk_section_course`
FOREIGN KEY (`course_id`) REFERENCES `courses`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `video_contents` (
`id` VARCHAR(36) PRIMARY KEY,
`url` TEXT NOT NULL,
`duration` INT,
`uploaded_by` VARCHAR(36) NOT NULL,
`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
`updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
CONSTRAINT `fk_video_uploader`
FOREIGN KEY (`uploaded_by`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `lessons` (
`id` VARCHAR(36) PRIMARY KEY,
`section_id` VARCHAR(36) NOT NULL,
`title` VARCHAR(255) NOT NULL,
`lesson_type_id` VARCHAR(36) NOT NULL,
`content_id` VARCHAR(36),
`order_index` INT DEFAULT 0,
`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
`updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
UNIQUE KEY `unique_lesson_order` (`section_id`, `order_index`),
CONSTRAINT `fk_lesson_section`
FOREIGN KEY (`section_id`) REFERENCES `sections`(`id`) ON DELETE CASCADE,
CONSTRAINT `fk_lesson_type`
FOREIGN KEY (`lesson_type_id`) REFERENCES `lesson_types`(`id`) ON DELETE RESTRICT,
CONSTRAINT `fk_lesson_content`
FOREIGN KEY (`content_id`) REFERENCES `video_contents`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `lesson_types` (
`id` VARCHAR(36) PRIMARY KEY,
`name` VARCHAR(50) NOT NULL UNIQUE,
`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
`updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `enrollments` (
`id` VARCHAR(36) PRIMARY KEY,
`user_id` VARCHAR(36) NOT NULL,
`course_id` VARCHAR(36) NOT NULL,
`enrolled_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
`completion_status` ENUM('IN_PROGRESS','COMPLETED') DEFAULT 'IN_PROGRESS',
`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
`updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
UNIQUE KEY `unique_enrollment` (`user_id`, `course_id`),
CONSTRAINT `fk_enr_user`
FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
CONSTRAINT `fk_enr_course`
FOREIGN KEY (`course_id`) REFERENCES `courses`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `lesson_completions` (
`id` VARCHAR(36) PRIMARY KEY,
`user_id` VARCHAR(36) NOT NULL,
`lesson_id` VARCHAR(36) NOT NULL,
`completed_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
UNIQUE KEY `unique_completion` (`user_id`, `lesson_id`),
CONSTRAINT `fk_lc_user`
FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
CONSTRAINT `fk_lc_lesson`
FOREIGN KEY (`lesson_id`) REFERENCES `lessons`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `reviews` (
`id` VARCHAR(36) PRIMARY KEY,
`user_id` VARCHAR(36) NOT NULL,
`course_id` VARCHAR(36) NOT NULL,
`rating` INT DEFAULT 5,
`review_text` TEXT,
`reviewed_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
`updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
UNIQUE KEY `unique_review` (`user_id`, `course_id`),
INDEX `idx_review_course` (`course_id`),
CONSTRAINT `fk_rev_user`
FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
CONSTRAINT `fk_rev_course`
FOREIGN KEY (`course_id`) REFERENCES `courses`(`id`) ON DELETE CASCADE,
CHECK (`rating` BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `payments` (
`id` VARCHAR(36) PRIMARY KEY,
`user_id` VARCHAR(36) NOT NULL,
`course_id` VARCHAR(36) NOT NULL,
`amount` DECIMAL(10,2) NOT NULL,
`status` ENUM('PENDING','COMPLETED','FAILED','REFUNDED') DEFAULT 'PENDING',
`payment_method` VARCHAR(50),
`session_id` TEXT, -- lưu Stripe Checkout Session ID
`paid_at` DATETIME,
`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
INDEX `idx_pay_user` (`user_id`),
INDEX `idx_pay_course` (`course_id`),
INDEX `idx_pay_status` (`status`),
INDEX `idx_pay_session_id` (`session_id`(255)), -- prefix index cho TEXT
CONSTRAINT `fk_pay_user`
FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
CONSTRAINT `fk_pay_course`
FOREIGN KEY (`course_id`) REFERENCES `courses`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `refunds` (
`id` VARCHAR(36) PRIMARY KEY,
`payment_id` VARCHAR(36) NOT NULL,
`amount` DECIMAL(10,2) NOT NULL,
`status` ENUM('PENDING','COMPLETED','FAILED') DEFAULT 'PENDING',
`reason` TEXT,
`requested_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
`processed_at` DATETIME,
UNIQUE KEY `unique_refund_payment` (`payment_id`),
INDEX `idx_refund_status` (`status`),
CONSTRAINT `fk_refund_payment`
FOREIGN KEY (`payment_id`) REFERENCES `payments`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `instructor_earnings` (
`id` VARCHAR(36) PRIMARY KEY,
`instructor_id` VARCHAR(36) NOT NULL,
`payment_id` VARCHAR(36) NOT NULL,
`course_id` VARCHAR(36) NOT NULL,
`amount` DECIMAL(10,2) NOT NULL,
`status` ENUM('PENDING','AVAILABLE','PAID') DEFAULT 'PENDING',
`paid_at` DATETIME,
`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
`updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
INDEX `idx_earn_instructor` (`instructor_id`),
UNIQUE KEY `unique_earn_payment` (`payment_id`),
INDEX `idx_earn_status` (`status`),
CONSTRAINT `fk_earn_instructor`
FOREIGN KEY (`instructor_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
CONSTRAINT `fk_earn_payment`
FOREIGN KEY (`payment_id`) REFERENCES `payments`(`id`) ON DELETE CASCADE,
CONSTRAINT `fk_earn_course`
FOREIGN KEY (`course_id`) REFERENCES `courses`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `instructor_applications` (
`id` VARCHAR(36) PRIMARY KEY,
`user_id` VARCHAR(36) NOT NULL,
`reviewed_by` VARCHAR(36),
`status` ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
`documents` JSON,
`submitted_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
`reviewed_at` DATETIME,
`rejection_reason` TEXT,
UNIQUE KEY `unique_app_user` (`user_id`),
INDEX `idx_app_status` (`status`),
CONSTRAINT `fk_app_user`
FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
CONSTRAINT `fk_app_reviewer`
FOREIGN KEY (`reviewed_by`) REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `refresh_tokens` (
`id` VARCHAR(36) PRIMARY KEY,
`user_id` VARCHAR(36) NOT NULL,
`token` VARCHAR(512) NOT NULL UNIQUE,
`expires_at` DATETIME NOT NULL,
`is_revoked` TINYINT(1) DEFAULT 0,
`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
INDEX `idx_rt_user` (`user_id`),
CONSTRAINT `fk_rt_user`
FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `quiz_questions` (
`id` VARCHAR(36) PRIMARY KEY,
`lesson_id` VARCHAR(36) NOT NULL,
`question_text` TEXT NOT NULL,
`options` JSON NOT NULL,
`correct_answer` VARCHAR(255) NOT NULL,
`explanation` TEXT,
`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
`updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
INDEX `idx_qq_lesson` (`lesson_id`),
CONSTRAINT `fk_qq_lesson`
FOREIGN KEY (`lesson_id`) REFERENCES `lessons`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `quiz_results` (
`id` VARCHAR(36) PRIMARY KEY,
`user_id` VARCHAR(36) NOT NULL,
`lesson_id` VARCHAR(36) NOT NULL,
`score` DECIMAL(5,2),
`answers` JSON,
`completed_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
`updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
UNIQUE KEY `unique_quiz_res` (`user_id`, `lesson_id`),
CONSTRAINT `fk_qr_user`
FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
CONSTRAINT `fk_qr_lesson`
FOREIGN KEY (`lesson_id`) REFERENCES `lessons`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `system_logs` (
`id` BIGINT AUTO_INCREMENT PRIMARY KEY,
`user_id` VARCHAR(36) NOT NULL COMMENT 'Người thực hiện',
`action` ENUM('CREATE','UPDATE','DELETE') NOT NULL,
`entity_type` VARCHAR(50) NOT NULL COMMENT 'Tên bảng hoặc module, ví dụ: courses, lessons…',
`entity_id` VARCHAR(36) COMMENT 'ID của bản ghi bị tác động (nullable nếu không có)',
`old_values` JSON COMMENT 'Giá trị cũ (JSON object, chỉ dùng cho UPDATE/DELETE)',
`new_values` JSON COMMENT 'Giá trị mới (JSON object, chỉ dùng cho CREATE/UPDATE)',
`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
INDEX `idx_logs_user` (`user_id`),
INDEX `idx_logs_action` (`action`),
INDEX `idx_logs_entity` (`entity_type`, `entity_id`),
INDEX `idx_logs_created` (`created_at`),
CONSTRAINT `fk_logs_user`
FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE NULL 
) ENGINE=InnoDB;
