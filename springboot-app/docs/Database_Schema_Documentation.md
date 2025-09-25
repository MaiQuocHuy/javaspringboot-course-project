# Tài Liệu Mô Tả Cấu Trúc Cơ Sở Dữ Liệu - KTC Learning Platform

## Tổng Quan

Hệ thống cơ sở dữ liệu của KTC Learning Platform bao gồm các bảng chính quản lý người dùng, khóa học, thanh toán, quyền hạn và các tính năng mở rộng. Tất cả các bảng sử dụng UUID làm khóa chính và có timestamp audit tự động.

## Bảng Chính (Main Tables)

### 1. users - Bảng Người Dùng

Lưu trữ thông tin cơ bản của tất cả người dùng trong hệ thống.

| Tên Trường | Kiểu Dữ Liệu | Ràng Buộc                                     | Mô Tả                                  |
| ---------- | ------------ | --------------------------------------------- | -------------------------------------- |
| id         | VARCHAR(36)  | PK, NOT NULL                                  | Khóa chính định danh người dùng (UUID) |
| name       | VARCHAR(255) | NOT NULL                                      | Họ và tên đầy đủ của người dùng        |
| email      | VARCHAR(255) | UNIQUE, NOT NULL                              | Email đăng nhập duy nhất               |
| password   | VARCHAR(255) | NOT NULL                                      | Mật khẩu đã mã hóa (BCrypt)            |
| phone      | VARCHAR(20)  | NULL                                          | Số điện thoại liên lạc                 |
| avatar     | VARCHAR(255) | NULL                                          | URL hình đại diện người dùng           |
| is_active  | BOOLEAN      | DEFAULT TRUE, NOT NULL                        | Trạng thái kích hoạt tài khoản         |
| created_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP, NOT NULL           | Thời điểm tạo tài khoản                |
| updated_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP ON UPDATE, NOT NULL | Thời điểm cập nhật cuối                |

**Quan hệ:**

- Một-nhiều với `user_roles` (Một người dùng có nhiều vai trò)
- Một-nhiều với `courses` (Một instructor tạo nhiều khóa học)
- Một-nhiều với `enrollments` (Một học viên đăng ký nhiều khóa học)
- Một-nhiều với `payments` (Một người dùng có nhiều giao dịch thanh toán)

### 2. user_roles - Bảng Vai Trò Người Dùng

Định nghĩa các vai trò có thể có trong hệ thống và gán vai trò cho người dùng.

| Tên Trường  | Kiểu Dữ Liệu                         | Ràng Buộc                                     | Mô Tả                        |
| ----------- | ------------------------------------ | --------------------------------------------- | ---------------------------- |
| id          | VARCHAR(36)                          | PK, NOT NULL                                  | Khóa chính định danh vai trò |
| user_id     | VARCHAR(36)                          | FK, NOT NULL                                  | Tham chiếu đến bảng users    |
| role        | ENUM('STUDENT','INSTRUCTOR','ADMIN') | NOT NULL                                      | Loại vai trò của người dùng  |
| is_active   | BOOLEAN                              | DEFAULT TRUE, NOT NULL                        | Trạng thái kích hoạt vai trò |
| description | VARCHAR(500)                         | NULL                                          | Mô tả chi tiết về vai trò    |
| created_at  | TIMESTAMP                            | DEFAULT CURRENT_TIMESTAMP, NOT NULL           | Thời điểm gán vai trò        |
| updated_at  | TIMESTAMP                            | DEFAULT CURRENT_TIMESTAMP ON UPDATE, NOT NULL | Thời điểm cập nhật vai trò   |

**Quan hệ:**

- Nhiều-một với `users` (Nhiều vai trò thuộc về một người dùng)
- Một-nhiều với `role_permissions` (Một vai trò có nhiều quyền hạn)

### 3. categories - Bảng Danh Mục Khóa Học

Phân loại các khóa học theo chủ đề.

| Tên Trường  | Kiểu Dữ Liệu | Ràng Buộc                                     | Mô Tả                          |
| ----------- | ------------ | --------------------------------------------- | ------------------------------ |
| id          | VARCHAR(36)  | PK, NOT NULL                                  | Khóa chính định danh danh mục  |
| name        | VARCHAR(255) | UNIQUE, NOT NULL                              | Tên danh mục khóa học          |
| slug        | VARCHAR(255) | UNIQUE, NOT NULL                              | Đường dẫn thân thiện URL       |
| description | TEXT         | NULL                                          | Mô tả chi tiết về danh mục     |
| image_url   | VARCHAR(255) | NULL                                          | URL hình ảnh đại diện danh mục |
| is_active   | BOOLEAN      | DEFAULT TRUE, NOT NULL                        | Trạng thái hiển thị danh mục   |
| created_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP, NOT NULL           | Thời điểm tạo danh mục         |
| updated_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP ON UPDATE, NOT NULL | Thời điểm cập nhật danh mục    |

**Quan hệ:**

- Một-nhiều với `course_categories` (Một danh mục chứa nhiều khóa học)

### 4. courses - Bảng Khóa Học

Lưu trữ thông tin chi tiết về các khóa học.

