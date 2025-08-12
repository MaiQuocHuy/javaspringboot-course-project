# KTC Course Platform - Backend API

## 📖 Tổng Quan

Đây là backend API cho nền tảng học trực tuyến KTC Course Platform, được xây dựng bằng Spring Boot. Hệ thống cung cấp các API cho việc quản lý khóa học, người dùng, thanh toán và các tính năng liên quan.

## 🚀 Cài Đặt & Chạy

### Yêu Cầu Hệ Thống

- Java 17+
- MySQL 8.0+
- Maven 3.6+

### Chạy Ứng Dụng

```bash
# Clone repository
git clone <repository-url>
cd springboot-app

# Cài đặt dependencies
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

### Cấu Hình Database

```properties
# File .env.local
DB_URL=jdbc:mysql://localhost:3306/ktc_course
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

### API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

---

## 📋 Danh Sách API Resources

### 🔐 1. Authentication APIs

**Base Path**: `/api/auth`

| Method | Endpoint                           | Mô Tả                       | Trạng Thái     |
| ------ | ---------------------------------- | --------------------------- | -------------- |
| POST   | `/register`                        | Đăng ký tài khoản học viên  | ✅ **(Done)**  |
| POST   | `/register/instructor-application` | Đăng ký ứng viên giảng viên | ⏳ **Planned** |
| POST   | `/login`                           | Đăng nhập                   | ✅ **(Done)**  |
| POST   | `/refresh`                         | Refresh token               | ⏳ **Planned** |
| POST   | `/reset-password`                  | Đặt lại mật khẩu            | ⏳ **Planned** |

---

### 👥 2. User Management APIs

#### 2.1 User Profile Management

**Base Path**: `/api/users`

| Method | Endpoint           | Mô Tả                 | Trạng Thái     |
| ------ | ------------------ | --------------------- | -------------- |
| GET    | `/profile`         | Lấy thông tin profile | ✅ **(Done)**  |
| PUT    | `/profile`         | Cập nhật profile      | ⏳ **Planned** |
| PUT    | `/profile/avatar`  | Cập nhật avatar       | ⏳ **Planned** |
| PUT    | `/change-password` | Đổi mật khẩu          | ⏳ **Planned** |

#### 2.2 Admin User Management

**Base Path**: `/api/admin/users`

| Method | Endpoint       | Mô Tả                      | Trạng Thái     |
| ------ | -------------- | -------------------------- | -------------- |
| GET    | `/`            | Lấy danh sách users        | ✅ **(Done)**  |
| GET    | `/{id}`        | Lấy thông tin user         | ✅ **(Done)**  |
| PUT    | `/{id}/status` | Kích hoạt/vô hiệu hóa user | ⏳ **Planned** |
| PUT    | `/{id}/role`   | Thay đổi role user         | ⏳ **Planned** |
| DELETE | `/{id}`        | Xóa user                   | ⏳ **Planned** |

---

### 📚 3. Course Management APIs

#### 3.1 Public Course APIs

**Base Path**: `/api/courses`

| Method | Endpoint        | Mô Tả                            | Trạng Thái    |
| ------ | --------------- | -------------------------------- | ------------- |
| GET    | `/`             | Lấy danh sách khóa học công khai | ✅ **(Done)** |
| GET    | `/{id}`         | Lấy chi tiết khóa học            | ✅ **(Done)** |
| GET    | `/{id}/reviews` | Lấy đánh giá khóa học            | ✅ **(Done)** |
| POST   | `/{id}/reviews` | Thêm đánh giá khóa học           | ✅ **(Done)** |

#### 3.2 Student Course APIs

**Base Path**: `/api/student/courses`

| Method | Endpoint        | Mô Tả               | Trạng Thái    |
| ------ | --------------- | ------------------- | ------------- |
| GET    | `/enrolled`     | Khóa học đã đăng ký | ✅ **(Done)** |
| GET    | `/{id}/content` | Nội dung khóa học   | ✅ **(Done)** |
| POST   | `/{id}/enroll`  | Đăng ký khóa học    | ✅ **(Done)** |

#### 3.3 Instructor Course APIs

**Base Path**: `/api/instructor/courses`

| Method | Endpoint        | Mô Tả                   | Trạng Thái    |
| ------ | --------------- | ----------------------- | ------------- |
| GET    | `/`             | Khóa học của giảng viên | ✅ **(Done)** |
| POST   | `/`             | Tạo khóa học mới        | ✅ **(Done)** |
| GET    | `/{id}`         | Chi tiết khóa học       | ✅ **(Done)** |
| PUT    | `/{id}`         | Cập nhật khóa học       | ✅ **(Done)** |
| DELETE | `/{id}`         | Xóa khóa học            | ✅ **(Done)** |
| PUT    | `/{id}/publish` | Đăng khóa học           | ✅ **(Done)** |

