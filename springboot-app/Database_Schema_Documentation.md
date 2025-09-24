# Tài Liệu Database Schema - Hệ Thống Quản Lý Khóa Học

## Tổng Quan
Tài liệu này mô tả cấu trúc database của hệ thống quản lý khóa học trực tuyến, bao gồm 31 bảng chính được tổ chức theo các module chức năng.

---

## 1. BẢNG QUẢN LÝ NGƯỜI DÙNG

### 1.1 users - Bảng Người Dùng
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh người dùng (UUID)
email VARCHAR(150) NOT NULL UNIQUE - Địa chỉ email đăng nhập
password VARCHAR(255) NOT NULL - Mật khẩu đã mã hóa
full_name VARCHAR(100) NOT NULL - Họ và tên đầy đủ
phone VARCHAR(20) - Số điện thoại liên lạc
avatar_url VARCHAR(500) - Đường dẫn ảnh đại diện
email_verified BOOLEAN DEFAULT FALSE - Trạng thái xác thực email
is_active BOOLEAN DEFAULT TRUE - Trạng thái hoạt động tài khoản
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo tài khoản
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật cuối
```
**Quan hệ**: Liên kết với tất cả các bảng khác qua foreign key user_id

### 1.2 user_roles - Bảng Vai Trò Người Dùng
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh vai trò
user_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại liên kết users.id
role VARCHAR(50) NOT NULL - Tên vai trò (STUDENT, INSTRUCTOR, ADMIN)
assigned_by VARCHAR(36) (FK) - Người gán vai trò
assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian gán vai trò
is_active BOOLEAN DEFAULT TRUE - Trạng thái hoạt động vai trò
```
**Quan hệ**: users.id → user_roles.user_id (CASCADE)

---

## 2. BẢNG QUẢN LÝ KHÓA HỌC

### 2.1 categories - Bảng Danh Mục Khóa Học
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh danh mục
name VARCHAR(150) NOT NULL UNIQUE - Tên danh mục
slug VARCHAR(150) NOT NULL UNIQUE - Slug SEO-friendly
description TEXT - Mô tả chi tiết danh mục
is_active BOOLEAN DEFAULT TRUE - Trạng thái hoạt động
display_order INT DEFAULT 0 - Thứ tự hiển thị
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```
**Quan hệ**: Liên kết với courses.category_id

### 2.2 courses - Bảng Khóa Học
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh khóa học
title VARCHAR(200) NOT NULL - Tiêu đề khóa học
slug VARCHAR(200) NOT NULL UNIQUE - Slug SEO-friendly
description TEXT - Mô tả chi tiết khóa học
short_description VARCHAR(500) - Mô tả ngắn gọn
thumbnail_url VARCHAR(500) - Đường dẫn ảnh thumbnail
instructor_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại users.id (giảng viên)
category_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại categories.id
price DECIMAL(10,2) NOT NULL DEFAULT 0.00 - Giá khóa học
original_price DECIMAL(10,2) - Giá gốc (trước khuyến mãi)
status ENUM('DRAFT','PUBLISHED','ARCHIVED') DEFAULT 'DRAFT' - Trạng thái khóa học
level ENUM('BEGINNER','INTERMEDIATE','ADVANCED') - Cấp độ khóa học
language VARCHAR(10) DEFAULT 'vi' - Ngôn ngữ khóa học
duration_hours INT DEFAULT 0 - Tổng thời lượng (giờ)
is_featured BOOLEAN DEFAULT FALSE - Khóa học nổi bật
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
published_at TIMESTAMP - Thời gian xuất bản
```
**Quan hệ**: 
- users.id → courses.instructor_id (RESTRICT)
- categories.id → courses.category_id (RESTRICT)

