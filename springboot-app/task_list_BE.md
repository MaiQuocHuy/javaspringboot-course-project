# Admin Dashboard Task Breakdown

## 1. Sơn – Core & Courses Review

### Frontend

#### Setup & Layout

- Khởi tạo project **React Admin Dashboard**.
- Cấu hình **routing**, **base layout**, **theme**.
- Tạo **Sidebar navigation**.

#### Dashboard Overview

- **StatCards**: hiển thị tổng quan (users, courses, revenue).
- **Charts**: thống kê doanh thu, user mới, số khóa học theo trạng thái.

#### Courses Approval

- **CourseApprovalQueue**: danh sách course ở trạng thái _pending_ / _resubmitted_.
- **CourseDetailReview**: hiển thị chi tiết course, cho phép _approve/reject_.

### Backend

- API: Lấy thống kê dashboard.
- API: Lấy danh sách course ở trạng thái _pending_ và _resubmitted_.
- API: Approve/Reject course.

---

## 2. Phương – User Management

### Frontend

#### Users

- **UserTable**: hiển thị danh sách user (id, tên, email, role, trạng thái).
- Tính năng **Search**, **Filter**, **Pagination**.
- Action: **Ban/Unban**, **Assign Roles**.

#### User Detail

- Thông tin cá nhân.
- Danh sách **courses đã enroll**, **payments**, **logs**.

### Role Permission

- RolePermission: danh sách permissions của user.
- RolePermissionDetail: chi tiết permissions của role.
    + Hành động mở hoặc ẩn quyền của role
### Backend

- API: Lấy danh sách user.
- API: Chi tiết user, thực hiện update role, ban/unban, edit.
- API: Hiển thị danh sách role kèm permissions.
- API: Thêm role, cập nhật permission cho role.

---

## 3. Tâm – Instructor Applications

### Frontend

#### Applications List

- Danh sách hồ sơ ứng tuyển (tên, email, trạng thái, ngày nộp).
- Tìm kiếm, filter theo trạng thái.

#### Application Detail

- Xem thông tin hồ sơ, portfolio, kinh nghiệm.
- Action: **Approve/Reject**.

### Backend

- API: Lấy danh sách applications.
- API: Approve/Reject application.

---

## 4. Thừa Quốc Huy – Payments & Refunds

### Frontend

#### Payments Management

- **PaymentsTable**: danh sách giao dịch (user, course, số tiền, trạng thái, ngày).
- Tính năng **Search**, **Filter**, **Pagination**.

#### Refund Queue

- Danh sách yêu cầu hoàn tiền.
- Action: **Approve/Reject**.

### Backend

- API: Lấy danh sách payments.
- API: Cập nhật trạng thái payment từ `PENDING` → `COMPLETED` / `FAILED`.
- API: Lấy danh sách refund.
- API: Cập nhật trạng thái refund từ `PENDING` → `COMPLETED` / `FAILED` (nếu `FAILED` thì nhập **REASON**).