| Tên Trường    | Kiểu Dữ Liệu                               | Ràng Buộc                                     | Mô Tả                                      |
| ------------- | ------------------------------------------ | --------------------------------------------- | ------------------------------------------ |
| id            | VARCHAR(36)                                | PK, NOT NULL                                  | Khóa chính định danh khóa học              |
| instructor_id | VARCHAR(36)                                | FK, NOT NULL                                  | Tham chiếu đến instructor trong bảng users |
| title         | VARCHAR(255)                               | NOT NULL                                      | Tiêu đề khóa học                           |
| slug          | VARCHAR(255)                               | UNIQUE, NOT NULL                              | Đường dẫn thân thiện URL                   |
| description   | TEXT                                       | NOT NULL                                      | Mô tả chi tiết khóa học                    |
| thumbnail_url | VARCHAR(255)                               | NULL                                          | URL hình thu nhỏ khóa học                  |
| price         | DECIMAL(10,2)                              | DEFAULT 0, NOT NULL                           | Giá khóa học (VND)                         |
| level         | ENUM('BEGINNER','INTERMEDIATE','ADVANCED') | DEFAULT 'BEGINNER', NOT NULL                  | Mức độ khó của khóa học                    |
| is_published  | BOOLEAN                                    | DEFAULT FALSE, NOT NULL                       | Trạng thái công khai khóa học              |
| is_approved   | BOOLEAN                                    | DEFAULT FALSE, NOT NULL                       | Trạng thái duyệt khóa học bởi admin        |
| is_free       | BOOLEAN                                    | DEFAULT FALSE, NOT NULL                       | Khóa học miễn phí hay trả phí              |
| created_at    | TIMESTAMP                                  | DEFAULT CURRENT_TIMESTAMP, NOT NULL           | Thời điểm tạo khóa học                     |
| updated_at    | TIMESTAMP                                  | DEFAULT CURRENT_TIMESTAMP ON UPDATE, NOT NULL | Thời điểm cập nhật khóa học                |

**Quan hệ:**

- Nhiều-một với `users` (Nhiều khóa học thuộc về một instructor)
- Một-nhiều với `course_categories` (Một khóa học thuộc nhiều danh mục)
- Một-nhiều với `sections` (Một khóa học có nhiều chương)
- Một-nhiều với `enrollments` (Một khóa học có nhiều học viên đăng ký)
- Một-nhiều với `reviews` (Một khóa học có nhiều đánh giá)

### 5. course_categories - Bảng Liên Kết Khóa Học - Danh Mục

Quan hệ nhiều-nhiều giữa khóa học và danh mục.

| Tên Trường  | Kiểu Dữ Liệu | Ràng Buộc                           | Mô Tả                            |
| ----------- | ------------ | ----------------------------------- | -------------------------------- |
| id          | VARCHAR(36)  | PK, NOT NULL                        | Khóa chính định danh mối quan hệ |
| course_id   | VARCHAR(36)  | FK, NOT NULL                        | Tham chiếu đến bảng courses      |
| category_id | VARCHAR(36)  | FK, NOT NULL                        | Tham chiếu đến bảng categories   |
| created_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP, NOT NULL | Thời điểm liên kết               |

**Quan hệ:**

- Nhiều-một với `courses` (Nhiều liên kết thuộc về một khóa học)
- Nhiều-một với `categories` (Nhiều liên kết thuộc về một danh mục)

### 6. sections - Bảng Chương Học

Phân chia khóa học thành các chương có thứ tự.

| Tên Trường  | Kiểu Dữ Liệu | Ràng Buộc                                     | Mô Tả                            |
| ----------- | ------------ | --------------------------------------------- | -------------------------------- |
| id          | VARCHAR(36)  | PK, NOT NULL                                  | Khóa chính định danh chương      |
| course_id   | VARCHAR(36)  | FK, NOT NULL                                  | Tham chiếu đến bảng courses      |
| title       | VARCHAR(255) | NOT NULL                                      | Tiêu đề chương học               |
| description | TEXT         | NULL                                          | Mô tả chi tiết chương            |
| order_index | INT          | NOT NULL                                      | Thứ tự hiển thị chương (0-based) |
| created_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP, NOT NULL           | Thời điểm tạo chương             |
| updated_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP ON UPDATE, NOT NULL | Thời điểm cập nhật chương        |

**Quan hệ:**

- Nhiều-một với `courses` (Nhiều chương thuộc về một khóa học)
- Một-nhiều với `lessons` (Một chương có nhiều bài học)

### 7. lesson_types - Bảng Loại Bài Học

Định nghĩa các loại bài học có thể có.

| Tên Trường  | Kiểu Dữ Liệu | Ràng Buộc              | Mô Tả                                |
| ----------- | ------------ | ---------------------- | ------------------------------------ |
| id          | VARCHAR(36)  | PK, NOT NULL           | Khóa chính định danh loại bài học    |
| name        | VARCHAR(50)  | UNIQUE, NOT NULL       | Tên loại bài học (VIDEO, QUIZ, TEXT) |
| description | VARCHAR(255) | NULL                   | Mô tả chi tiết loại bài học          |
| is_active   | BOOLEAN      | DEFAULT TRUE, NOT NULL | Trạng thái kích hoạt loại            |

### 8. lessons - Bảng Bài Học

Lưu trữ thông tin chi tiết về từng bài học.

| Tên Trường     | Kiểu Dữ Liệu | Ràng Buộc                                     | Mô Tả                                    |
| -------------- | ------------ | --------------------------------------------- | ---------------------------------------- |
| id             | VARCHAR(36)  | PK, NOT NULL                                  | Khóa chính định danh bài học             |
| section_id     | VARCHAR(36)  | FK, NOT NULL                                  | Tham chiếu đến bảng sections             |
| lesson_type_id | VARCHAR(36)  | FK, NOT NULL                                  | Tham chiếu đến bảng lesson_types         |
| title          | VARCHAR(255) | NOT NULL                                      | Tiêu đề bài học                          |
| description    | TEXT         | NULL                                          | Mô tả chi tiết bài học                   |
| content_id     | VARCHAR(36)  | NULL                                          | ID nội dung (video_contents, quiz, etc.) |
| order_index    | INT          | NOT NULL                                      | Thứ tự hiển thị bài học (0-based)        |
| is_free        | BOOLEAN      | DEFAULT FALSE, NOT NULL                       | Bài học miễn phí hay trả phí             |
| created_at     | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP, NOT NULL           | Thời điểm tạo bài học                    |
| updated_at     | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP ON UPDATE, NOT NULL | Thời điểm cập nhật bài học               |