### 2.3 sections - Bảng Chương Học
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh chương
course_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại courses.id
title VARCHAR(200) NOT NULL - Tiêu đề chương học
description TEXT - Mô tả chương học
display_order INT NOT NULL - Thứ tự hiển thị
is_active BOOLEAN DEFAULT TRUE - Trạng thái hoạt động
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```
**Quan hệ**: courses.id → sections.course_id (CASCADE)

### 2.4 lessons - Bảng Bài Học
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh bài học
section_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại sections.id
title VARCHAR(200) NOT NULL - Tiêu đề bài học
content LONGTEXT - Nội dung chi tiết bài học
video_url VARCHAR(500) - Đường dẫn video bài học
duration_minutes INT DEFAULT 0 - Thời lượng bài học (phút)
display_order INT NOT NULL - Thứ tự hiển thị trong chương
lesson_type ENUM('VIDEO','TEXT','QUIZ','DOCUMENT') DEFAULT 'VIDEO' - Loại bài học
is_preview BOOLEAN DEFAULT FALSE - Cho phép xem trước miễn phí
is_active BOOLEAN DEFAULT TRUE - Trạng thái hoạt động
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```
**Quan hệ**: sections.id → lessons.section_id (CASCADE)

---

## 3. BẢNG QUẢN LÝ ĐĂNG KÝ VÀ TIẾN ĐỘ HỌC TẬP

### 3.1 enrollments - Bảng Đăng Ký Khóa Học
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh đăng ký
user_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại users.id (học viên)
course_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại courses.id
enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian đăng ký
progress_percent DECIMAL(5,2) DEFAULT 0.00 - Phần trăm tiến độ hoàn thành
last_accessed_at TIMESTAMP - Thời gian truy cập cuối cùng
completed_at TIMESTAMP - Thời gian hoàn thành khóa học
status ENUM('ACTIVE','COMPLETED','CANCELLED','EXPIRED') DEFAULT 'ACTIVE' - Trạng thái đăng ký
certificate_issued BOOLEAN DEFAULT FALSE - Đã cấp chứng chỉ
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```
**Quan hệ**: 
- users.id → enrollments.user_id (CASCADE)
- courses.id → enrollments.course_id (CASCADE)
**Unique**: (user_id, course_id) - Không trùng lặp đăng ký

### 3.2 lesson_progress - Bảng Tiến Độ Bài Học
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh tiến độ
enrollment_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại enrollments.id
lesson_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại lessons.id
is_completed BOOLEAN DEFAULT FALSE - Trạng thái hoàn thành bài học
completed_at TIMESTAMP - Thời gian hoàn thành
last_position_seconds INT DEFAULT 0 - Vị trí dừng video (giây)
watch_time_seconds INT DEFAULT 0 - Thời gian xem thực tế (giây)
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```
**Quan hệ**: 
- enrollments.id → lesson_progress.enrollment_id (CASCADE)
- lessons.id → lesson_progress.lesson_id (CASCADE)
**Unique**: (enrollment_id, lesson_id) - Không trùng lặp tiến độ

---

## 4. BẢNG QUẢN LÝ THANH TOÁN

### 4.1 payments - Bảng Thanh Toán
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh thanh toán
user_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại users.id (người thanh toán)
course_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại courses.id
amount DECIMAL(10,2) NOT NULL - Số tiền thanh toán
currency VARCHAR(10) DEFAULT 'VND' - Loại tiền tệ
payment_method VARCHAR(50) - Phương thức thanh toán
payment_gateway VARCHAR(50) - Cổng thanh toán sử dụng
transaction_id VARCHAR(100) - Mã giao dịch từ gateway
status ENUM('PENDING','COMPLETED','FAILED','CANCELLED','REFUNDED') DEFAULT 'PENDING' - Trạng thái thanh toán
metadata JSON - Thông tin bổ sung dạng JSON
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
completed_at TIMESTAMP - Thời gian hoàn thành thanh toán
```
**Quan hệ**: 
- users.id → payments.user_id (RESTRICT)
- courses.id → payments.course_id (RESTRICT)

### 4.2 refunds - Bảng Hoàn Tiền
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh hoàn tiền
payment_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại payments.id
user_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại users.id
amount DECIMAL(10,2) NOT NULL - Số tiền hoàn lại
reason TEXT - Lý do hoàn tiền
status ENUM('PENDING','APPROVED','REJECTED','COMPLETED') DEFAULT 'PENDING' - Trạng thái hoàn tiền
processed_by VARCHAR(36) (FK) - Người xử lý hoàn tiền
processed_at TIMESTAMP - Thời gian xử lý
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```
**Quan hệ**: 
- payments.id → refunds.payment_id (RESTRICT)
- users.id → refunds.user_id (RESTRICT)
- users.id → refunds.processed_by (SET NULL)

