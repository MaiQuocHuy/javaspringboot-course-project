# 🔐 Swagger Login API với Role Dropdown

## 📋 **Test Accounts Available:**

### 👑 **ADMIN** (Full System Access)

- **Email:** `alice@example.com`
- **Password:** `alice123`
- **Permissions:** Complete system administration, user management, course approval

### 👨‍🎓 **STUDENT** (Learning Access)

- **Email:** `bob@example.com`
- **Password:** `bob123`
- **Permissions:** Enroll in courses, track progress, submit reviews

### 👨‍🏫 **INSTRUCTOR** (Teaching Access)

- **Email:** `charlie@example.com`
- **Password:** `charlie123`
- **Permissions:** Create courses, manage content, view earnings

## 🎯 **How to Use in Swagger UI:**

### Method 1: Manual Entry

1. Go to `http://localhost:8080/swagger-ui/index.html`
2. Find `POST /api/auth/login`
3. Click "Try it out"
4. Enter any of the test credentials above

### Method 2: Dropdown Selection

1. Look for the "roleExample" dropdown in the request body
2. Select desired role (ADMIN/STUDENT/INSTRUCTOR)
3. The email and password fields will show the appropriate values
4. Copy and paste them into the actual email/password fields

## 📊 **Expected Response:**

```json
{
  "statusCode": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "refresh_token_here",
    "user": {
      "id": "uuid",
      "email": "alice@example.com",
      "name": "Alice Nguyen",
      "roles": ["ADMIN"]
    }
  },
  "timestamp": "2025-07-23T..."
}
```

## 🔄 **Role-Based API Testing:**

After login, use the returned `accessToken` in the Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### Admin-Only Endpoints:

- `GET /api/admin/users`
- `POST /api/admin/courses/{id}/approve`
- `GET /api/admin/instructors/applications`

### Student-Only Endpoints:

- `POST /api/courses/{id}/enroll`
- `GET /api/enrollments/my-courses`
- `POST /api/lessons/{id}/complete`

### Instructor-Only Endpoints:

- `POST /api/instructor/courses`
- `PUT /api/instructor/courses/{id}`
- `POST /api/instructor/sections/{sectionId}/lessons`

## 🛠️ **cURL Examples:**

### Admin Login:

```bash
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "alice@example.com",
    "password": "alice123"
  }'
```

### Student Login:

```bash
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "bob@example.com",
    "password": "bob123"
  }'
```

### Instructor Login:

```bash
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "charlie@example.com",
    "password": "charlie123"
  }'
```
