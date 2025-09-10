# Notification Test Endpoints - Updated Guide

## Tình trạng hiện tại

✅ **ĐÃ KHẮC PHỤC:** Tất cả test endpoints đã được thêm `@PreAuthorize("permitAll()")` để cho phép truy cập mà không cần authentication.

## Danh sách Test Endpoints

### 1. Simple Test Endpoints

```
GET  /api/admin/notifications/test
POST /api/admin/notifications/test
```

### 2. Student Notification Test Endpoints

```
POST /api/admin/notifications/test/payment-success
POST /api/admin/notifications/test/enrollment
POST /api/admin/notifications/test/certificate
POST /api/admin/notifications/test/course-approval-approved
POST /api/admin/notifications/test/course-approval-rejected
POST /api/admin/notifications/test/refund
```

### 3. Admin Notification Test Endpoints

```
POST /api/admin/notifications/test/admin/student-payment
POST /api/admin/notifications/test/admin/payment-status-change
POST /api/admin/notifications/test/admin/course-approval-needed
POST /api/admin/notifications/test/admin/instructor-application
```

### 4. Bulk Test Endpoint

```
POST /api/admin/notifications/test/all
```

## Cách Test

### 1. Test không cần Authentication (Đề xuất bắt đầu từ đây)

```bash
# 1. Test GET endpoint đơn giản
curl -X GET "http://localhost:8080/api/admin/notifications/test"

# 2. Test POST endpoint đơn giản
curl -X POST "http://localhost:8080/api/admin/notifications/test"

# 3. Test student payment notification
curl -X POST "http://localhost:8080/api/admin/notifications/test/payment-success"

# 4. Test admin notification
curl -X POST "http://localhost:8080/api/admin/notifications/test/admin/student-payment"
```

### 2. Test với Postman

1. **Mở Postman**
2. **Tạo request mới:**

   - Method: `POST`
   - URL: `http://localhost:8080/api/admin/notifications/test/payment-success`
   - Headers: `Content-Type: application/json`
   - Body: Để trống (không cần body cho test endpoints)

3. **Gửi request** - Bây giờ không cần authentication token nữa!

### 3. Test với Authentication (Optional)

Nếu bạn muốn test với authentication:

```bash
curl -X POST "http://localhost:8080/api/admin/notifications/test/payment-success" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json"
```

## Expected Responses

### Successful Response (Student Notifications)

```json
{
  "status": "success",
  "code": 201,
  "message": "Payment success notification created successfully",
  "data": {
    "id": "notification-id",
    "title": "Payment Successful",
    "message": "...",
    "priority": "MEDIUM",
    "userId": "user-001",
    "resourceId": "res-payment-001",
    "resourceType": "PAYMENT"
  },
  "timestamp": "2025-01-10T03:25:00Z"
}
```

### Successful Response (Admin Notifications)

```json
{
  "status": "success",
  "code": 200,
  "message": "Admin student payment notification sent successfully",
  "data": "Notification sent to all users with payment:READ permission",
  "timestamp": "2025-01-10T03:25:00Z"
}
```

### Error Response

```json
{
  "status": "error",
  "code": 500,
  "message": "Error: [error details]",
  "data": null,
  "timestamp": "2025-01-10T03:25:00Z"
}
```

## Khắc phục sự cố

### Nếu vẫn gặp 403 Forbidden:

1. Kiểm tra Spring Boot đã restart chưa
2. Kiểm tra URL có đúng không
3. Kiểm tra logs trong terminal để xem lỗi chi tiết

### Nếu gặp 404 Not Found:

- Kiểm tra lại URL path
- Đảm bảo server đang chạy trên port 8080

### Nếu gặp 500 Internal Server Error:

- Kiểm tra logs của Spring Boot
- Có thể do database connection hoặc dependency injection issues

## Thông tin Debug

### Các thông tin sẽ xuất hiện trong logs:

```
INFO: Testing payment success notification
INFO: Testing admin student payment notification
INFO: Creating notification for user: user-001
INFO: Notification created successfully with ID: xxx
```

### Kiểm tra database:

- Notification table sẽ có records mới được tạo
- User notifications sẽ được gửi đến users có permission tương ứng

## Kết luận

✅ Tất cả test endpoints hiện tại đã có `@PreAuthorize("permitAll()")`
✅ Có thể test mà không cần authentication token
✅ 12 endpoints sẵn sàng để test tất cả functionality của NotificationHelper
✅ Bao gồm cả student notifications và admin notifications

**Bước tiếp theo:** Hãy test endpoint đầu tiên với curl hoặc Postman để đảm bảo hệ thống hoạt động chính xác!