#### 3.4 Admin Course APIs

**Base Path**: `/api/admin/courses`

| Method | Endpoint        | Mô Tả            | Trạng Thái    |
| ------ | --------------- | ---------------- | ------------- |
| GET    | `/`             | Tất cả khóa học  | ✅ **(Done)** |
| PUT    | `/{id}/approve` | Duyệt khóa học   | ✅ **(Done)** |
| PUT    | `/{id}/reject`  | Từ chối khóa học | ✅ **(Done)** |

---

### 📂 4. Category Management APIs

**Base Path**: `/api/categories`

| Method | Endpoint | Mô Tả                    | Trạng Thái     |
| ------ | -------- | ------------------------ | -------------- |
| GET    | `/`      | Lấy danh sách categories | ✅ **(Done)**  |
| POST   | `/`      | Tạo category mới         | ⏳ **Planned** |
| GET    | `/{id}`  | Lấy chi tiết category    | ✅ **(Done)**  |
| PUT    | `/{id}`  | Cập nhật category        | ⏳ **Planned** |
| DELETE | `/{id}`  | Xóa category             | ⏳ **Planned** |

---

### 📖 5. Section & Lesson Management APIs

#### 5.1 Section Management

**Base Path**: `/api/instructor/courses/{courseId}/sections`

| Method | Endpoint        | Mô Tả                     | Trạng Thái     |
| ------ | --------------- | ------------------------- | -------------- |
| GET    | `/`             | Lấy sections của khóa học | ✅ **(Done)**  |
| POST   | `/`             | Tạo section mới           | ⏳ **Planned** |
| GET    | `/{id}`         | Lấy chi tiết section      | ✅ **(Done)**  |
| PUT    | `/{id}`         | Cập nhật section          | ⏳ **Planned** |
| DELETE | `/{id}`         | Xóa section               | ⏳ **Planned** |
| PUT    | `/{id}/reorder` | Sắp xếp lại thứ tự        | ⏳ **Planned** |

#### 5.2 Instructor Lesson Management

**Base Path**: `/api/instructor/lessons`

| Method | Endpoint        | Mô Tả               | Trạng Thái     |
| ------ | --------------- | ------------------- | -------------- |
| POST   | `/`             | Tạo lesson mới      | ⏳ **Planned** |
| GET    | `/{id}`         | Lấy chi tiết lesson | ✅ **(Done)**  |
| PUT    | `/{id}`         | Cập nhật lesson     | ⏳ **Planned** |
| DELETE | `/{id}`         | Xóa lesson          | ⏳ **Planned** |
| PUT    | `/{id}/reorder` | Sắp xếp lại thứ tự  | ⏳ **Planned** |

#### 5.3 Student Lesson APIs

**Base Path**: `/api/student/lessons`

| Method | Endpoint           | Mô Tả                 | Trạng Thái     |
| ------ | ------------------ | --------------------- | -------------- |
| GET    | `/{id}`            | Xem nội dung lesson   | ✅ **(Done)**  |
| POST   | `/{id}/complete`   | Đánh dấu hoàn thành   | ⏳ **Planned** |
| GET    | `/{id}/completion` | Trạng thái hoàn thành | ⏳ **Planned** |

---

### 🎯 6. Quiz Management APIs

#### 6.1 Instructor Quiz Management

**Base Path**: `/api/instructor/quizzes`

| Method | Endpoint      | Mô Tả               | Trạng Thái     |
| ------ | ------------- | ------------------- | -------------- |
| POST   | `/`           | Tạo quiz cho lesson | ⏳ **Planned** |
| GET    | `/{lessonId}` | Lấy quiz của lesson | ✅ **(Done)**  |
| PUT    | `/{id}`       | Cập nhật quiz       | ⏳ **Planned** |
| DELETE | `/{id}`       | Xóa quiz            | ⏳ **Planned** |

#### 6.2 Student Quiz APIs

**Base Path**: `/api/student/quizzes`

| Method | Endpoint             | Mô Tả               | Trạng Thái     |
| ------ | -------------------- | ------------------- | -------------- |
| GET    | `/{lessonId}`        | Lấy quiz để làm bài | ✅ **(Done)**  |
| POST   | `/{lessonId}/submit` | Nộp bài quiz        | ⏳ **Planned** |
| GET    | `/{lessonId}/result` | Kết quả quiz        | ⏳ **Planned** |

#### 6.3 MCQ Management

**Base Path**: `/api/mcq`