---

## 5. BẢNG ĐÁNH GIÁ VÀ PHẢN HỒI

### 5.1 reviews - Bảng Đánh Giá Khóa Học
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh đánh giá
user_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại users.id (người đánh giá)
course_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại courses.id
rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5) - Điểm đánh giá (1-5 sao)
comment TEXT - Nội dung bình luận đánh giá
is_anonymous BOOLEAN DEFAULT FALSE - Ẩn danh đánh giá
is_approved BOOLEAN DEFAULT FALSE - Trạng thái duyệt đánh giá
approved_by VARCHAR(36) (FK) - Người duyệt đánh giá
approved_at TIMESTAMP - Thời gian duyệt
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```
**Quan hệ**: 
- users.id → reviews.user_id (CASCADE)
- courses.id → reviews.course_id (CASCADE)
- users.id → reviews.approved_by (SET NULL)
**Unique**: (user_id, course_id) - Một người chỉ đánh giá một lần

---

## 6. BẢNG QUẢN LÝ GIẢNG VIÊN

### 6.1 instructor_profiles - Bảng Hồ Sơ Giảng Viên
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh hồ sơ
user_id VARCHAR(36) (FK) NOT NULL UNIQUE - Khóa ngoại users.id
bio TEXT - Tiểu sử và kinh nghiệm giảng viên
specialization VARCHAR(200) - Chuyên môn chính
experience_years INT DEFAULT 0 - Số năm kinh nghiệm
education_background TEXT - Bằng cấp và học vấn
achievements TEXT - Các thành tựu đạt được
social_links JSON - Liên kết mạng xã hội (JSON)
hourly_rate DECIMAL(10,2) - Mức phí theo giờ
is_verified BOOLEAN DEFAULT FALSE - Trạng thái xác minh giảng viên
verified_by VARCHAR(36) (FK) - Người xác minh
verified_at TIMESTAMP - Thời gian xác minh
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```
**Quan hệ**: 
- users.id → instructor_profiles.user_id (CASCADE)
- users.id → instructor_profiles.verified_by (SET NULL)

### 6.2 instructor_earnings - Bảng Thu Nhập Giảng Viên
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh thu nhập
instructor_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại users.id (giảng viên)
course_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại courses.id
payment_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại payments.id
gross_amount DECIMAL(10,2) NOT NULL - Tổng thu nhập trước phí
platform_fee_percent DECIMAL(5,2) NOT NULL - Phần trăm phí nền tảng
platform_fee_amount DECIMAL(10,2) NOT NULL - Số tiền phí nền tảng
net_amount DECIMAL(10,2) NOT NULL - Thu nhập thực tế sau phí
payout_status ENUM('PENDING','PROCESSING','COMPLETED','FAILED') DEFAULT 'PENDING' - Trạng thái chi trả
payout_date TIMESTAMP - Ngày chi trả thực tế
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian ghi nhận
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```
**Quan hệ**: 
- users.id → instructor_earnings.instructor_id (RESTRICT)
- courses.id → instructor_earnings.course_id (RESTRICT)
- payments.id → instructor_earnings.payment_id (RESTRICT)

### 6.3 instructor_applications - Bảng Đơn Xin Làm Giảng Viên
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh đơn đăng ký
user_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại users.id (người nộp đơn)
application_data JSON NOT NULL - Dữ liệu đơn đăng ký (JSON)
status ENUM('PENDING','UNDER_REVIEW','APPROVED','REJECTED') DEFAULT 'PENDING' - Trạng thái đơn
reviewed_by VARCHAR(36) (FK) - Người xem xét đơn
reviewed_at TIMESTAMP - Thời gian xem xét
review_notes TEXT - Ghi chú từ người xem xét
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian nộp đơn
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```
**Quan hệ**: 
- users.id → instructor_applications.user_id (CASCADE)
- users.id → instructor_applications.reviewed_by (SET NULL)

---

## 7. BẢNG BẢO MẬT VÀ PHIÊN LÀM VIỆC