**Quan hệ:**

- Nhiều-một với `sections` (Nhiều bài học thuộc về một chương)
- Nhiều-một với `lesson_types` (Nhiều bài học cùng một loại)
- Một-nhiều với `lesson_completions` (Một bài học có nhiều lần hoàn thành)

### 9. video_contents - Bảng Nội Dung Video

Lưu trữ thông tin về video bài học.

| Tên Trường  | Kiểu Dữ Liệu | Ràng Buộc                                     | Mô Tả                                  |
| ----------- | ------------ | --------------------------------------------- | -------------------------------------- |
| id          | VARCHAR(36)  | PK, NOT NULL                                  | Khóa chính định danh video             |
| title       | VARCHAR(255) | NOT NULL                                      | Tiêu đề video                          |
| description | TEXT         | NULL                                          | Mô tả nội dung video                   |
| url         | VARCHAR(500) | NOT NULL                                      | URL truy cập video (Cloudinary/AWS S3) |
| duration    | INT          | NULL                                          | Thời lượng video (giây)                |
| file_size   | BIGINT       | NULL                                          | Dung lượng file video (bytes)          |
| resolution  | VARCHAR(20)  | NULL                                          | Độ phân giải video (720p, 1080p)       |
| created_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP, NOT NULL           | Thời điểm upload video                 |
| updated_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP ON UPDATE, NOT NULL | Thời điểm cập nhật video               |

### 10. enrollments - Bảng Đăng Ký Khóa Học

Quản lý việc đăng ký khóa học của học viên.

| Tên Trường          | Kiểu Dữ Liệu                                | Ràng Buộc                           | Mô Tả                                    |
| ------------------- | ------------------------------------------- | ----------------------------------- | ---------------------------------------- |
| id                  | VARCHAR(36)                                 | PK, NOT NULL                        | Khóa chính định danh đăng ký             |
| user_id             | VARCHAR(36)                                 | FK, NOT NULL                        | Tham chiếu đến học viên trong bảng users |
| course_id           | VARCHAR(36)                                 | FK, NOT NULL                        | Tham chiếu đến bảng courses              |
| status              | ENUM('IN_PROGRESS','COMPLETED','SUSPENDED') | DEFAULT 'IN_PROGRESS', NOT NULL     | Trạng thái học tập                       |
| progress_percentage | DECIMAL(5,2)                                | DEFAULT 0, NOT NULL                 | Phần trăm hoàn thành (0-100)             |
| enrolled_at         | TIMESTAMP                                   | DEFAULT CURRENT_TIMESTAMP, NOT NULL | Thời điểm đăng ký                        |
| completed_at        | TIMESTAMP                                   | NULL                                | Thời điểm hoàn thành khóa học            |

**Quan hệ:**

- Nhiều-một với `users` (Nhiều đăng ký thuộc về một học viên)
- Nhiều-một với `courses` (Nhiều đăng ký thuộc về một khóa học)
- Một-nhiều với `lesson_completions` (Một đăng ký có nhiều bài học hoàn thành)

### 11. lesson_completions - Bảng Hoàn Thành Bài Học

Theo dõi tiến độ hoàn thành bài học của từng học viên.

| Tên Trường   | Kiểu Dữ Liệu | Ràng Buộc                           | Mô Tả                                    |
| ------------ | ------------ | ----------------------------------- | ---------------------------------------- |
| id           | VARCHAR(36)  | PK, NOT NULL                        | Khóa chính định danh hoàn thành          |
| user_id      | VARCHAR(36)  | FK, NOT NULL                        | Tham chiếu đến học viên trong bảng users |
| lesson_id    | VARCHAR(36)  | FK, NOT NULL                        | Tham chiếu đến bảng lessons              |
| completed_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP, NOT NULL | Thời điểm hoàn thành bài học             |

**Quan hệ:**

- Nhiều-một với `users` (Nhiều hoàn thành thuộc về một học viên)
- Nhiều-một với `lessons` (Nhiều hoàn thành thuộc về một bài học)

### 12. reviews - Bảng Đánh Giá Khóa Học

Lưu trữ đánh giá và nhận xét về khóa học từ học viên.

| Tên Trường  | Kiểu Dữ Liệu | Ràng Buộc                                     | Mô Tả                                    |
| ----------- | ------------ | --------------------------------------------- | ---------------------------------------- |
| id          | VARCHAR(36)  | PK, NOT NULL                                  | Khóa chính định danh đánh giá            |
| user_id     | VARCHAR(36)  | FK, NOT NULL                                  | Tham chiếu đến học viên trong bảng users |
| course_id   | VARCHAR(36)  | FK, NOT NULL                                  | Tham chiếu đến bảng courses              |
| rating      | INT          | CHECK (rating BETWEEN 1 AND 5), NOT NULL      | Điểm đánh giá (1-5 sao)                  |
| review_text | TEXT         | NULL                                          | Nội dung nhận xét chi tiết               |
| is_public   | BOOLEAN      | DEFAULT TRUE, NOT NULL                        | Hiển thị công khai hay riêng tư          |
| created_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP, NOT NULL           | Thời điểm tạo đánh giá                   |
| updated_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP ON UPDATE, NOT NULL | Thời điểm cập nhật đánh giá              |

