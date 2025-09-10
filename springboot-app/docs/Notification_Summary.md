# Tổng Hợp Các Loại Thông Báo Cho Hệ Thống E-Learning

Dựa trên cấu trúc và các tính năng của hệ thống e-learning, đây là danh sách các sự kiện quan trọng nên có thông báo để tham khảo và chốt phương án triển khai. Việc xây dựng một hệ thống thông báo (cả trong ứng dụng và qua email) toàn diện sẽ giúp tăng cường trải nghiệm người dùng và giữ cho họ luôn được cập nhật.

---

### 1. **Quản Lý Người Dùng & Xác Thực**

- **Chào mừng người dùng mới:** Gửi email chào mừng ngay sau khi người dùng đăng ký thành công.
- **Yêu cầu đặt lại mật khẩu:** Gửi email chứa liên kết để người dùng đặt lại mật khẩu khi họ yêu cầu.
- **Xác nhận thay đổi mật khẩu:** Thông báo cho người dùng biết mật khẩu của họ đã được thay đổi thành công.
- **Liên kết tài khoản mạng xã hội:** Thông báo khi tài khoản mạng xã hội (Google, GitHub) được liên kết thành công với tài khoản trên hệ thống.

### 2. **Quy Trình Đăng Ký Giảng Viên**

- **Xác nhận nộp đơn:** Gửi thông báo cho người dùng xác nhận rằng đơn đăng ký làm giảng viên của họ đã được gửi đi.
- **Đơn được duyệt:** Thông báo cho người dùng khi đơn đăng ký của họ được quản trị viên phê duyệt, và vai trò của họ được nâng cấp thành `INSTRUCTOR`.
- **Đơn bị từ chối:** Thông báo cho người dùng khi đơn đăng ký bị từ chối, có thể kèm theo lý do để họ cải thiện và nộp lại (nếu được phép).

### 3. **Ghi Danh & Tiến Độ Học Tập (Dành cho Học viên)**

- **Xác nhận ghi danh:** Gửi thông báo (và email hóa đơn nếu là khóa học trả phí) sau khi học viên ghi danh thành công.
- **Hoàn thành khóa học:** Gửi lời chúc mừng và thông báo khi học viên hoàn thành 100% khóa học.
- **Chứng chỉ được cấp:** Thông báo cho học viên khi chứng chỉ hoàn thành khóa học của họ đã được tạo và sẵn sàng để tải xuống.

### 4. **Quản Lý Khóa Học (Dành cho Giảng viên)**

- **Khóa học chờ duyệt:** Xác nhận với giảng viên rằng khóa học của họ đã được gửi và đang chờ quản trị viên xem xét.
- **Khóa học được duyệt:** Thông báo cho giảng viên khi khóa học của họ được phê duyệt.
- **Khóa học bị từ chối:** Thông báo cho giảng viên khi khóa học bị từ chối, kèm theo phản hồi chi tiết từ quản trị viên.
- **Khóa học được xuất bản:** Thông báo khi khóa học của họ chính thức được đăng bán trên nền tảng.

### 5. **Thanh Toán & Doanh Thu**

- **Xác nhận thanh toán thành công:** Gửi email hóa đơn và xác nhận cho học viên.
- **Thông báo có học viên mới:** Gửi thông báo cho giảng viên khi có người mua khóa học của họ.
- **Yêu cầu hoàn tiền được gửi:** Xác nhận với học viên rằng yêu cầu hoàn tiền của họ đã được ghi nhận.
- **Kết quả xử lý hoàn tiền:** Thông báo cho học viên về kết quả (được chấp thuận hay bị từ chối) của yêu cầu hoàn tiền.
- **Doanh thu có sẵn để rút:** Thông báo cho giảng viên khi doanh thu từ một giao dịch đã qua thời gian chờ (ví dụ: 3 ngày) và có sẵn để rút.
- **Yêu cầu rút tiền được xử lý:** Thông báo cho giảng viên khi yêu cầu rút tiền của họ đã được quản trị viên xử lý và thanh toán.

### 6. **Tương Tác Cộng Đồng**

- **Có đánh giá mới:** Thông báo cho giảng viên khi có học viên để lại đánh giá (review) cho khóa học của họ.
- **Có câu hỏi mới:** Thông báo cho giảng viên khi có học viên đặt câu hỏi trong mục Q&A của khóa học.
- **Câu hỏi được trả lời:** Thông báo cho học viên khi câu hỏi của họ được giảng viên hoặc người khác trả lời.
- **Thông báo mới từ giảng viên:** Gửi đến tất cả học viên đã ghi danh khi giảng viên đăng một thông báo (announcement) mới cho khóa học.

### 7. **Thông Báo Dành Cho Quản Trị Viên (Admin)**

- **Có đơn đăng ký giảng viên mới:** Thông báo cho admin để họ vào xem xét.
- **Có khóa học mới cần duyệt:** Thông báo cho admin khi có giảng viên gửi khóa học mới.
- **Có yêu cầu hoàn tiền mới:** Thông báo cho admin để xử lý yêu cầu hoàn tiền từ học viên.
- **Cảnh báo hệ thống:** Các thông báo quan trọng như lỗi cổng thanh toán, lỗi server, hoặc các hoạt động đáng ngờ.

---

### **Phương án triển khai đề xuất:**

1.  **Tạo một `Notification` entity:** Lưu trữ tất cả các thông báo trong một bảng chung với các trường như `user_id` (người nhận), `type` (loại thông báo, vd: `ENROLLMENT_SUCCESS`), `message`, `is_read`, `link` (URL để điều hướng khi nhấp vào).
2.  **Sử dụng WebSocket:** Để đẩy thông báo real-time đến người dùng trên giao diện web (biểu tượng chuông).
3.  **Tích hợp Email Service:** Gửi email cho các thông báo quan trọng (hóa đơn, đặt lại mật khẩu, phê duyệt/từ chối).
4.  **Tạo một `NotificationService`:** Đóng gói logic tạo và gửi thông báo. Các service khác (như `PaymentService`, `EnrollmentService`) sẽ gọi `NotificationService` khi một sự kiện xảy ra.