### 7.1 user_tokens - Bảng Token Người Dùng
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh token
user_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại users.id
token_hash VARCHAR(255) NOT NULL - Hash của token
token_type ENUM('ACCESS','REFRESH','RESET_PASSWORD','EMAIL_VERIFICATION') NOT NULL - Loại token
expires_at TIMESTAMP NOT NULL - Thời gian hết hạn
is_revoked BOOLEAN DEFAULT FALSE - Trạng thái thu hồi token
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
used_at TIMESTAMP - Thời gian sử dụng
```
**Quan hệ**: users.id → user_tokens.user_id (CASCADE)

---

## 8. BẢNG KIỂM TRA VÀ QUIZ

### 8.1 quizzes - Bảng Bài Kiểm Tra
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh quiz
lesson_id VARCHAR(36) (FK) - Khóa ngoại lessons.id (nếu thuộc bài học)
course_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại courses.id
title VARCHAR(200) NOT NULL - Tiêu đề bài kiểm tra
description TEXT - Mô tả bài kiểm tra
time_limit_minutes INT - Giới hạn thời gian (phút)
passing_score DECIMAL(5,2) DEFAULT 70.00 - Điểm đậu tối thiểu
max_attempts INT DEFAULT 3 - Số lần làm bài tối đa
is_active BOOLEAN DEFAULT TRUE - Trạng thái hoạt động
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```
**Quan hệ**: 
- lessons.id → quizzes.lesson_id (CASCADE)
- courses.id → quizzes.course_id (CASCADE)

### 8.2 quiz_questions - Bảng Câu Hỏi Quiz
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh câu hỏi
quiz_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại quizzes.id
question_text TEXT NOT NULL - Nội dung câu hỏi
question_type ENUM('MULTIPLE_CHOICE','TRUE_FALSE','FILL_BLANK') DEFAULT 'MULTIPLE_CHOICE' - Loại câu hỏi
options JSON - Các lựa chọn (dạng JSON cho multiple choice)
correct_answer TEXT NOT NULL - Đáp án đúng
explanation TEXT - Giải thích đáp án
points DECIMAL(5,2) DEFAULT 1.00 - Điểm số câu hỏi
display_order INT NOT NULL - Thứ tự hiển thị
is_active BOOLEAN DEFAULT TRUE - Trạng thái hoạt động
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```
**Quan hệ**: quizzes.id → quiz_questions.quiz_id (CASCADE)

### 8.3 quiz_attempts - Bảng Lần Làm Quiz
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh lần làm
user_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại users.id
quiz_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại quizzes.id
score DECIMAL(5,2) - Điểm số đạt được
max_score DECIMAL(5,2) NOT NULL - Điểm tối đa
is_passed BOOLEAN DEFAULT FALSE - Trạng thái đậu/rớt
attempt_number INT NOT NULL - Lần thử thứ mấy
time_spent_minutes INT - Thời gian làm bài (phút)
answers JSON - Câu trả lời chi tiết (JSON)
started_at TIMESTAMP NOT NULL - Thời gian bắt đầu
completed_at TIMESTAMP - Thời gian hoàn thành
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
```
**Quan hệ**: 
- users.id → quiz_attempts.user_id (CASCADE)
- quizzes.id → quiz_attempts.quiz_id (CASCADE)

---

## 9. BẢNG NHẬT KÝ HỆ THỐNG

### 9.1 system_logs - Bảng Nhật Ký Hệ Thống
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh log
user_id VARCHAR(36) (FK) - Khóa ngoại users.id (nếu có)
action VARCHAR(100) NOT NULL - Hành động thực hiện
resource_type VARCHAR(50) - Loại tài nguyên tác động
resource_id VARCHAR(36) - ID tài nguyên cụ thể
ip_address VARCHAR(45) - Địa chỉ IP thực hiện
user_agent TEXT - Thông tin trình duyệt
metadata JSON - Dữ liệu bổ sung (JSON)
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian ghi log
```
**Quan hệ**: users.id → system_logs.user_id (SET NULL)

---

## 10. BẢNG HỆ THỐNG PHÂN QUYỀN (RBAC)

### 10.1 filter_types - Bảng Loại Bộ Lọc
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh loại bộ lọc
name VARCHAR(50) NOT NULL UNIQUE - Tên loại bộ lọc
description TEXT - Mô tả chi tiết loại bộ lọc
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```