**Quan hệ:**

- Nhiều-một với `users` (Nhiều đánh giá thuộc về một học viên)
- Nhiều-một với `courses` (Nhiều đánh giá thuộc về một khóa học)

### 13. payments - Bảng Thanh Toán

Quản lý các giao dịch thanh toán cho khóa học.

| Tên Trường        | Kiểu Dữ Liệu                                    | Ràng Buộc                                     | Mô Tả                                     |
| ----------------- | ----------------------------------------------- | --------------------------------------------- | ----------------------------------------- |
| id                | VARCHAR(36)                                     | PK, NOT NULL                                  | Khóa chính định danh thanh toán           |
| user_id           | VARCHAR(36)                                     | FK, NOT NULL                                  | Tham chiếu đến người mua trong bảng users |
| course_id         | VARCHAR(36)                                     | FK, NOT NULL                                  | Tham chiếu đến bảng courses               |
| amount            | DECIMAL(10,2)                                   | NOT NULL                                      | Số tiền thanh toán (VND)                  |
| currency          | VARCHAR(3)                                      | DEFAULT 'VND', NOT NULL                       | Loại tiền tệ                              |
| payment_method    | ENUM('STRIPE','PAYPAL','BANK_TRANSFER','MOMO')  | NOT NULL                                      | Phương thức thanh toán                    |
| transaction_id    | VARCHAR(255)                                    | NULL                                          | ID giao dịch từ cổng thanh toán           |
| status            | ENUM('PENDING','COMPLETED','FAILED','REFUNDED') | DEFAULT 'PENDING', NOT NULL                   | Trạng thái thanh toán                     |
| stripe_session_id | VARCHAR(255)                                    | NULL                                          | Session ID từ Stripe                      |
| paid_out_at       | TIMESTAMP                                       | NULL                                          | Thời điểm chi trả cho instructor          |
| created_at        | TIMESTAMP                                       | DEFAULT CURRENT_TIMESTAMP, NOT NULL           | Thời điểm tạo giao dịch                   |
| updated_at        | TIMESTAMP                                       | DEFAULT CURRENT_TIMESTAMP ON UPDATE, NOT NULL | Thời điểm cập nhật giao dịch              |

**Quan hệ:**

- Nhiều-một với `users` (Nhiều thanh toán thuộc về một người dùng)
- Nhiều-một với `courses` (Nhiều thanh toán thuộc về một khóa học)
- Một-nhiều với `refunds` (Một thanh toán có thể có nhiều refund)
- Một-nhiều với `instructor_earnings` (Một thanh toán tạo ra thu nhập cho instructor)

### 14. refunds - Bảng Hoàn Tiền

Quản lý các yêu cầu hoàn tiền.

| Tên Trường      | Kiểu Dữ Liệu                         | Ràng Buộc                                     | Mô Tả                          |
| --------------- | ------------------------------------ | --------------------------------------------- | ------------------------------ |
| id              | VARCHAR(36)                          | PK, NOT NULL                                  | Khóa chính định danh hoàn tiền |
| payment_id      | VARCHAR(36)                          | FK, NOT NULL                                  | Tham chiếu đến bảng payments   |
| amount          | DECIMAL(10,2)                        | NOT NULL                                      | Số tiền hoàn lại               |
| reason          | TEXT                                 | NOT NULL                                      | Lý do yêu cầu hoàn tiền        |
| status          | ENUM('PENDING','COMPLETED','FAILED') | DEFAULT 'PENDING', NOT NULL                   | Trạng thái hoàn tiền           |
| rejected_reason | TEXT                                 | NULL                                          | Lý do từ chối hoàn tiền        |
| processed_by    | VARCHAR(36)                          | FK, NULL                                      | Admin xử lý yêu cầu            |
| created_at      | TIMESTAMP                            | DEFAULT CURRENT_TIMESTAMP, NOT NULL           | Thời điểm yêu cầu hoàn tiền    |
| updated_at      | TIMESTAMP                            | DEFAULT CURRENT_TIMESTAMP ON UPDATE, NOT NULL | Thời điểm cập nhật trạng thái  |

**Quan hệ:**

- Nhiều-một với `payments` (Nhiều hoàn tiền thuộc về một thanh toán)
- Nhiều-một với `users` qua processed_by (Admin xử lý)

### 15. instructor_earnings - Bảng Thu Nhập Instructor

Theo dõi thu nhập của các instructor từ việc bán khóa học.

| Tên Trường       | Kiểu Dữ Liệu                       | Ràng Buộc                           | Mô Tả                                      |
| ---------------- | ---------------------------------- | ----------------------------------- | ------------------------------------------ |
| id               | VARCHAR(36)                        | PK, NOT NULL                        | Khóa chính định danh thu nhập              |
| instructor_id    | VARCHAR(36)                        | FK, NOT NULL                        | Tham chiếu đến instructor trong bảng users |
| payment_id       | VARCHAR(36)                        | FK, NOT NULL                        | Tham chiếu đến bảng payments               |
| amount           | DECIMAL(10,2)                      | NOT NULL                            | Số tiền thu nhập thực tế                   |
| platform_cut     | DECIMAL(10,2)                      | NOT NULL                            | Phần trăm hoa hồng nền tảng                |
| instructor_share | DECIMAL(10,2)                      | NOT NULL                            | Phần thu nhập của instructor               |
| status           | ENUM('PENDING','AVAILABLE','PAID') | DEFAULT 'PENDING', NOT NULL         | Trạng thái thu nhập                        |
| paid_at          | TIMESTAMP                          | NULL                                | Thời điểm chi trả cho instructor           |
| created_at       | TIMESTAMP                          | DEFAULT CURRENT_TIMESTAMP, NOT NULL | Thời điểm tạo bản ghi thu nhập             |