| Method | Endpoint          | Mô Tả                | Trạng Thái     |
| ------ | ----------------- | -------------------- | -------------- |
| GET    | `/questions/{id}` | Lấy chi tiết câu hỏi | ✅ **(Done)**  |
| PUT    | `/questions/{id}` | Cập nhật câu hỏi     | ⏳ **Planned** |
| DELETE | `/questions/{id}` | Xóa câu hỏi          | ⏳ **Planned** |

---

### 💳 7. Payment Management APIs

#### 7.1 Student Payment APIs

**Base Path**: `/api/student/payments`

| Method | Endpoint                 | Mô Tả               | Trạng Thái     |
| ------ | ------------------------ | ------------------- | -------------- |
| POST   | `/create-payment-intent` | Tạo payment intent  | ⏳ **Planned** |
| GET    | `/history`               | Lịch sử thanh toán  | ✅ **(Done)**  |
| GET    | `/{id}`                  | Chi tiết thanh toán | ✅ **(Done)**  |

#### 7.2 Stripe Integration

**Base Path**: `/api/stripe`

| Method | Endpoint                   | Mô Tả                 | Trạng Thái     |
| ------ | -------------------------- | --------------------- | -------------- |
| POST   | `/create-checkout-session` | Tạo Stripe checkout   | ⏳ **Planned** |
| POST   | `/webhook`                 | Stripe webhook        | ⏳ **Planned** |
| GET    | `/session/{sessionId}`     | Lấy thông tin session | ✅ **(Done)**  |

---

### 💰 8. Refund Management APIs

#### 8.1 Student Refund APIs

**Base Path**: `/api/student/refunds`

| Method | Endpoint | Mô Tả                       | Trạng Thái     |
| ------ | -------- | --------------------------- | -------------- |
| POST   | `/`      | Yêu cầu hoàn tiền           | ⏳ **Planned** |
| GET    | `/`      | Danh sách yêu cầu hoàn tiền | ✅ **(Done)**  |
| GET    | `/{id}`  | Chi tiết yêu cầu hoàn tiền  | ✅ **(Done)**  |

#### 8.2 Admin Refund Management

**Base Path**: `/api/admin/refunds`

| Method | Endpoint        | Mô Tả                    | Trạng Thái     |
| ------ | --------------- | ------------------------ | -------------- |
| GET    | `/`             | Tất cả yêu cầu hoàn tiền | ✅ **(Done)**  |
| PUT    | `/{id}/approve` | Duyệt hoàn tiền          | ⏳ **Planned** |
| PUT    | `/{id}/reject`  | Từ chối hoàn tiền        | ⏳ **Planned** |

---

### ⭐ 9. Review Management APIs

#### 9.1 Student Review APIs

**Base Path**: `/api/student/reviews`

| Method | Endpoint              | Mô Tả             | Trạng Thái     |
| ------ | --------------------- | ----------------- | -------------- |
| POST   | `/courses/{courseId}` | Thêm đánh giá     | ⏳ **Planned** |
| PUT    | `/{id}`               | Cập nhật đánh giá | ⏳ **Planned** |
| DELETE | `/{id}`               | Xóa đánh giá      | ⏳ **Planned** |
| GET    | `/my-reviews`         | Đánh giá của tôi  | ✅ **(Done)**  |

---

### 📊 10. Enrollment Management APIs

**Base Path**: `/api/enrollments`

| Method | Endpoint         | Mô Tả               | Trạng Thái     |
| ------ | ---------------- | ------------------- | -------------- |
| POST   | `/`              | Đăng ký khóa học    | ⏳ **Planned** |
| GET    | `/my-courses`    | Khóa học đã đăng ký | ✅ **(Done)**  |
| GET    | `/{id}/progress` | Tiến độ học tập     | ⏳ **Planned** |

---

### 🎓 11. Instructor Application APIs

**Base Path**: `/api/instructor-applications`

| Method | Endpoint          | Mô Tả                        | Trạng Thái     |
| ------ | ----------------- | ---------------------------- | -------------- |
| POST   | `/`               | Nộp đơn ứng tuyển            | ⏳ **Planned** |
| GET    | `/my-application` | Đơn ứng tuyển của tôi        | ✅ **(Done)**  |
| GET    | `/`               | Tất cả đơn ứng tuyển (Admin) | ✅ **(Done)**  |
| PUT    | `/{id}/review`    | Duyệt/từ chối đơn            | ⏳ **Planned** |

---

### 💵 12. Instructor Earning APIs

**Base Path**: `/api/instructor/earnings`

| Method | Endpoint   | Mô Tả              | Trạng Thái     |
| ------ | ---------- | ------------------ | -------------- |
| GET    | `/`        | Lịch sử thu nhập   | ⏳ **Planned** |
| GET    | `/summary` | Tổng quan thu nhập | ⏳ **Planned** |