### 10.2 resources - Bảng Tài Nguyên Hệ Thống
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh tài nguyên
name VARCHAR(100) NOT NULL UNIQUE - Tên tài nguyên
description TEXT - Mô tả chi tiết tài nguyên
parent_id VARCHAR(36) (FK) - Khóa ngoại tự tham chiếu (phân cấp)
resource_path VARCHAR(255) - Đường dẫn API tài nguyên
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```
**Quan hệ**: resources.id → resources.parent_id (CASCADE) - Tự tham chiếu phân cấp

### 10.3 actions - Bảng Hành Động Hệ Thống
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh hành động
name VARCHAR(50) NOT NULL UNIQUE - Tên hành động (CREATE, READ, UPDATE, DELETE)
description TEXT - Mô tả chi tiết hành động
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```

### 10.4 permissions - Bảng Quyền Hạn
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh quyền
resource_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại resources.id
action_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại actions.id
permission_key VARCHAR(255) NOT NULL UNIQUE - Khóa quyền duy nhất
description TEXT - Mô tả chi tiết quyền hạn
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```
**Quan hệ**: 
- resources.id → permissions.resource_id (CASCADE)
- actions.id → permissions.action_id (CASCADE)
**Unique**: (resource_id, action_id) - Không trùng lặp quyền

### 10.5 role_permissions - Bảng Phân Quyền Vai Trò
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh phân quyền
role VARCHAR(50) NOT NULL - Tên vai trò (STUDENT, INSTRUCTOR, ADMIN)
permission_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại permissions.id
filter_type_id VARCHAR(36) (FK) - Khóa ngoại filter_types.id (bộ lọc)
filter_conditions JSON - Điều kiện lọc dạng JSON
is_granted BOOLEAN DEFAULT TRUE - Trạng thái cấp quyền
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```
**Quan hệ**: 
- permissions.id → role_permissions.permission_id (CASCADE)
- filter_types.id → role_permissions.filter_type_id (SET NULL)
**Unique**: (role, permission_id) - Không trùng lặp phân quyền

---

## 11. BẢNG HỆ THỐNG GIẢM GIÁ VÀ AFFILIATE

### 11.1 discounts - Bảng Mã Giảm Giá
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh mã giảm giá
code VARCHAR(50) NOT NULL UNIQUE - Mã giảm giá duy nhất
discount_percent DECIMAL(5,2) - Phần trăm giảm giá (0-100)
discount_amount DECIMAL(10,2) - Số tiền giảm cố định
description TEXT - Mô tả chi tiết mã giảm giá
type ENUM('GENERAL','REFERRAL') DEFAULT 'GENERAL' - Loại mã giảm giá
owner_user_id VARCHAR(36) (FK) - Khóa ngoại users.id (chủ sở hữu mã)
applicable_courses JSON - Danh sách khóa học áp dụng (JSON)
start_date TIMESTAMP NOT NULL - Thời gian bắt đầu hiệu lực
end_date TIMESTAMP NOT NULL - Thời gian kết thúc hiệu lực
usage_limit INT - Giới hạn số lần sử dụng tổng
per_user_limit INT - Giới hạn số lần sử dụng mỗi người
times_used INT DEFAULT 0 - Số lần đã sử dụng
is_active BOOLEAN DEFAULT TRUE - Trạng thái hoạt động
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
```
**Quan hệ**: users.id → discounts.owner_user_id (SET NULL)

### 11.2 discount_usages - Bảng Lịch Sử Sử Dụng Mã Giảm Giá
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh lần sử dụng
discount_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại discounts.id
user_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại users.id (người sử dụng)
payment_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại payments.id
discount_amount DECIMAL(10,2) NOT NULL - Số tiền được giảm thực tế
referred_by_user_id VARCHAR(36) (FK) - Khóa ngoại users.id (người giới thiệu)
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian sử dụng
```
**Quan hệ**: 
- discounts.id → discount_usages.discount_id (CASCADE)
- users.id → discount_usages.user_id (CASCADE)
- payments.id → discount_usages.payment_id (CASCADE)
- users.id → discount_usages.referred_by_user_id (SET NULL)
**Unique**: (discount_id, payment_id) - Không trùng lặp sử dụng