**Quan hệ:**

- Nhiều-một với `users` qua instructor_id (Nhiều thu nhập thuộc về một instructor)
- Nhiều-một với `payments` (Nhiều thu nhập từ một thanh toán)

### 16. instructor_applications - Bảng Đăng Ký Làm Instructor

Quản lý việc đăng ký trở thành instructor.

| Tên Trường   | Kiểu Dữ Liệu                          | Ràng Buộc                           | Mô Tả                                           |
| ------------ | ------------------------------------- | ----------------------------------- | ----------------------------------------------- |
| id           | VARCHAR(36)                           | PK, NOT NULL                        | Khóa chính định danh đơn đăng ký                |
| user_id      | VARCHAR(36)                           | FK, NOT NULL                        | Tham chiếu đến người dùng trong bảng users      |
| documents    | JSON                                  | NOT NULL                            | Thông tin tài liệu (certificate, CV, portfolio) |
| status       | ENUM('PENDING','APPROVED','REJECTED') | DEFAULT 'PENDING', NOT NULL         | Trạng thái đơn đăng ký                          |
| reason       | TEXT                                  | NULL                                | Lý do phê duyệt/từ chối                         |
| reviewed_by  | VARCHAR(36)                           | FK, NULL                            | Admin xem xét đơn                               |
| is_deleted   | BOOLEAN                               | DEFAULT FALSE, NOT NULL             | Đơn đã bị xóa hay chưa                          |
| submitted_at | TIMESTAMP                             | DEFAULT CURRENT_TIMESTAMP, NOT NULL | Thời điểm nộp đơn                               |
| reviewed_at  | TIMESTAMP                             | NULL                                | Thời điểm xem xét đơn                           |

**Quan hệ:**

- Nhiều-một với `users` qua user_id (Người nộp đơn)
- Nhiều-một với `users` qua reviewed_by (Admin xem xét)

### 17. refresh_tokens - Bảng Token Làm Mới

Quản lý JWT refresh tokens cho authentication.

| Tên Trường | Kiểu Dữ Liệu | Ràng Buộc                           | Mô Tả                        |
| ---------- | ------------ | ----------------------------------- | ---------------------------- |
| id         | VARCHAR(36)  | PK, NOT NULL                        | Khóa chính định danh token   |
| user_id    | VARCHAR(36)  | FK, NOT NULL                        | Tham chiếu đến bảng users    |
| token      | VARCHAR(500) | UNIQUE, NOT NULL                    | Refresh token string         |
| expires_at | TIMESTAMP    | NOT NULL                            | Thời điểm hết hạn token      |
| is_revoked | BOOLEAN      | DEFAULT FALSE, NOT NULL             | Token đã bị thu hồi hay chưa |
| created_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP, NOT NULL | Thời điểm tạo token          |

**Quan hệ:**

- Nhiều-một với `users` (Nhiều token thuộc về một người dùng)

### 18. quiz_questions - Bảng Câu Hỏi Quiz

Lưu trữ câu hỏi và đáp án cho các bài quiz.

| Tên Trường     | Kiểu Dữ Liệu | Ràng Buộc                                             | Mô Tả                                          |
| -------------- | ------------ | ----------------------------------------------------- | ---------------------------------------------- |
| id             | VARCHAR(36)  | PK, NOT NULL                                          | Khóa chính định danh câu hỏi                   |
| lesson_id      | VARCHAR(36)  | FK, NOT NULL                                          | Tham chiếu đến bài học quiz trong bảng lessons |
| question_text  | TEXT         | NOT NULL                                              | Nội dung câu hỏi                               |
| options        | JSON         | NOT NULL                                              | Các lựa chọn đáp án (A, B, C, D)               |
| correct_answer | VARCHAR(1)   | CHECK (correct_answer IN ('A','B','C','D')), NOT NULL | Đáp án đúng                                    |
| explanation    | TEXT         | NULL                                                  | Giải thích chi tiết đáp án                     |
| created_at     | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP, NOT NULL                   | Thời điểm tạo câu hỏi                          |
| updated_at     | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP ON UPDATE, NOT NULL         | Thời điểm cập nhật câu hỏi                     |

**Quan hệ:**

- Nhiều-một với `lessons` (Nhiều câu hỏi thuộc về một bài quiz)
- Một-nhiều với `quiz_results` (Một câu hỏi có nhiều kết quả làm bài)

### 19. quiz_results - Bảng Kết Quả Quiz

Lưu trữ kết quả làm quiz của học viên.

| Tên Trường   | Kiểu Dữ Liệu | Ràng Buộc                           | Mô Tả                                      |
| ------------ | ------------ | ----------------------------------- | ------------------------------------------ |
| id           | VARCHAR(36)  | PK, NOT NULL                        | Khóa chính định danh kết quả               |
| user_id      | VARCHAR(36)  | FK, NOT NULL                        | Tham chiếu đến học viên trong bảng users   |
| lesson_id    | VARCHAR(36)  | FK, NOT NULL                        | Tham chiếu đến bài quiz trong bảng lessons |
| answers      | JSON         | NOT NULL                            | Đáp án đã chọn cho từng câu hỏi            |
| score        | DECIMAL(5,2) | NOT NULL                            | Điểm số đạt được (0-100)                   |
| submitted_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP, NOT NULL | Thời điểm nộp bài quiz                     |

**Quan hệ:**

- Nhiều-một với `users` (Nhiều kết quả thuộc về một học viên)
- Nhiều-một với `lessons` (Nhiều kết quả thuộc về một bài quiz)