---

### 📤 13. Upload Management APIs

#### 13.1 File Upload

**Base Path**: `/api/upload`

| Method | Endpoint    | Mô Tả           | Trạng Thái     |
| ------ | ----------- | --------------- | -------------- |
| POST   | `/image`    | Upload hình ảnh | ⏳ **Planned** |
| POST   | `/video`    | Upload video    | ⏳ **Planned** |
| DELETE | `/{fileId}` | Xóa file        | ⏳ **Planned** |

#### 13.2 Video Metadata

**Base Path**: `/api/videos`

| Method | Endpoint         | Mô Tả              | Trạng Thái     |
| ------ | ---------------- | ------------------ | -------------- |
| GET    | `/{id}/metadata` | Lấy metadata video | ✅ **(Done)**  |
| PUT    | `/{id}/metadata` | Cập nhật metadata  | ⏳ **Planned** |

---

### 📋 14. System Log APIs

**Base Path**: `/api/admin/logs`

| Method | Endpoint      | Mô Tả                | Trạng Thái     |
| ------ | ------------- | -------------------- | -------------- |
| GET    | `/`           | Lấy system logs      | ⏳ **Planned** |
| GET    | `/activities` | Hoạt động người dùng | ⏳ **Planned** |
| GET    | `/export`     | Xuất logs            | ⏳ **Planned** |

---

## 🔧 Các APIs Cần Triển Khai Thêm

### 📊 15. Analytics & Reporting APIs

**Base Path**: `/api/analytics`

| Method | Endpoint           | Mô Tả                | Trạng Thái     |
| ------ | ------------------ | -------------------- | -------------- |
| GET    | `/dashboard`       | Dashboard thống kê   | ⏳ **Planned** |
| GET    | `/courses/popular` | Khóa học phổ biến    | ⏳ **Planned** |
| GET    | `/revenue/monthly` | Doanh thu theo tháng | ⏳ **Planned** |
| GET    | `/users/activity`  | Hoạt động người dùng | ⏳ **Planned** |

### � 16. Notification APIs

**Base Path**: `/api/notifications`

| Method | Endpoint     | Mô Tả               | Trạng Thái     |
| ------ | ------------ | ------------------- | -------------- |
| GET    | `/`          | Danh sách thông báo | ⏳ **Planned** |
| POST   | `/`          | Tạo thông báo       | ⏳ **Planned** |
| PUT    | `/{id}/read` | Đánh dấu đã đọc     | ⏳ **Planned** |
| DELETE | `/{id}`      | Xóa thông báo       | ⏳ **Planned** |

### 💬 17. Discussion Forum APIs

**Base Path**: `/api/discussions`

| Method | Endpoint              | Mô Tả              | Trạng Thái     |
| ------ | --------------------- | ------------------ | -------------- |
| GET    | `/courses/{courseId}` | Thảo luận khóa học | ⏳ **Planned** |
| POST   | `/courses/{courseId}` | Tạo bài thảo luận  | ⏳ **Planned** |
| POST   | `/{id}/replies`       | Trả lời thảo luận  | ⏳ **Planned** |
| PUT    | `/{id}/like`          | Like/Unlike        | ⏳ **Planned** |

### 🏆 18. Certificate APIs

**Base Path**: `/api/certificates`

| Method | Endpoint | Mô Tả             | Trạng Thái     |
| ------ | -------- | ----------------- | -------------- |
| GET    | `/`      | Chứng chỉ của tôi | ⏳ **Planned** |

---

## 🏗️ Kiến Trúc Hệ Thống

### Tech Stack

- **Framework**: Spring Boot 3.x
- **Database**: MySQL 8.0
- **ORM**: JPA/Hibernate
- **Migration**: Liquibase
- **Documentation**: Swagger/OpenAPI 3
- **Security**: Spring Security + JWT
- **Payment**: Stripe
- **File Storage**: Local/Cloud Storage

### Package Structure

```
src/main/java/project/ktc/springboot_app/
├── auth/                   # Authentication & Authorization
├── user/                   # User Management
├── course/                 # Course Management
├── category/               # Category Management
├── section/                # Section Management
├── lesson/                 # Lesson Management
├── quiz/                   # Quiz Management
├── payment/                # Payment Processing
├── refund/                 # Refund Management
├── review/                 # Review System
├── enrollment/             # Course Enrollment
├── instructor_application/ # Instructor Applications
├── earning/                # Instructor Earnings
├── upload/                 # File Upload
├── stripe/                 # Stripe Integration
├── log/                    # System Logging
├── common/                 # Common Utilities
├── config/                 # Configuration
├── security/               # Security Configuration
└── entity/                 # JPA Entities
```

=