### 11.3 affiliate_payouts - Bảng Chi Trả Hoa Hồng Affiliate
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh chi trả
referred_by_user_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại users.id (người giới thiệu)
course_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại courses.id
discount_usage_id VARCHAR(36) (FK) - Khóa ngoại discount_usages.id
commission_percent DECIMAL(4,2) NOT NULL - Phần trăm hoa hồng
commission_amount DECIMAL(10,2) NOT NULL - Số tiền hoa hồng
payout_status VARCHAR(20) DEFAULT 'PENDING' - Trạng thái chi trả (PENDING, PAID, CANCELLED)
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
paid_at TIMESTAMP - Thời gian chi trả thực tế
cancelled_at TIMESTAMP - Thời gian hủy chi trả
```
**Quan hệ**: 
- users.id → affiliate_payouts.referred_by_user_id (RESTRICT)
- courses.id → affiliate_payouts.course_id (RESTRICT)
- discount_usages.id → affiliate_payouts.discount_usage_id (SET NULL)

---

## 12. BẢNG HỆ THỐNG THÔNG BÁO

### 12.1 notifications - Bảng Thông Báo
```
id VARCHAR(36) (PK) NOT NULL - Khóa chính định danh thông báo
user_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại users.id (người nhận)
resource_id VARCHAR(36) (FK) NOT NULL - Khóa ngoại resources.id (tài nguyên liên quan)
entity_id VARCHAR(36) NOT NULL - ID thực thể cụ thể (course_id, payment_id, etc.)
message TEXT NOT NULL - Nội dung thông báo
action_url VARCHAR(255) NOT NULL - URL hành động khi click thông báo
priority ENUM('LOW','MEDIUM','HIGH') DEFAULT 'LOW' - Mức độ ưu tiên
is_read BOOLEAN DEFAULT FALSE - Trạng thái đã đọc
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP - Thời gian tạo
read_at TIMESTAMP - Thời gian đọc thông báo
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE - Thời gian cập nhật
expired_at TIMESTAMP - Thời gian hết hạn thông báo
```
**Quan hệ**: 
- users.id → notifications.user_id (CASCADE)
- resources.id → notifications.resource_id (CASCADE)

---

## TỔNG KẾT QUAN HỆ CHÍNH

### Quan Hệ 1-n (One-to-Many):
- **users** → **user_roles** (Một người dùng có nhiều vai trò)
- **users** → **courses** (Một giảng viên có nhiều khóa học)
- **users** → **enrollments** (Một học viên đăng ký nhiều khóa học)
- **courses** → **sections** (Một khóa học có nhiều chương)
- **sections** → **lessons** (Một chương có nhiều bài học)
- **courses** → **reviews** (Một khóa học có nhiều đánh giá)
- **users** → **payments** (Một người dùng có nhiều thanh toán)

### Quan Hệ n-n (Many-to-Many):
- **users** ↔ **courses** (qua bảng enrollments)
- **roles** ↔ **permissions** (qua bảng role_permissions)
- **discounts** ↔ **payments** (qua bảng discount_usages)

### Đặc Điểm Kỹ Thuật:
- **UUID Primary Keys**: Tất cả bảng sử dụng VARCHAR(36) UUID làm khóa chính
- **Timestamp Auditing**: Các bảng có created_at và updated_at để theo dõi thay đổi
- **Soft Delete**: Sử dụng cột is_active thay vì xóa vật lý
- **JSON Columns**: Lưu trữ dữ liệu phức tạp như metadata, options, social_links
- **Enum Constraints**: Sử dụng ENUM để ràng buộc giá trị hợp lệ
- **Comprehensive Indexing**: Index trên các cột thường được query và foreign keys

### Performance Considerations:
- **Composite Indexes**: Tạo index phức hợp cho các query thường dùng
- **Foreign Key Constraints**: Đảm bảo tính toàn vẹn dữ liệu với CASCADE/RESTRICT/SET NULL
- **Unique Constraints**: Ngăn chặn dữ liệu trùng lặp trên các cột quan trọng
- **Check Constraints**: Validate dữ liệu tại database level (rating 1-5, percent 0-100)

---

*Tài liệu này được tạo tự động từ database schema và sẽ được cập nhật khi có thay đổi cấu trúc.*