### 20. system_logs - Bảng Nhật Ký Hệ Thống

Ghi lại các hoạt động quan trọng trong hệ thống để audit và debug.

| Tên Trường  | Kiểu Dữ Liệu                                      | Ràng Buộc                           | Mô Tả                                              |
| ----------- | ------------------------------------------------- | ----------------------------------- | -------------------------------------------------- |
| id          | VARCHAR(36)                                       | PK, NOT NULL                        | Khóa chính định danh log                           |
| user_id     | VARCHAR(36)                                       | FK, NOT NULL                        | Tham chiếu đến người thực hiện hành động           |
| action      | ENUM('CREATE','UPDATE','DELETE','LOGIN','LOGOUT') | NOT NULL                            | Loại hành động thực hiện                           |
| entity_type | VARCHAR(100)                                      | NOT NULL                            | Loại đối tượng bị tác động (COURSE, USER, PAYMENT) |
| entity_id   | VARCHAR(36)                                       | NULL                                | ID của đối tượng bị tác động                       |
| old_values  | JSON                                              | NULL                                | Giá trị cũ trước khi thay đổi                      |
| new_values  | JSON                                              | NULL                                | Giá trị mới sau khi thay đổi                       |
| ip_address  | VARCHAR(45)                                       | NULL                                | Địa chỉ IP thực hiện hành động                     |
| user_agent  | TEXT                                              | NULL                                | Thông tin trình duyệt/thiết bị                     |
| created_at  | TIMESTAMP                                         | DEFAULT CURRENT_TIMESTAMP, NOT NULL | Thời điểm thực hiện hành động                      |

**Quan hệ:**

- Nhiều-một với `users` (Nhiều log thuộc về một người dùng)

## Hệ Thống Phân Quyền (Permission System)

### 21. filter_types - Bảng Loại Bộ Lọc

Định nghĩa các loại bộ lọc cho hệ thống phân quyền.

| Tên Trường  | Kiểu Dữ Liệu | Ràng Buộc              | Mô Tả                            |
| ----------- | ------------ | ---------------------- | -------------------------------- |
| id          | VARCHAR(20)  | PK, NOT NULL           | Khóa chính định danh loại bộ lọc |
| name        | VARCHAR(100) | NOT NULL               | Tên loại bộ lọc                  |
| description | TEXT         | NULL                   | Mô tả chi tiết về loại bộ lọc    |
| is_active   | BOOLEAN      | DEFAULT TRUE, NOT NULL | Trạng thái kích hoạt             |

### 22. resources - Bảng Tài Nguyên

Định nghĩa các tài nguyên trong hệ thống cần được bảo vệ.

| Tên Trường         | Kiểu Dữ Liệu | Ràng Buộc                                     | Mô Tả                                             |
| ------------------ | ------------ | --------------------------------------------- | ------------------------------------------------- |
| id                 | VARCHAR(36)  | PK, NOT NULL                                  | Khóa chính định danh tài nguyên                   |
| key                | VARCHAR(100) | UNIQUE, NOT NULL                              | Khóa định danh tài nguyên (course, user, payment) |
| name               | VARCHAR(255) | NOT NULL                                      | Tên hiển thị tài nguyên                           |
| description        | TEXT         | NULL                                          | Mô tả chi tiết tài nguyên                         |
| parent_resource_id | VARCHAR(36)  | FK, NULL                                      | Tham chiếu đến tài nguyên cha (cấu trúc cây)      |
| is_active          | BOOLEAN      | DEFAULT TRUE, NOT NULL                        | Trạng thái kích hoạt                              |
| created_at         | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP, NOT NULL           | Thời điểm tạo tài nguyên                          |
| updated_at         | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP ON UPDATE, NOT NULL | Thời điểm cập nhật tài nguyên                     |

**Quan hệ:**

- Tự tham chiếu với `parent_resource_id` (Cấu trúc cây tài nguyên)
- Một-nhiều với `permissions` (Một tài nguyên có nhiều quyền hạn)

### 23. actions - Bảng Hành Động

Định nghĩa các hành động có thể thực hiện trên tài nguyên.

| Tên Trường  | Kiểu Dữ Liệu                                      | Ràng Buộc                                     | Mô Tả                                                   |
| ----------- | ------------------------------------------------- | --------------------------------------------- | ------------------------------------------------------- |
| id          | VARCHAR(36)                                       | PK, NOT NULL                                  | Khóa chính định danh hành động                          |
| key         | VARCHAR(100)                                      | UNIQUE, NOT NULL                              | Khóa định danh hành động (create, read, update, delete) |
| name        | VARCHAR(255)                                      | NOT NULL                                      | Tên hiển thị hành động                                  |
| description | TEXT                                              | NULL                                          | Mô tả chi tiết hành động                                |
| action_type | ENUM('CREATE','READ','UPDATE','DELETE','EXECUTE') | NOT NULL                                      | Phân loại hành động                                     |
| is_active   | BOOLEAN                                           | DEFAULT TRUE, NOT NULL                        | Trạng thái kích hoạt                                    |
| created_at  | TIMESTAMP                                         | DEFAULT CURRENT_TIMESTAMP, NOT NULL           | Thời điểm tạo hành động                                 |
| updated_at  | TIMESTAMP                                         | DEFAULT CURRENT_TIMESTAMP ON UPDATE, NOT NULL | Thời điểm cập nhật hành động                            |

**Quan hệ:**

- Một-nhiều với `permissions` (Một hành động có nhiều quyền hạn)

### 24. permissions - Bảng Quyền Hạn

Kết hợp tài nguyên và hành động thành các quyền hạn cụ thể.

