# 🔄 Liquibase Changeset Update Solutions

## 🚨 **Vấn đề:** Seed data thay đổi nhưng không được update

Khi bạn thay đổi dữ liệu trong changeset đã chạy, Liquibase sẽ không tự động chạy lại vì:

- Changeset đã được ghi vào bảng `DATABASECHANGELOG`
- Liquibase kiểm tra checksum và bỏ qua các changeset đã thực thi

## ✅ **Giải pháp 1: Tạo Changeset Mới (Recommended)**

Tôi đã tạo file `005-add-instructor-user.xml` với:

- Thêm user Charlie (instructor)
- Thêm role INSTRUCTOR cho Charlie
- Changeset ID mới: 36, 37

Cách này an toàn và theo best practice của Liquibase.

## 🔧 **Giải pháp 2: Clear Liquibase History (Force)**

### Nếu đang trong môi trường Development:

```sql
-- 1. Connect to your MySQL database
-- 2. Delete Liquibase tracking tables
DROP TABLE DATABASECHANGELOG;
DROP TABLE DATABASECHANGELOGLOCK;

-- 3. Restart application - Liquibase sẽ chạy lại tất cả changesets
```

### PowerShell Command:

```powershell
# Connect to MySQL and run the commands
mysql -u root -p course_management -e "DROP TABLE IF EXISTS DATABASECHANGELOG; DROP TABLE IF EXISTS DATABASECHANGELOGLOCK;"
```

## 🎯 **Giải pháp 3: Force Update Specific Changeset**

### Update checksum cho changeset đã thay đổi:

```sql
UPDATE DATABASECHANGELOG
SET MD5SUM = NULL
WHERE ID IN ('19', '20');
```

### Hoặc xóa specific changeset:

```sql
DELETE FROM DATABASECHANGELOG
WHERE ID IN ('19', '20');
```

## 🚀 **Cách Test:**

### 1. Check current changesets:

```sql
SELECT ID, AUTHOR, FILENAME, MD5SUM
FROM DATABASECHANGELOG
ORDER BY DATEEXECUTED DESC;
```

### 2. Check users in database:

```sql
SELECT u.id, u.name, u.email, ur.role
FROM USER u
LEFT JOIN USER_ROLE ur ON u.id = ur.user_id;
```

### 3. Expected result after fix:

```
alice@example.com    - ADMIN
bob@example.com      - STUDENT
charlie@example.com  - INSTRUCTOR
```

## 🎯 **Recommended Approach:**

1. ✅ **Use Solution 1** (new changeset) - đã implement
2. 🔄 Start application với `mvnw spring-boot:run`
3. 🧪 Test login với charlie@example.com/charlie123
4. 📋 Verify trong Swagger UI có dropdown với 3 roles

## 🛠️ **Database Connection Fix:**

File `application.properties` đã được update với default values:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/course_management}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
```

Bây giờ application sẽ chạy được mà không cần environment variables.
