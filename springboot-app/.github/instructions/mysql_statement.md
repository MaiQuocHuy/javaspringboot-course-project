-- MySQL database export
START TRANSACTION;

CREATE TABLE IF NOT EXISTS `CATEGORY` (
`id` VARCHAR(36),
`name` VARCHAR(255) NOT NULL UNIQUE,
`created_at` DATETIME DEFAULT '[object Object]',
`updated_at` DATETIME DEFAULT '[object Object]',
PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `LESSON_COMPLETION` (
`id` VARCHAR(36),
`user_id` VARCHAR(36) NOT NULL,
`lesson_id` VARCHAR(36) NOT NULL,
`completed_at` DATETIME DEFAULT '[object Object]',
PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `ENROLLMENT` (
`id` VARCHAR(36),
`user_id` VARCHAR(36) NOT NULL,
`course_id` VARCHAR(36) NOT NULL,
`enrolled_at` DATETIME DEFAULT '[object Object]',
`completion_status` VARCHAR(50) DEFAULT '[object Object]',
PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `QUIZ_QUESTION` (
`id` VARCHAR(36),
`lesson_id` VARCHAR(36) NOT NULL,
`question_text` TEXT NOT NULL,
`options` JSON NOT NULL,
`correct_answer` VARCHAR(255) NOT NULL,
`explanation` TEXT,
`created_at` DATETIME DEFAULT '[object Object]',
`updated_at` DATETIME DEFAULT '[object Object]',
PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `INSTRUCTOR_APPLICATION` (
`id` VARCHAR(36),
`user_id` VARCHAR(36) NOT NULL,
`reviewed_by` VARCHAR(36),
`status` VARCHAR(50) DEFAULT '[object Object]',
`documents` JSON,
`submitted_at` DATETIME DEFAULT '[object Object]',
`reviewed_at` DATETIME,
`rejection_reason` TEXT,
PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `COURSE` (
`id` VARCHAR(36),
`title` VARCHAR(255) NOT NULL,
`description` TEXT,
`instructor_id` VARCHAR(36),
`price` DECIMAL DEFAULT '[object Object]',
`is_published` TINYINT(1) DEFAULT '[object Object]',
`is_approved` TINYINT(1) DEFAULT '[object Object]',
`thumbnail_url` VARCHAR(255) DEFAULT '[object Object]',
`thumbnail_id` VARCHAR(255) DEFAULT '[object Object]',
`level` VARCHAR(50) DEFAULT '[object Object]',
`is_deleted` TINYINT(1) DEFAULT '[object Object]',
`created_at` DATETIME DEFAULT '[object Object]',
`updated_at` DATETIME DEFAULT '[object Object]',
PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `QUIZ_RESULT` (
`id` VARCHAR(36),
`user_id` VARCHAR(36) NOT NULL,
`lesson_id` VARCHAR(36) NOT NULL,
`score` DECIMAL,
`answers` JSON,
`completed_at` DATETIME DEFAULT '[object Object]',
`created_at` DATETIME DEFAULT '[object Object]',
`updated_at` DATETIME DEFAULT '[object Object]',
PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `REVIEW` (
`id` VARCHAR(36),
`user_id` VARCHAR(36) NOT NULL,
`course_id` VARCHAR(36) NOT NULL,
`rating` INT DEFAULT '[object Object]',
`review_text` TEXT,
`reviewed_at` DATETIME DEFAULT '[object Object]',
`updated_at` DATETIME DEFAULT '[object Object]',
PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `PAYMENT` (
`id` VARCHAR(36),
`user_id` VARCHAR(36) NOT NULL,
`course_id` VARCHAR(36) NOT NULL,
`amount` DECIMAL NOT NULL,
`status` VARCHAR(50) DEFAULT '[object Object]',
`payment_method` VARCHAR(50),
`paid_at` DATETIME,
`created_at` DATETIME DEFAULT '[object Object]',
PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `VIDEO_CONTENT` (
`id` VARCHAR(36),
`url` TEXT NOT NULL,
`duration` INT,
`uploaded_by` VARCHAR(36) NOT NULL,
`created_at` DATETIME DEFAULT '[object Object]',
`updated_at` DATETIME DEFAULT '[object Object]',
PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `USER` (
`id` VARCHAR(36),
`name` VARCHAR(255) NOT NULL,
`email` VARCHAR(255) NOT NULL UNIQUE,
`password` VARCHAR(255) NOT NULL,
`is_active` TINYINT(1) DEFAULT '[object Object]',
`thumbnail_url` VARCHAR(255) DEFAULT '[object Object]',
`thumbnail_id` VARCHAR(255) DEFAULT '[object Object]',
`bio` TEXT DEFAULT '[object Object]',
`created_at` DATETIME DEFAULT '[object Object]',
`updated_at` DATETIME DEFAULT '[object Object]',
`role_id` VARCHAR(255),
PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `SECTION` (
`id` VARCHAR(36),
`course_id` VARCHAR(36) NOT NULL,
`title` VARCHAR(255) NOT NULL,
`order_index` INT DEFAULT '[object Object]',
`created_at` DATETIME DEFAULT '[object Object]',
`updated_at` DATETIME DEFAULT '[object Object]',
PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `LESSON` (
`id` VARCHAR(36),
`section_id` VARCHAR(36) NOT NULL,
`title` VARCHAR(255) NOT NULL,
`type` VARCHAR(50) DEFAULT '[object Object]',
`content_id` VARCHAR(36),
`order_index` INT DEFAULT '[object Object]',
`created_at` DATETIME DEFAULT '[object Object]',
`updated_at` DATETIME DEFAULT '[object Object]',
PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `INSTRUCTOR_EARNING` (
`id` VARCHAR(36),
`instructor_id` VARCHAR(36) NOT NULL,
`payment_id` VARCHAR(36) NOT NULL,
`course_id` VARCHAR(36) NOT NULL,
`amount` DECIMAL NOT NULL,
`status` VARCHAR(50) DEFAULT '[object Object]',
`available_at` DATETIME,
`paid_at` DATETIME,
`created_at` DATETIME DEFAULT '[object Object]',
`updated_at` DATETIME DEFAULT '[object Object]',
PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `REFRESH_TOKEN` (
`id` INT,
`user_id` VARCHAR(36) NOT NULL,
`token` VARCHAR(512) NOT NULL,
`expires_at` DATETIME NOT NULL,
`is_revoked` TINYINT(1) DEFAULT '[object Object]',
`created_at` DATETIME DEFAULT '[object Object]',
PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `USER_ROLE` (
`id` VARCHAR(255),
`role` VARCHAR(50) NOT NULL,
PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `COURSE_CATEGORY` (
`course_id` VARCHAR(36) NOT NULL,
`category_id` VARCHAR(36) NOT NULL,
PRIMARY KEY (`course_id`, `category_id`)
);

CREATE TABLE IF NOT EXISTS `REFUND` (
`id` VARCHAR(36),
`payment_id` VARCHAR(36) NOT NULL,
`amount` DECIMAL NOT NULL,
`status` VARCHAR(50) DEFAULT '[object Object]',
`reason` TEXT,
`requested_at` DATETIME DEFAULT '[object Object]',
`processed_at` DATETIME,
PRIMARY KEY (`id`)
);

-- Foreign key constraints
ALTER TABLE `COURSE_CATEGORY` ADD CONSTRAINT `fk_COURSE_CATEGORY_category_id` FOREIGN KEY(`category_id`) REFERENCES `CATEGORY`(`id`);
ALTER TABLE `COURSE_CATEGORY` ADD CONSTRAINT `fk_COURSE_CATEGORY_course_id` FOREIGN KEY(`course_id`) REFERENCES `COURSE`(`id`);
ALTER TABLE `COURSE` ADD CONSTRAINT `fk_COURSE_instructor_id` FOREIGN KEY(`instructor_id`) REFERENCES `USER`(`id`);
ALTER TABLE `ENROLLMENT` ADD CONSTRAINT `fk_ENROLLMENT_course_id` FOREIGN KEY(`course_id`) REFERENCES `COURSE`(`id`);
ALTER TABLE `ENROLLMENT` ADD CONSTRAINT `fk_ENROLLMENT_user_id` FOREIGN KEY(`user_id`) REFERENCES `USER`(`id`);
ALTER TABLE `INSTRUCTOR_APPLICATION` ADD CONSTRAINT `fk_INSTRUCTOR_APPLICATION_reviewed_by` FOREIGN KEY(`reviewed_by`) REFERENCES `USER`(`id`);
ALTER TABLE `INSTRUCTOR_APPLICATION` ADD CONSTRAINT `fk_INSTRUCTOR_APPLICATION_user_id` FOREIGN KEY(`user_id`) REFERENCES `USER`(`id`);
ALTER TABLE `INSTRUCTOR_EARNING` ADD CONSTRAINT `fk_INSTRUCTOR_EARNING_course_id` FOREIGN KEY(`course_id`) REFERENCES `COURSE`(`id`);
ALTER TABLE `INSTRUCTOR_EARNING` ADD CONSTRAINT `fk_INSTRUCTOR_EARNING_instructor_id` FOREIGN KEY(`instructor_id`) REFERENCES `USER`(`id`);
ALTER TABLE `INSTRUCTOR_EARNING` ADD CONSTRAINT `fk_INSTRUCTOR_EARNING_payment_id` FOREIGN KEY(`payment_id`) REFERENCES `PAYMENT`(`id`);
ALTER TABLE `LESSON_COMPLETION` ADD CONSTRAINT `fk_LESSON_COMPLETION_lesson_id` FOREIGN KEY(`lesson_id`) REFERENCES `LESSON`(`id`);
ALTER TABLE `USER` ADD CONSTRAINT `fk_USER_id` FOREIGN KEY(`id`) REFERENCES `LESSON_COMPLETION`(`user_id`);
ALTER TABLE `LESSON` ADD CONSTRAINT `fk_LESSON_content_id` FOREIGN KEY(`content_id`) REFERENCES `VIDEO_CONTENT`(`id`);
ALTER TABLE `LESSON` ADD CONSTRAINT `fk_LESSON_section_id` FOREIGN KEY(`section_id`) REFERENCES `SECTION`(`id`);
ALTER TABLE `PAYMENT` ADD CONSTRAINT `fk_PAYMENT_course_id` FOREIGN KEY(`course_id`) REFERENCES `COURSE`(`id`);
ALTER TABLE `PAYMENT` ADD CONSTRAINT `fk_PAYMENT_user_id` FOREIGN KEY(`user_id`) REFERENCES `USER`(`id`);
ALTER TABLE `QUIZ_QUESTION` ADD CONSTRAINT `fk_QUIZ_QUESTION_lesson_id` FOREIGN KEY(`lesson_id`) REFERENCES `LESSON`(`id`);
ALTER TABLE `QUIZ_RESULT` ADD CONSTRAINT `fk_QUIZ_RESULT_lesson_id` FOREIGN KEY(`lesson_id`) REFERENCES `LESSON`(`id`);
ALTER TABLE `QUIZ_RESULT` ADD CONSTRAINT `fk_QUIZ_RESULT_user_id` FOREIGN KEY(`user_id`) REFERENCES `USER`(`id`);
ALTER TABLE `REFRESH_TOKEN` ADD CONSTRAINT `fk_REFRESH_TOKEN_user_id` FOREIGN KEY(`user_id`) REFERENCES `USER`(`id`);
ALTER TABLE `REFUND` ADD CONSTRAINT `fk_REFUND_payment_id` FOREIGN KEY(`payment_id`) REFERENCES `PAYMENT`(`id`);
ALTER TABLE `REVIEW` ADD CONSTRAINT `fk_REVIEW_course_id` FOREIGN KEY(`course_id`) REFERENCES `COURSE`(`id`);
ALTER TABLE `REVIEW` ADD CONSTRAINT `fk_REVIEW_user_id` FOREIGN KEY(`user_id`) REFERENCES `USER`(`id`);
ALTER TABLE `SECTION` ADD CONSTRAINT `fk_SECTION_course_id` FOREIGN KEY(`course_id`) REFERENCES `COURSE`(`id`);
ALTER TABLE `USER` ADD CONSTRAINT `fk_USER_role_id` FOREIGN KEY(`role_id`) REFERENCES `USER_ROLE`(`id`);

COMMIT;