| Tên Trường     | Kiểu Dữ Liệu | Ràng Buộc                                     | Mô Tả                                     |
| -------------- | ------------ | --------------------------------------------- | ----------------------------------------- |
| id             | VARCHAR(36)  | PK, NOT NULL                                  | Khóa chính định danh quyền hạn            |
| resource_id    | VARCHAR(36)  | FK, NOT NULL                                  | Tham chiếu đến bảng resources             |
| action_id      | VARCHAR(36)  | FK, NOT NULL                                  | Tham chiếu đến bảng actions               |
| permission_key | VARCHAR(200) | UNIQUE, NOT NULL                              | Khóa quyền hạn (course:create, user:read) |
| description    | TEXT         | NULL                                          | Mô tả chi tiết quyền hạn                  |
| is_active      | BOOLEAN      | DEFAULT TRUE, NOT NULL                        | Trạng thái kích hoạt                      |
| created_at     | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP, NOT NULL           | Thời điểm tạo quyền hạn                   |
| updated_at     | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP ON UPDATE, NOT NULL | Thời điểm cập nhật quyền hạn              |

**Quan hệ:**

- Nhiều-một với `resources` (Nhiều quyền hạn thuộc về một tài nguyên)
- Nhiều-một với `actions` (Nhiều quyền hạn thuộc về một hành động)
- Một-nhiều với `role_permissions` (Một quyền hạn được gán cho nhiều vai trò)

### 25. role_permissions - Bảng Phân Quyền Vai Trò

Gán quyền hạn cụ thể cho từng vai trò.

| Tên Trường     | Kiểu Dữ Liệu | Ràng Buộc                                     | Mô Tả                            |
| -------------- | ------------ | --------------------------------------------- | -------------------------------- |
| id             | VARCHAR(36)  | PK, NOT NULL                                  | Khóa chính định danh phân quyền  |
| role_id        | VARCHAR(36)  | FK, NOT NULL                                  | Tham chiếu đến bảng user_roles   |
| permission_id  | VARCHAR(36)  | FK, NOT NULL                                  | Tham chiếu đến bảng permissions  |
| filter_type_id | VARCHAR(20)  | FK, NOT NULL                                  | Tham chiếu đến bảng filter_types |
| granted_by     | VARCHAR(36)  | FK, NULL                                      | Admin cấp quyền                  |
| is_active      | BOOLEAN      | DEFAULT TRUE, NOT NULL                        | Trạng thái kích hoạt phân quyền  |
| created_at     | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP, NOT NULL           | Thời điểm cấp quyền              |
| updated_at     | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP ON UPDATE, NOT NULL | Thời điểm cập nhật quyền         |

**Quan hệ:**

- Nhiều-một với `user_roles` (Nhiều phân quyền thuộc về một vai trò)
- Nhiều-một với `permissions` (Nhiều phân quyền thuộc về một quyền hạn)
- Nhiều-một với `filter_types` (Nhiều phân quyền thuộc về một loại bộ lọc)
- Nhiều-một với `users` qua granted_by (Admin cấp quyền)

## Các Bảng Mở Rộng (Extended Tables)

### 26. notifications - Bảng Thông Báo

Quản lý hệ thống thông báo cho người dùng.

| Tên Trường  | Kiểu Dữ Liệu                | Ràng Buộc                                     | Mô Tả                                          |
| ----------- | --------------------------- | --------------------------------------------- | ---------------------------------------------- |
| id          | VARCHAR(36)                 | PK, NOT NULL                                  | Khóa chính định danh thông báo                 |
| user_id     | VARCHAR(36)                 | FK, NOT NULL                                  | Tham chiếu đến người nhận trong bảng users     |
| resource_id | VARCHAR(36)                 | FK, NOT NULL                                  | Tham chiếu đến bảng resources                  |
| entity_id   | VARCHAR(36)                 | NOT NULL                                      | ID đối tượng liên quan (course_id, payment_id) |
| message     | TEXT                        | NOT NULL                                      | Nội dung thông báo                             |
| action_url  | VARCHAR(255)                | NOT NULL                                      | URL hành động khi click thông báo              |
| priority    | ENUM('LOW','MEDIUM','HIGH') | DEFAULT 'LOW', NOT NULL                       | Mức độ ưu tiên thông báo                       |
| is_read     | BOOLEAN                     | DEFAULT FALSE, NOT NULL                       | Trạng thái đã đọc                              |
| created_at  | TIMESTAMP                   | DEFAULT CURRENT_TIMESTAMP, NOT NULL           | Thời điểm tạo thông báo                        |
| read_at     | TIMESTAMP                   | NULL                                          | Thời điểm đọc thông báo                        |
| updated_at  | TIMESTAMP                   | DEFAULT CURRENT_TIMESTAMP ON UPDATE, NOT NULL | Thời điểm cập nhật thông báo                   |
| expired_at  | TIMESTAMP                   | NULL                                          | Thời điểm hết hạn thông báo                    |

**Quan hệ:**

- Nhiều-một với `users` (Nhiều thông báo thuộc về một người dùng)
- Nhiều-một với `resources` (Nhiều thông báo thuộc về một tài nguyên)

### 27. discounts - Bảng Mã Giảm Giá

Quản lý các mã giảm giá và chương trình khuyến mãi.

| Tên Trường       | Kiểu Dữ Liệu | Ràng Buộc                           | Mô Tả                                |
| ---------------- | ------------ | ----------------------------------- | ------------------------------------ |
| id               | VARCHAR(36)  | PK, NOT NULL                        | Khóa chính định danh mã giảm giá     |
| code             | VARCHAR(50)  | UNIQUE, NOT NULL                    | Mã giảm giá duy nhất                 |
| discount_percent | DECIMAL(4,2) | NOT NULL                            | Phần trăm giảm giá (0-100)           |
| description      | VARCHAR(255) | NULL                                | Mô tả chi tiết mã giảm giá           |
| type             | VARCHAR(50)  | NOT NULL                            | Loại mã giảm giá (GENERAL, REFERRAL) |
| owner_user_id    | VARCHAR(36)  | FK, NULL                            | Chủ sở hữu mã (cho REFERRAL)         |
| start_date       | TIMESTAMP    | NULL                                | Thời điểm bắt đầu có hiệu lực        |
| end_date         | TIMESTAMP    | NULL                                | Thời điểm hết hiệu lực               |
| usage_limit      | INT          | NULL                                | Giới hạn số lần sử dụng              |
| per_user_limit   | INT          | NULL                                | Giới hạn sử dụng mỗi người           |
| is_active        | BOOLEAN      | DEFAULT TRUE, NOT NULL              | Trạng thái kích hoạt                 |
| created_at       | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP, NOT NULL | Thời điểm tạo mã                     |
| updated_at       | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP, NOT NULL | Thời điểm cập nhật mã                |

**Quan hệ:**

- Nhiều-một với `users` qua owner_user_id (Chủ sở hữu mã referral)
- Một-nhiều với `discount_usages` (Một mã có nhiều lần sử dụng)

### 28. discount_usages - Bảng Sử Dụng Mã Giảm Giá

Theo dõi việc sử dụng mã giảm giá.

| Tên Trường          | Kiểu Dữ Liệu  | Ràng Buộc                           | Mô Tả                                 |
| ------------------- | ------------- | ----------------------------------- | ------------------------------------- |
| id                  | VARCHAR(36)   | PK, NOT NULL                        | Khóa chính định danh lần sử dụng      |
| discount_id         | VARCHAR(36)   | FK, NOT NULL                        | Tham chiếu đến bảng discounts         |
| user_id             | VARCHAR(36)   | FK, NOT NULL                        | Tham chiếu đến người dùng mã          |
| course_id           | VARCHAR(36)   | FK, NOT NULL                        | Tham chiếu đến khóa học được giảm giá |
| referred_by_user_id | VARCHAR(36)   | FK, NULL                            | Người giới thiệu (cho REFERRAL)       |
| used_at             | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP, NOT NULL | Thời điểm sử dụng mã                  |
| discount_percent    | DECIMAL(4,2)  | NOT NULL                            | Phần trăm giảm giá thực tế            |
| discount_amount     | DECIMAL(10,2) | NOT NULL                            | Số tiền được giảm                     |

**Quan hệ:**

- Nhiều-một với `discounts` (Nhiều lần sử dụng thuộc về một mã)
- Nhiều-một với `users` qua user_id (Người sử dụng mã)
- Nhiều-một với `courses` (Khóa học được áp dụng giảm giá)
- Nhiều-một với `users` qua referred_by_user_id (Người giới thiệu)

## Ràng Buộc và Chỉ Mục (Constraints & Indexes)

### Ràng Buộc Unique

- `users.email` - Email duy nhất
- `categories.name, categories.slug` - Tên và slug danh mục duy nhất
- `courses.slug` - Slug khóa học duy nhất
- `discounts.code` - Mã giảm giá duy nhất
- `permissions.resource_id, permissions.action_id` - Kết hợp tài nguyên-hành động duy nhất
- `role_permissions.role_id, role_permissions.permission_id` - Kết hợp vai trò-quyền hạn duy nhất

### Chỉ Mục Hiệu Suất

- `idx_courses_instructor_published` - Tìm khóa học theo instructor và trạng thái published
- `idx_enrollments_user_course` - Tìm đăng ký theo user và course
- `idx_payments_user_status` - Tìm thanh toán theo user và trạng thái
- `idx_notifications_user_read_created` - Tìm thông báo theo user, trạng thái đọc và thời gian
- `idx_permissions_key` - Tìm quyền hạn theo key
- `idx_discount_usage_user_discount` - Tìm sử dụng mã giảm giá

### Cascade Relationships

- `notifications.user_id` → `users.id` (CASCADE) - Xóa user thì xóa thông báo
- `notifications.resource_id` → `resources.id` (CASCADE) - Xóa resource thì xóa thông báo
- `course_categories.course_id` → `courses.id` (CASCADE) - Xóa course thì xóa liên kết danh mục
- `sections.course_id` → `courses.id` (CASCADE) - Xóa course thì xóa sections
- `lessons.section_id` → `sections.id` (CASCADE) - Xóa section thì xóa lessons

## Ghi Chú Kỹ Thuật

1. **UUID Primary Keys**: Tất cả bảng sử dụng UUID (VARCHAR(36)) làm khóa chính để tránh enumeration attacks và hỗ trợ distributed systems.

2. **Timestamp Auditing**: Các bảng chính đều có `created_at` và `updated_at` với auto-update để tracking changes.

3. **Soft Delete Pattern**: Một số bảng như `instructor_applications` có trường `is_deleted` thay vì xóa thật.

4. **JSON Storage**: Sử dụng JSON columns cho data phức tạp như `instructor_applications.documents`, `quiz_questions.options`, `quiz_results.answers`.

5. **Enum Constraints**: Sử dụng ENUM để giới hạn giá trị hợp lệ cho các trường như status, role, priority.

6. **Indexing Strategy**: Tạo indexes composite cho các query patterns phổ biến (user + status + time).

7. **Foreign Key Constraints**: Đảm bảo referential integrity với appropriate cascade rules.

8. **Performance Considerations**: Partition lớn tables như `system_logs`, `notifications` theo thời gian nếu cần thiết.
