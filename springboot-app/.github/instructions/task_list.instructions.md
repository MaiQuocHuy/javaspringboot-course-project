---
applyTo: "**"
---

## Standard API Response Format

All API responses will follow a standardized format for consistency and ease of use on the frontend.

### Standard Success Response

For single resource or simple message responses.

```json
{
  "statusCode": 200,
  "message": "string",
  "data": {}
}
```

- `statusCode`: The HTTP status code.
- `message`: A descriptive message about the result.
- `data`: The payload of the response. Can be an object or a simple message object like `{"status": "success"}`.

### Standard Paginated Response

For lists of resources that support pagination.

```json
{
  "statusCode": 200,
  "message": "string",
  "data": {
    "content": [],
    "page": 0,
    "size": 10,
    "totalPages": 1,
    "totalElements": 1
  }
}
```

- `data.content`: An array of the resource objects.
- `data.page`: The current page number.
- `data.size`: The number of items per page.
- `data.totalPages`: The total number of pages.
- `data.totalElements`: The total number of items across all pages.

### Standard Error Response

```json
{
  "statusCode": 400,
  "message": "Error message describing the issue.",
  "error": "Bad Request"
}
```

---

## 1. Authentication & User Management

### 1.1. `POST /api/auth/register`

- **Description:** Registers a new user.
- **Request:**
  - **Method:** `POST`
  - **Path:** `/api/auth/register`
  - **Body (`RegisterUserDto`):**
    ```json
    {
      "name": "string",
      "email": "string",
      "password": "string"
    }
    ```
- **Response:**
  - **Success (201 Created):**
    ```json
    {
      "statusCode": 201,
      "message": "User registered successfully",
      "data": null
    }
    ```
- **Controller:** Create `AuthController` with a `register` endpoint.
- **Service:** Implement `AuthService` with a `register` method.
- Hash the user's password using BCrypt.
- Create a new `USER` entity and save it to the database.
- Assign the default `STUDENT` role in the `USER_ROLE` table.
- **Security:** Publicly accessible endpoint.
- **Testing:** Write unit tests for the registration service and integration tests for the endpoint.

### 1.2. `POST /api/auth/login`

- **Description:** Authenticates a user and returns JWT tokens.
- **Request:**
  - **Method:** `POST`
  - **Path:** `/api/auth/login`
  - **Body (`LoginUserDto`):**
    ```json
    {
      "email": "string",
      "password": "string"
    }
    ```
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Login successful",
      "data": {
        "accessToken": "string",
        "refreshToken": "string"
      }
    }
    ```
- **Controller:** Add a `login` endpoint to `AuthController`.
- **Service:** Implement `login` method in `AuthService`.
- Validate user credentials.
- Generate a JWT `accessToken` and `refreshToken`.
- **Security:** Publicly accessible. Use Spring Security for authentication logic.
- **Testing:** Test valid and invalid login attempts.

### 1.3. `POST /api/auth/refresh`

- **Description:** Refreshes an expired access token using a refresh token.
- **Request:**
  - **Method:** `POST`
  - **Path:** `/api/auth/refresh`
  - **Body:**
    ```json
    {
      "refreshToken": "string"
    }
    ```
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Token refreshed successfully",
      "data": {
        "accessToken": "string"
      }
    }
    ```
- **Controller:** Add a `refresh` endpoint to `AuthController`.
- **Service:** Implement `refresh` method in `AuthService`.
- Validate the `refreshToken`.
- Issue a new `accessToken`.
- **Security:** Requires a valid `refreshToken`.
- **Testing:** Test token refresh success and failure scenarios.

### 1.4. `GET /api/users/profile`

- **Description:** Retrieves the profile of the currently authenticated user.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/users/profile`
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Profile retrieved successfully",
      "data": {
        "id": "string",
        "name": "string",
        "email": "string",
        "roles": ["STUDENT"]
      }
    }
    ```
- **Controller:** Create `UserController` with a `getProfile` endpoint.
- **Service:** Implement `getProfile` in `UserService` to retrieve the current user's data from the security context.
- **Security:** Protected endpoint, accessible by any authenticated user.
- **Testing:** Test that the correct user profile is returned.

### 1.5. `PUT /api/users/profile`

- **Description:** Updates the profile of the currently authenticated user.
- **Request:**
  - **Method:** `PUT`
  - **Path:** `/api/users/profile`
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Body (`UpdateUserDto`):**
    ```json
    {
      "name": "string"
    }
    ```
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Profile updated successfully",
      "data": {
        "id": "string",
        "name": "string",
        "email": "string"
      }
    }
    ```
- **Controller:** Add an `updateProfile` endpoint to `UserController`.
- **Service:** Implement `updateProfile` in `UserService`.
- **Security:** Protected endpoint, accessible by any authenticated user.
- **Testing:** Test profile updates with valid and invalid data.

### 1.6. `POST /api/auth/forgot-password`

- **Description:** Initiates the password reset process for a user.
- **Request:**
  - **Method:** `POST`
  - **Path:** `/api/auth/forgot-password`
  - **Body:**
    ```json
    {
      "email": "string"
    }
    ```
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Password reset link has been sent to your email.",
      "data": null
    }
    ```
- **Controller:** Add a `forgotPassword` endpoint to `AuthController`.
- **Service:** Implement `forgotPassword` in `AuthService`.
  - Generate a secure, time-limited password reset token.
  - Store the token associated with the user.
  - Send an email to the user with the reset link.
- **Security:** Publicly accessible.
- **Testing:** Test with existing and non-existing email addresses.

### 1.7. `POST /api/auth/reset-password`

- **Description:** Resets a user's password using a valid token.
- **Request:**
  - **Method:** `POST`
  - **Path:** `/api/auth/reset-password`
  - **Body:**
    ```json
    {
      "oldPassword": "string",
      "newPassword": "string"
    }
    ```
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Password has been reset successfully.",
      "data": null
    }
    ```
- **Controller:** Add a `resetPassword` endpoint to `AuthController`.
- **Service:** Implement `resetPassword` in `AuthService`.
  - Validate the reset token.
  - Hash the new password and update the user's record.
  - Invalidate the reset token.
- **Security:** Publicly accessible.
- **Testing:** Test with valid, invalid, and expired tokens.

### 1.8. Social Login Integration (OAuth 2.0)

- **Description:** Configure social login providers like Google and GitHub.
- **Task:**
  - Add Spring Security OAuth2 Client dependency.
  - Configure `application.properties` or `application.yml` with client IDs and secrets for each provider.
  - Create a custom `OAuth2UserService` to handle user creation or linking upon successful social login.
  - Implement a success handler (`AuthenticationSuccessHandler`) to issue application-specific JWTs after a successful OAuth2 login.
- **Security:** Involves redirect flows to external providers.
- **Testing:** Test the full login flow for each configured social provider.

### 1.9. Brute-Force Protection

- **Description:** Implement a mechanism to prevent login brute-force attacks.
- **Task:**
  - Choose a caching mechanism (e.g., Caffeine for in-memory, Redis for distributed).
  - Create a service (`LoginAttemptService`) to track failed login attempts per IP or username.
  - Implement an `AuthenticationFailureHandler` in Spring Security.
  - On login failure, record the attempt in the service.
  - Before processing a login, check if the user/IP is currently blocked.
- **Security:** Core security enhancement.
- **Testing:** Write tests to ensure users are locked out after exceeding the attempt limit and that the lock expires correctly.

### 1.10. Manage Users

- **Description:** Admin functionality to manage users.
- **Endpoints:**
  - `GET /api/admin/users` - List all users.
  - `GET /api/admin/users/{id}` - Get user details by ID.
  - `PUT /api/admin/users/{id}/role` - Update user roles.
  - `PUT /api/admin/users/{id}/status` - Update user status (ACTIVE, INACTIVE).
- **Task:**
  - Create an `UserController` with endpoints to list, update, and delete users.
  - Implement methods in `UserService` for admin operations.
  - Use role-based access control to restrict these endpoints to users with the `ADMIN` role.
- **Security:** Requires `ADMIN` role.
- **Testing:** Write tests for user management operations, ensuring only admins can access them.

### 1.11. `POST /api/auth/logout`

- **Description:** Logs out the user by revoking the provided refresh token
- **Request:**
  - **Method:** `POST`
  - **Path:** `/api/auth/logout`
  - **Body:**
    ```json
    {
      "refreshToken": "string"
    }
    ```
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Logout successful.",
      "data": null
    }
    ```
  - **Error (400 Bad Request):**
    ```json
    {
      "statusCode": 400,
      "message": "Invalid refresh token.",
      "data": null
    }
    ```
- **Business Logic:**
  - Refresh Token is Required
    - The client must provide a valid refreshToken.
    - If missing or malformed, return 400 Bad Request.
  - Token Must Exist and Not Be Revoked
    - The refresh token must exist in the REFRESH_TOKEN table and not have is_revoked = true.
- **Controller:** Add a `logout` endpoint to `AuthController`.
- **Service:** Implement `logout` in `AuthService`.
  - Invalidate the provided refresh token.
  - Optionally, clear the user's session or token store.
- **Security:** Requires a valid `refreshToken`.
- **Testing:** Test logout with valid and invalid tokens, ensuring the token is invalidated.

### 1.12. `POST /api/auth/register-application`

- **Description:** Registers a new user account. If the role is INSTRUCTOR, an instructor application is also submitted along with the required documents.
- **Request:**

  - **Method:** `POST`
  - **Headers:**
    Content-Type: multipart/form-data
    \| Field | Type | Required | Description |
    \|---------------|------------------|----------|-------------|
    \| `name` | `string` | ✅ | Full name of the user |
    \| `email` | `string` | ✅ | Valid and unique email |
    \| `password` | `string` | ✅ | Password (minimum 6 characters) |
    \| `role` | `string` | ✅ | Role of the user (`STUDENT` or `INSTRUCTOR`) |
    \| `certificate` | `file` (PDF/DOCX/IMG) | ✅ if role = `INSTRUCTOR` | Professional certification file (max 15MB) |
    \| `cv` | `file` (PDF/DOCX/IMG) | ✅ if role = `INSTRUCTOR` | Resume/CV (max 15MB) |
    \| `portfolio` | `string (URL)` | ✅ if role = `INSTRUCTOR` | GitHub/LinkedIn/portfolio URL |
    \| `other` | `file` (optional)| ❌ | Optional supporting documents (max 15MB) |
  - **Response:**:
    - **Success (201 Created) – Role: STUDENT **-
    ```json
    {
      "statusCode": 201,
      "message": "Registration successful",
      "data": null
    }
    ```
    - **Success (201 Created) – Role: INSTRUCTOR**:
    ```json
    {
      "statusCode": 201,
      "message": "Registration successful",
      "data": null
    }
    ```
  - **Business Rules:**:
    - If role = INSTRUCTOR:
      - Instructor application is automatically created along with user registration.
      - User must provide certificate, cv, and valid portfolio URL.
      - Only users with no prior submission or rejected (once) within 3 days can apply.
    - If role = STUDENT:
      - Instructor application data is ignored.
    - If invalid data is provided or documents are missing (for INSTRUCTOR), registration will fail.
  - **Controller:** Add a `registerApplication` endpoint to `AuthController`.

- **Service:** Implement `registerApplication` in `AuthService`.
  - Validate the role and required fields.
  - If role = INSTRUCTOR, create an `InstructorApplication` with the provided documents.
  - Hash the password and save the user.
  - Assign the appropriate role in the `USER_ROLE` table.
- **Security:** Publicly accessible endpoint.
- **Testing:** Write tests for successful and failed registrations, ensuring the correct role and application status.

---

## 2. Course & Content (Public)

### 2.1. `GET /api/courses`

- **Description:** Retrieves a paginated list of all published courses.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/courses`
  - **Query Params:** `page` (number, optional), `size` (number, optional), `categoryId` (string, optional), `search` (string, optional), price range filters (optional), level , `sort` (string, optional)
- **Response:**

  - **Success (200 OK):** (Standard Paginated Response)

    ```json
    {
      "statusCode": 200,
      "message": "Courses retrieved successfully",
      "data": {
        "content": [
          {
            "id": "course-id",
            "title": "Mastering Java Spring Boot",
            "price": 29.99,
            "level": "INTERMEDIATE",
            "thumbnailUrl": "https://res.cloudinary.com/.../java-spring.jpg",
            "category": {
              "id": "category-id",
              "name": "Programming"
            },
            "instructor": {
              "id": "instructor-id",
              "name": "John Doe",
              "avatar": "https://res.cloudinary.com/.../avatar.jpg"
            }
          }
        ],
        "page": 0,
        "size": 10,
        "totalPages": 5,
        "totalElements": 42
      }
    }
    ```

- **Controller:** Create `CourseController` with a `findAllPublic` endpoint.
- **Service:** Implement `findAllPublic` in `CourseService` to fetch all published, non-deleted courses.
- **Repository:** Create a custom query in `CourseRepository`.
- **Security:** Publicly accessible.
- **Testing:** Test pagination and filtering.

### 2.2. `GET /api/courses/:id`

- **Description:** Retrieves the details of a single published course.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/courses/:id`
  - **Path Params:** `id` (string)
- **Response:**

  ```json
  {
    "statusCode": 200,
    "message": "Course details retrieved successfully",
    "data": {
      "id": "course-id",
      "slug": "mastering-java-spring-boot",
      "title": "Mastering Java Spring Boot",
      "description": "Learn how to build robust applications with Java Spring Boot.",
      "price": 29.99,
      "level": "INTERMEDIATE",
      "thumbnailUrl": "https://res.cloudinary.com/.../course-thumbnail.jpg",
      "lessonCount": 12,
      "sampleVideoUrl": "https://res.cloudinary.com/.../sample.mp4",
      "rating": {
        "average": 4.6,
        "totalReviews": 128
      },
      "isEnrolled": false,
      "instructor": {
        "id": "instructor-id",
        "name": "John Doe"
      },
      "sections": [
        {
          "id": "section-id",
          "title": "Introduction to Spring Boot",
          "lessons": [
            {
              "id": "lesson-id-1",
              "title": "Getting Started with Spring Boot",
              "type": "VIDEO"
            }
          ]
        }
      ]
    }
  }
  ```

- **Controller:** Add a `findOnePublic` endpoint to `CourseController`.
- **Service:** Implement `findOnePublic` to fetch a single published course by its ID.
- **Security:** Publicly accessible.
- **Testing:** Test for existing and non-existing courses.

### 2.3. `GET /api/categories`

- **Description:** Retrieves a list of all course categories.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/categories`
- **Response:**

  - **Success (200 OK):**

    ```json
    {
      "statusCode": 200,
      "message": "Categories retrieved successfully",
      "data": [
        {
          "id": "cat-001",
          "name": "Development",
          "slug": "development",
          "courseCount": 42
        }
      ]
    }
    ```

- **Controller:** Create `CategoryController` with a `findAll` endpoint.
- **Service:** Implement `findAll` in `CategoryService`.
- **Security:** Publicly accessible.
- **Testing:** Test that all categories are returned.

---

## 3. Student Role

### 3.1. `POST /api/courses/:id/enroll`

- **Description:** Enrolls the current user in a course.
- **Request:**

  - **Method:** `POST`
  - **Path:** `/api/courses/:id/enroll`
  - **Path Params:** `id` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`

- **Response:**

  - **Success (201 Created):**
    ```json
    {
      "statusCode": 201,
      "message": "Successfully enrolled in the course",
      "data": {
        "courseId": "course-001",
        "title": "Spring Boot Masterclass",
        "enrollmentDate": "2025-07-23T22:00:00Z"
      }
    }
    ```

- **Controller:** Create `EnrollmentController` with an `enroll` endpoint.
- **Service:** Implement `enroll(courseId, user)` in `EnrollmentService`.

- **Logic:**

  - Validate that the course exists, is published, and is not deleted.
  - Check if the user is already enrolled (return 409 Conflict if so).
  - If the course is paid:
    - Integrate with `PaymentService` to charge the user.
    - Ensure the actual paid amount is **≥ course price** before proceeding.
  - Create an `ENROLLMENT` record and return the enrollment data.

- **Security:** Requires `STUDENT` role.

- **Testing:**
  - Test enrollment in:
    - Paid course (valid payment).
    - Already enrolled case.
    - Invalid course ID.

### 3.2. `GET /api/enrollments/my-courses`

- **Description:** Retrieves all courses the current user is enrolled in.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/enrollments/my-courses`
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**

  - **Success (200 OK):**

  ```json
  {
    "statusCode": 200,
    "message": "Enrolled courses retrieved successfully",
    "data": {
      "content": [
        {
          "courseId": "course-001",
          "title": "Spring Boot Masterclass",
          "thumbnailUrl": "https://cdn.example.com/courses/springboot.jpg",
          "slug": "spring-boot-masterclass",
          "level": "INTERMEDIATE",
          "progress": 0.75,
          "completionStatus": "IN_PROGRESS",
          "instructor": {
            "id": "instructor-id",
            "name": "John Doe"
          }
        }
      ],
      "page": 0,
      "size": 10,
      "totalPages": 5,
      "totalElements": 42
    }
  }
  ```

- **Controller:** Add a `getMyCourses` endpoint to `EnrollmentController`.
- **Service:** Implement `getMyCourses` to fetch all courses the current user is enrolled in.
- **Security:** Requires `STUDENT` role.
- **Testing:** Test for a user with and without enrollments.

### 3.3. `POST /api/lessons/:id/complete`

- **Description:** Marks a lesson as completed for the current user.
- **Request:**
  - **Method:** `POST`
  - **Path:** `/api/lessons/:id/complete`
  - **Path Params:** `id` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Lesson marked as complete",
      "data": null
    }
    ```
- **Controller:** Create `LessonCompletionController` with a `completeLesson` endpoint.
- **Service:** Implement `completeLesson` in `LessonCompletionService`.
- Verify that the user is enrolled in the course.
- Create a `LESSON_COMPLETION` record.
- **Security:** Requires `STUDENT` role.
- **Testing:** Test marking a lesson as complete.

### 3.4. `POST /api/courses/:id/reviews`

- **Description:** Submits a review for a course.
- **Request:**
  - **Method:** `POST`
  - **Path:** `/api/courses/:id/reviews`
  - **Path Params:** `id` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Body (`CreateReviewDto`):**
    ```json
    {
      "rating": 5,
      "review_text": "string"
    }
    ```
- **Response:**
  - **Success (201 Created):**
    ```json
    {
      "statusCode": 201,
      "message": "Review submitted successfully",
      "data": {
        "id": "string",
        "rating": 5,
        "review_text": "string"
      }
    }
    ```
- **Controller:** Create `ReviewController` with a `createReview` endpoint.
- **Service:** Implement `createReview` in `ReviewService`.
- Verify the user is enrolled and has not already reviewed the course.
- **Security:** Requires `STUDENT` role.
- **Testing:** Test creating and failing to create a review.

### 3.6. `POST /api/instructor-applications/documents/upload`

- **Description:** Uploads required documents (certificate, portfolio, cv, optional other) to apply for the instructor role.
  Documents are stored on Cloudinary and recorded as a JSON key-value structure.
- **Request:**

  - **Method:** `POST`
  - **Path:** `/api/instructor-applications/documents/upload`
    `Content-Type: multipart/form-data`
  - **Headers**
    - `Authorization: Bearer <accessToken>`
  - **Body (multipart/form-data):**

    ```
     Field         | Type   | Required | Description                                                    |
    | ------------- | ------ | -------- | -------------------------------------------------------------- |
    |`certificate`|`file` | ✅ Yes    | Professional certification file (`pdf`, `docx`, `png`, `jpg`)  |
    | `portfolio`  |`text`| ✅ Yes    | Link to GitHub, LinkedIn, or demo portfolio                    |
    |`cv`         |`file` | ✅ Yes    | Resume/CV file (`pdf`, `docx`, `jpg`)                          |
    | `other`      |`file` | ❌ No | Additional supporting documents (e.g., awards, projects, etc.) |
    ```

- **Response:**

  - **Success (200 OK):**

    ```json
    {
      "statusCode": 200,
      "message": "Documents uploaded successfully",
      "data": {
        "userId": "c24b3fbd-8123-4567-a12b-9ee1abc12345",
        "documents": {
          "certificate": "https://res.cloudinary.com/.../certificate.pdf",
          "portfolio": "https://github.com/johndoe",
          "cv": "https://res.cloudinary.com/.../resume.pdf",
          "other": "https://res.cloudinary.com/.../award.png"
        }
      }
    }
    ```

- **Business Rules**:
  - Each user can submit only once, unless their previous application was REJECTED.
  - If an application is rejected, the user is allowed to resubmit only once, within 3 days from the rejection date.
  - If the user is rejected twice or the resubmission period expires, further submissions are not allowed.
  - While the user's application status is PENDING, they are not allowed to purchase courses or perform actions restricted to regular STUDENT roles.
- **Controller:** Create `InstructorApplicationController` with an `uploadDocument` endpoint.
- **Service:** Implement `uploadDocument` in `InstructorApplicationService`.
  - Store the document in Cloudinary or local storage.
  - Return the URL of the uploaded document.
- **Security:** Requires `STUDENT` role.
- **Testing:** Test document upload with valid and invalid files, ensuring the correct response structure.

### 3.5. `GET /api/student/courses/:id`

- **Description:** Retrieves all sections and lessons of a course for the student.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/student/courses/:id`
  - **Path Params:** `id` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Sections retrieved successfully",
      "data": [
        {
          "id": "section-uuid",
          "title": "Introduction",
          "order_index": 0,
          "lessonCount": 3,
          "lessons": [
            {
              "id": "lesson-uuid",
              "title": "Getting Started",
              "type": "VIDEO",
              "video": {
                "id": "video-uuid",
                "url": "https://res.cloudinary.com/.../video.mp4",
                "duration": 300
              },
              "isCompleted": false
            },
            {
              "id": "lesson-uuid-2",
              "title": "Course Overview",
              "type": "QUIZ",
              "quiz": {
                "questions": [
                  {
                    "id": "question-id-1",
                    "questionText": "What is Java?",
                    "options": [
                      "A. Language",
                      "B. Framework",
                      "C. OS",
                      "D. Compiler"
                    ],
                    "correctAnswer": "A",
                    "explanation": "Java is a programming language."
                  },
                  {
                    "id": "question-id-2",
                    "questionText": "What is JVM?",
                    "options": ["A", "B", "C", "D"],
                    "correctAnswer": "C",
                    "explanation": null
                  }
                ]
              },
              "isCompleted": false
            }
          ]
        },
        {
          "id": "section-uuid-2",
          "title": "Advanced Concepts",
          "order_index": 1,
          "lessonCount": 0,
          "lessons": []
        }
      ]
    }
    ```
- **Controller:** Create `StudentCourseController` with a `getSections` endpoint.
- **Service:** Implement `getSections` in `StudentCourseService`.
  - Verify the user is enrolled in the course.
  - Student must be enrolled in the course.
- **Security:** Requires `STUDENT` role.
- **Testing:** Test retrieval of sections and lessons for enrolled courses.

### 3.6. `GET /api/student/payments`

- **Description:** Retrieve a list of all payment transactions made by the currently authenticated student.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/student/payments`
  - **Headers:** `Authorization: Bearer <accessToken>`
  - \*\*Query Parameters: (Optional, for pagination if needed)
    - page (default: 0)
    - size (default: 10)
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Payments retrieved successfully",
      "data": [
        {
          "id": "payment-uuid",
          "amount": 1200000,
          "currency": "USD",
          "status": "COMPLETED",
          "paymentMethod": "STRIPE",
          "createdAt": "2025-08-01T10:30:00Z",
          "course": {
            "id": "course-uuid",
            "title": "KTC Backend Spring Boot",
            "thumbnailUrl": "https://cdn.ktc.com/images/spring-boot.png"
          }
        },
        {
          "id": "payment-uuid-2",
          "amount": 800000,
          "currency": "USD",
          "status": "PENDING",
          "paymentMethod": "BANK_TRANSFER",
          "createdAt": "2025-08-03T09:45:00Z",
          "course": {
            "id": "course-uuid-2",
            "title": "KTC Frontend React",
            "thumbnailUrl": "https://cdn.ktc.com/images/react.png"
          }
        }
      ]
    }
    ```
- **Controller:** Create `StudentPaymentController` with a `getPayments` endpoint.
- **Service:**
  - Authenticate the user.
  - Requires STUDENT role.
  - Implement `getPayments` in `StudentPaymentService`.
  - Fetch all payment transactions for the current student.
  - Default format is USD, but can be extended to support multiple currencies.
- **Repository:** Create a custom query in `PaymentRepository` to filter by student ID.
- **Security:** Requires `STUDENT` role.
- **Testing:** Test retrieval of payment transactions for the current student.

### 3.7 `GET /api/student/payments/:id`

- **Description:** Retrieve detailed information about a specific payment made by the student, including external gateway (e.g., Stripe) data if available.

- **Request:**
  - **Method:** `GET`
- **Endpoint:** `/api/student/payments/:id`
- **Headers:** `Authorization: Bearer <accessToken>`
- **Path Params:** `id` (string)
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Payment detail retrieved successfully",
      "data": {
        "id": "payment-uuid",
        "amount": 1200000,
        "currency": "VND",
        "status": "COMPLETED",
        "paymentMethod": "STRIPE",
        "createdAt": "2025-08-01T10:30:00Z",
        "transactionId": "pi_1OpYuW2eZvKYlo2Cabc123",
        "stripeSessionId": "cs_test_a1b2c3d4e5",
        "receiptUrl": "https://pay.stripe.com/receipts/xyz",
        "card": {
          "brand": "visa",
          "last4": "4242",
          "expMonth": 8,
          "expYear": 2027
        },
        "course": {
          "id": "course-uuid",
          "title": "KTC Backend Spring Boot",
          "thumbnailUrl": "https://cdn.ktc.com/images/spring-boot.png"
        }
      }
    }
    ```
- **Controller:** Add a `getPaymentDetail` endpoint to `StudentPaymentController`.
- **Service:** Implement `getPaymentDetail` in `StudentPaymentService`.
- **Logic:**
  - Validate the payment ID exists and belongs to the authenticated student.
  - Fetch detailed payment information, including external gateway data if available.
  - If stripeSessionId is present:
    - Use Stripe API to fetch additional details like transaction ID, card brand, last4 digits, and receipt URL.
    - Use Stripe API to fetch related data:
      - PaymentIntent → Transaction ID
      - Charge → card brand, last4, receipt URL, etc.
- **Security:** Requires `STUDENT` role.
- **Testing:** Test retrieval of payment details for valid and invalid payment IDs.

### 3.8 `GET /api/student/reviews`

- **Description:** Retrieves all reviews submitted by the current student.
- **Request:**

  - **Method:** `GET`
  - **Path:** `/api/student/reviews`
  - **Headers:** `Authorization: Bearer <accessToken>`
  - \*\*Query Parameters: (Optional, for pagination if needed)
    - page (default: 0)
    - size (default: 10)
  - **Options:**
    - `sort` (string, optional) - e.g., `rating,desc` to sort by rating in descending order, `reviewedAt,desc` to sort by review date in descending order.

- **Response:**

  - **Success (200 OK):**

    ```json
    {
      "statusCode": 200,
      "message": "Reviews retrieved successfully",
      "data": [
        {
          "id": "review-uuid-1",
          "course": {
            "id": "course-uuid-1",
            "title": "KTC Backend Spring Boot"
          },
          "rating": 5,
          "reviewText": "Great course!",
          "reviewedAt": "2025-08-01T10:30:00Z"
        },
        {
          "id": "review-uuid-2",
          "course": {
            "id": "course-uuid-2",
            "title": "KTC Frontend React"
          },
          "rating": 4,
          "reviewText": "Very informative.",
          "reviewedAt": "2025-08-03T09:45:00Z"
        }
      ]
    }
    ```

- **Controller:** Create `StudentReviewController` with a `getReviews` endpoint.
- **Service:** Implement `getReviews` in `StudentReviewService`.
- **Logic:**
  - Fetch all reviews submitted by the current student.
  - Include course details (ID, title) in the response.
- **Security:** Requires `STUDENT` role.
- **Testing:** Test retrieval of reviews for the current student.

### 3.9 `PATCH /api/student/reviews/{id}`

- **Description:** Updates an existing review submitted by the current student.
- **Request:**
  - **Method:** `PATCH`
  - **Path:** `/api/student/reviews/{id}`
  - **Headers:** `Authorization: Bearer <accessToken>` `Content-Type: application/json`
  - **Body (application/json):**
    ```json
    {
      "rating": 4,
      "reviewText": "Updated review text."
    }
    ```
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Review updated successfully",
      "data": {
        "id": "review-uuid",
        "course": {
          "id": "course-uuid",
          "title": "KTC Backend Spring Boot"
        },
        "rating": 4,
        "reviewText": "Updated review text.",
        "reviewedAt": "2025-08-01T10:30:00Z"
      }
    }
    ```
- **Controller:** Add an `updateReview` endpoint to `StudentReviewController`.
- **Service:** Implement `updateReview` in `StudentReviewService`.
- **Logic:**
  - Validate the review ID exists and belongs to the authenticated student.
  - Update the review with the provided rating and text.
- **Security:** Requires `STUDENT` role.
- **Testing:** Test updating a review with valid and invalid data.

---

## 4. Instructor Role

### 4.1. `POST /api/instructor/courses`

- **Description:** Creates a new course, including a thumbnail image upload.
- **Request:**

  - **Method:** `POST`
  - **Path:** `/api/instructor/courses`
  - **Headers:** `Authorization: Bearer <accessToken>` `Content-Type: multipart/form-data`
  - **Body (multipart/form-data):**
    ``
    "title": "string",
    "description": "string",
    "price": 0,
    "categoryId": "string",
    "categoryId": "string",
    "thumbnail": "file"

    ```

    ```

- **Response:**
  - **Success (201 Created):**
    ```json
    {
      "statusCode": 201,
      "message": "Course created successfully",
      "data": {
        "id": "string",
        "title": "string",
        "description": "string",
        "price": 0,
        "categories": [
          {
            "id": "string",
            "name": "string"
          },
          {
            "id": "string",
            "name": "string"
          }
        ],
        "thumbnailUrl": "file"
      }
    }
    ```
- **Controller:** Create `InstructorCourseController` with a `createCourse` endpoint.
- **Service:** Implement `createCourse` in `CourseService`, associating the course with the current instructor.
- **Security:** Requires `INSTRUCTOR` role.
- **Testing:** Test course creation.

### 4.2. `PATCH /api/instructor/courses/:id`

- **Description:** Updates a course owned by the instructor.
- **Request:**

  - **Method:** `PATCH`
  - **Path:** `/api/instructor/courses/:id`
  - **Path Params:** `id` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
    `Content-Type: multipart/form-data`
  - **Body (multipart/form-data):**
    ``
    "title": "string",
    "description": "string",
    "price": 0,
    "categoryId": "string",
    "categoryId": "string",
    "thumbnail": "file"

    ```

    ```

- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Course updated successfully",
      "data": {
        "id": "string",
        "title": "string",
        "description": "string",
        "price": 0,
        "categories": [
          {
            "id": "string",
            "name": "string"
          },
          {
            "id": "string",
            "name": "string"
          }
        ],
        "thumbnailUrl": "file",
        "level": "BEGINNER" | "INTERMEDIATE" | "ADVANCED"
      }
    }
    ```
- **Controller:** Add an `updateCourse` endpoint.
- **Service:** Implement `updateCourse`, ensuring the instructor owns the course.
- **Security:** Requires `INSTRUCTOR` role.
- **Testing:** Test successful and unauthorized updates.

### 4.6. `GET /api/instructor/courses`

- **Description:** Retrieves all courses created by the instructor.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/instructor/courses`
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**

  - **Success (200 OK):**

    ```json
    {
      "statusCode": 200,
      "message": "Instructor courses retrieved successfully",
      "data": {
        "content": [
          {
            "id": "course-id-001",
            "title": "Mastering Spring Boot",
            "price": 49.99,
            "level": "INTERMEDIATE",
            "thumbnailUrl": "https://res.cloudinary.com/.../spring-boot.jpg",
            "category": {
              "id": "cat-001",
              "name": "Programming"
            },
            "status": "PUBLISHED",
            "isApproved": true,
            "createdAt": "2025-06-01T08:00:00Z",
            "updatedAt": "2025-07-22T10:30:00Z",
            "lastContentUpdate": "2025-07-20T09:15:00Z",
            "totalStudents": 120,
            "averageRating": 4.5,
            "revenue": 5998.8,
            "canEdit": false,
            "canUnpublish": true,
            "canDelete": false
          }
        ],
        "page": 0,
        "size": 10,
        "totalPages": 1,
        "totalElements": 1
      }
    }
    ```

- **Controller:** Add a `getCourses` endpoint.
- **Service:** Implement `getCourses`, ensuring only the instructor's courses are retrieved.
- **Security:** Requires `INSTRUCTOR` role.
- **Testing:** Test retrieval of instructor's courses.

### 4.7. `DELETE /api/instructor/courses/:id`

- **Description:** Deletes a course owned by the instructor.
  Only allowed if the course is not approved and has no enrolled students.
- **Request:**
  - **Method:** `DELETE`
  - **Path:** `/api/instructor/courses/:id`
  - **Path Params:** `id` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Business Rules**:
  - Only the instructor who created the course can delete it.
  - Courses that are approved cannot be deleted.
  - Courses with enrolled students cannot be deleted.
- **Response:**
  - **Success (204 No Content):**
    ```json
    {
      "statusCode": 200,
      "message": "Course deleted successfully"
    }
    ```
- **Controller:** Add a `deleteCourse` endpoint.
- **Service:** Implement `deleteCourse`, ensuring the instructor owns the course, verify course ownership, check isApproved, check enrollment count, then delete.
- **Security:** Requires `INSTRUCTOR` role.
- **Testing:**
  - Successful deletion when rules are satisfied
  - Prevent deletion if course is approved or has students
  - Unauthorized users can't delete other's courses

### 4.8. `PATCH /api/instructor/courses/:id/status`

- **Description:** Updates the visibility status of a course.
  Instructors can only PUBLISH a course if it meets all required conditions and can only UNPUBLISH a course that is approved and already published.
- **Request:**
  - **Method:** `PATCH`
  - **Path:** `/api/instructor/courses/:id/status`
  - **Path Params:** `id` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Body:**
    ```json
    {
      "status": "PUBLISHED" | "UNPUBLISHED"
    }
    ```
  - **Business Rules:**
    - Only the instructor who owns the course can update its status.
    - Allowed status values: "PUBLISHED" or "UNPUBLISHED".
    - Can only PUBLISH if:
      - Course has complete details: title, description, thumbnail, and at least one section or lesson.
    - Can only UNPUBLISH if:
      - Course is approved (isApproved = true) and currently published (isPublished = true).
    - Not allowed to:
      - UNPUBLISH a course that has never been published
      - Re-publish a course that is already published
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Course status updated successfully",
      "data": {
        "id": "string",
        "title": "string",
        "previousStatus": "UNPUBLISHED",
        "currentStatus": "PUBLISHED"
      }
    }
    ```
- **Controller:** Add an `updateCourseStatus` endpoint.
- **Service:** Implement `updateCourseStatus` in `CourseService`.Verify course ownership, Validate the course status change rules, Update isPublished if valid, Return old and new status in the response
- **Security:** Only users with the INSTRUCTOR role are allowed.Ownership verification is required
- **Testing:**
- Success when the course is valid and the instructor owns it
- Reject if:
  - Course does not meet the publish conditions
  - The user is not the course owner
  - Attempting to unpublish a course that was never published
- No status change if the same status is sent again

### 4.9. `GET /api/instructor/courses/:id/sections`

- **Description:** Retrieves all sections of a course owned by the instructor.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/instructor/courses/:id/sections`
  - **Path Params:** `id` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Business Rules:**
    - Only the instructor who owns the course can view its sections.
    - If the course doesn't exist or the instructor is not the owner, access should be denied.
    - If the course exists but has no sections, return an empty array.
  - **Response:** - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Sections retrieved successfully",
      "data": [
        {
          "id": "section-uuid",
          "title": "Introduction",
          "order": 1,
          "lessonCount": 3,
          "lessons": [
            {
              "id": "lesson-uuid",
              "title": "Getting Started",
              "type": "VIDEO",
              "video": {
                "id": "video-uuid",
                "url": "https://res.cloudinary.com/.../video.mp4",
                "duration": 300
              }
            },
            {
              "id": "lesson-uuid-2",
              "title": "Course Overview",
              "type": "QUIZ",
              "quiz": {
                "questions": [
                  {
                    "id": "question-id-1",
                    "questionText": "What is Java?",
                    "options": [
                      "A. Language",
                      "B. Framework",
                      "C. OS",
                      "D. Compiler"
                    ],
                    "correctAnswer": "A",
                    "explanation": "Java is a programming language."
                  },
                  {
                    "id": "question-id-2",
                    "questionText": "What is JVM?",
                    "options": ["A", "B", "C", "D"],
                    "correctAnswer": "C",
                    "explanation": null
                  }
                ]
              }
            }
          ]
        },
        {
          "id": "section-uuid-2",
          "title": "Advanced Concepts",
          "order": 2,
          "lessonCount": 0,
          "lessons": []
        }
      ]
    }
    ```
- **Controller:** Add a `getSections` endpoint.
- **Service:**

  - Check course ownership.
  - Fetch sections with course_id.
  - For each section, fetch lessons ordered by order.
  - For each lesson:
    - If type = "VIDEO", include video info.
    - If type = "QUIZ", fetch all QUIZ_QUESTION by lesson_id.
  - Map results to SectionWithLessonsDto.

- **Security:** Requires `INSTRUCTOR` role and course ownership.
- **Testing:** Test retrieval of course sections.

### 4.10. `POST /api/instructor/courses/:id/sections`

- **Description:** Creates a new section in a course owned by the instructor.

- **Request:**

  - **Method:** `POST`
  - **Path:** `/api/instructor/courses/:id/sections`
  - **Path Params:** `id` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Body (`CreateSectionDto`):**
    ```json
    {
      "title": "string"
    }
    ```

- **Response:**

  - **Success (201 Created):**

    ```json
    {
      "statusCode": 201,
      "message": "Section created successfully",
      "data": {
        "id": "section-uuid",
        "title": "string",
        "order_index": 0,
        "courseId": "course-uuid"
      }
    }
    ```

  - **Error (403 Forbidden):** Not course owner
  - **Error (404 Not Found):** Course doesn't exist

- **Controller:** `InstructorSectionController.createSection`

- **Service:**

  - Validate course exists and instructor is the owner.
  - Calculate the next order_index.
  - Save new section and return DTO.

- **Security:** Requires `INSTRUCTOR` role and course ownership.

- **Testing:**
  - Valid section creation
  - Missing/invalid title
  - Unauthorized access
  - Not the course owner

### 4.11 `PATCH /api/instructor/courses/:courseId/sections/:sectionId`

- **Description:** Updates an existing section in a course owned by the instructor.

- **Request:**

  - **Method:** `PATCH`
  - **Path:** `/api/instructor/courses/:courseId/sections/:sectionId`
  - **Path Params:** `courseId` (string), `sectionId` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Body (`CreateSectionDto`):**
    ```json
    {
      "title": "string (required, min: 3, max: 255)"
    }
    ```

- **Response:**

  - **Success (200 OK):**

    ```json
    {
      "statusCode": 200,
      "message": "Section updated successfully",
      "data": {
        "id": "section-uuid",
        "title": "string",
        "order_index": 0,
        "courseId": "course-uuid"
      }
    }
    ```

  - **Error (403 Forbidden):** Not course owner
  - **Error (404 Not Found):** Course or section doesn't exist

- **Controller:** `InstructorSectionController.updateSection`
- **Service:** Implement `updateSection` in `SectionService`.
- **Security:** Requires `INSTRUCTOR` role and course ownership.

- **Testing:**
  - Valid section update
  - Missing/invalid title
  - Unauthorized access

### 4.12 `DELETE /api/instructor/courses/:courseId/sections/:sectionId`

- **Description:**
  - Deletes a section from a course that is owned by the currently authenticated instructor.
  - After deletion, the system automatically reorders the remaining sections to maintain continuous order values.
- **Request:**
  - **Method:** `DELETE`
  - **Path:** `/api/instructor/courses/:courseId/sections/:sectionId`
  - **Path Params:** `courseId` (string), `sectionId` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**
  - **Success (204 No Content):**
    ```json
    {
      "statusCode": 204,
      "message": "Section deleted successfully"
    }
    ```
  - **Error (403 Forbidden):** Not course owner
  - **Error (404 Not Found):** Course or section doesn't exist
- **Controller:** `InstructorSectionController.deleteSection`
- **Service:**
  - Verify that the course exists and is owned by the current instructor
  - Verify that the section exists and belongs to the course
  - Delete the section
  - Reorder the remaining sections of the course to maintain a valid sequence of order (e.g: 0,1,2,3)
- **Security:** Requires `INSTRUCTOR` role and course ownership.
- **Testing:**
  - Valid section deletion
  - Unauthorized access
  - Non-existent course or section

### 4.13. `PATCH /api/instructor/courses/:courseId/sections/reorder`

- **Description:** Reorders sections within a course owned by the instructor.
- **Request:**
  - **Method:** `PATCH`
  - **Path:** `/api/instructor/courses/:courseId/sections/reorder`
  - **Path Params:** `courseId` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Body:**
    `json(This array must include all section IDs of the course in their intended order.
Any missing or duplicate IDs will result in a 400 Bad Request.)
    {
      "sectionOrder": [
        "section-uuid-1",
        "section-uuid-2",
        "section-uuid-3"
      ]
    }
    `
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Sections reordered successfully",
      "data": null
    }
    ```
  - **Error (403 Forbidden):** Not course owner
  - **Error (404 Not Found):** Course doesn't exist
  - **Error (400 Bad Request):** Invalid section order (missing or duplicate IDs)
- **Controller:** `InstructorSectionController.reorderSections`, Course exists, Instructor is the course owner, All section IDs belong to the course, No missing or duplicate IDs

- **Service:** Implement `reorderSections` in `SectionService`, reorder eg(0,1,2,3).
- **Security:** Requires `INSTRUCTOR` role and course ownership.
- **Testing:**
  - Valid reordering of sections
  - Unauthorized access
  - Non-existent course

### 4.4. `POST /api/instructor/sections/:sectionId/lessons`

- **Description:** Creates a new lesson in a section.
- **Request:**
  - **Method:** `POST`
  - **Path:** `/api/instructor/sections/:sectionId/lessons`
  - **Path Params:** `sectionId` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Body (`CreateLessonDto`):**
    ```json
    {
      "title": "string",
      "type": "VIDEO",
      "contentId": "string"
    }
    ```
- **Response:**
  - **Success (201 Created):**
    ```json
    {
      "statusCode": 201,
      "message": "Lesson created successfully",
      "data": {
        "id": "string",
        "title": "string"
      }
    }
    ```
- **Controller:** Create `InstructorLessonController` with a `createLesson` endpoint.
- **Service:** Implement `createLesson` in `LessonService`.
- **Security:** Requires `INSTRUCTOR` role and section ownership.
- **Testing:** Test lesson creation.

### 4.5. `GET /api/instructor/sections/:sectionId/lessons`

- **Description:** Retrieves a section and all of its lessons (including video or quiz details and the instructor's lesson completion status), ensuring ownership and access rights.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/instructor/sections/:sectionId/lessons`
  - **Path Params:** `sectionId` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**

  - **Success (200 OK):**

  ```json
  {
    "statusCode": 200,
    "message": "Lessons retrieved successfully",
    "data": {
      "id": "section-uuid",
      "title": "Introduction to Spring Boot",
      "orderIndex": 0,
      "lessonCount": 2,
      "lessons": [
        {
          "id": "lesson-uuid-1",
          "title": "Getting Started with Spring Boot",
          "type": "VIDEO",
          "video": {
            "id": "video-uuid-1",
            "url": "https://res.cloudinary.com/.../video.mp4",
            "duration": 300
          },
          "orderIndex": 0,
          "isCompleted": true
        },
        {
          "id": "lesson-uuid-2",
          "title": "Understanding Spring Boot Annotations",
          "type": "QUIZ",
          "quiz": {
            "questions": [
              {
                "id": "question-id-1",
                "questionText": "What is @SpringBootApplication?",
                "options": ["A", "B", "C", "D"],
                "correctAnswer": "A",
                "explanation": null
              }
            ]
          },
          "orderIndex": 1,
          "isCompleted": false
        }
      ]
    }
  }
  ```

- **Controller:** Create `InstructorLessonController` with a `getLessons` endpoint.
- **Service:**
  - Implement `getLessons` in `LessonService`.
  - Lessons can be of type VIDEO or QUIZ:
    - If type === "VIDEO" and content_id != null → the video key will be included .
    - If type === "QUIZ" and content_id == null → the quiz key will be included.
  - isCompleted is derived from the LESSON_COMPLETION table based on the user_id.
- **Security:** Requires `INSTRUCTOR` role and section ownership.Requires user to have INSTRUCTOR role. Instructor must be the owner of the section's course.

### 4.14. `POST /api/instructor/sections/:sectionId/lessons`

- **Description:** Creates a new lesson in a section owned by the instructor.
- **Request:**
  - **Method:** `POST`
  - **Path:** `/api/instructor/sections/:sectionId/lessons`
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Body (`CreateLessonDto`):**
    ```(multipart/form-data)
    {
      "title": "string",
      "type": "VIDEO",
      "videoFile": "file",
    }
    ```
- **Response:**
  - **Success (201 Created):**
    ```json
    {
      "statusCode": 201,
      "message": "Lesson created successfully",
      "data": {
        "id": "string",
        "title": "string",
        "type": "VIDEO",
        "video": {
          "id": "video-uuid",
          "url": "https://res.cloudinary.com/.../video.mp4",
          "duration": 300
        },
        "order_index": 0
      }
    }
    ```
- **Controller:** Create `InstructorLessonController` with a `createLesson` endpoint.
- **Service:** Implement `createLesson` in `LessonService`. `order_index`: auto-incremented based on the current number of lessons in the section(0-based). "duration: number (in seconds, rounded to integer)"
- **Security:** Requires `INSTRUCTOR` role and section ownership.
- **Testing:**
  - Create video lesson successfully with valid input
  - Create lesson with missing videoFile (expect 400)
  - Unauthorized access (expect 401)
  - Instructor creates lesson in section they do not own (expect 403)

### 4.15. `PATCH /api/instructor/sections/:sectionId/lessons/:lessonId`

- **Description:** Updates an existing lesson (title and video) in a section owned by the instructor.
  If the lesson is of type VIDEO, only title and videoFile can be updated — the type cannot be changed.
  If a new videoFile is uploaded, the old video (if any) will be deleted automatically.
  The uploaded video must be in a valid video format.
- **Request:**
  - **Method:** `PATCH`
  - **Path:** `/api/instructor/sections/:sectionId/lessons/:lessonId`
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Body (`UpdateLessonDto`):**
    ```(multipart/form-data)
    {
      "title": "string",
      "type": "VIDEO",
      "videoFile": "file (optional)"
    }
    ```
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Lesson updated successfully",
      "data": {
        "id": "string",
        "title": "string",
        "type": "VIDEO",
        "video": {
          "id": "video-uuid",
          "url": "https://res.cloudinary.com/.../video.mp4",
          "duration": 300
        },
        "order_index": 0
      }
    }
    ```
- **Business Rules:**
  - Only the instructor who owns the section can update the lesson.
  - The lesson type cannot be changed from VIDEO to QUIZ or vice versa.
  - If a new videoFile is provided, it must be in a valid video format (e.g., mp4, avi, etc.).
  - If a new videoFile is uploaded, the old video will be deleted automatically.
- **Controller:** Create `InstructorLessonController` with an `updateLesson` endpoint.
- **Service:** Implement `updateLesson` in `LessonService`.
- **Security:** Requires `INSTRUCTOR` role and section ownership.
- **Testing:**
  - Update lesson successfully with valid input
  - Update lesson with missing videoFile (expect 200, no change to video)
  - Unauthorized access (expect 401)
  - Instructor updates lesson in section they do not own (expect 403)

### 4.16. `DELETE /api/instructor/sections/:sectionId/lessons/:lessonId`

- **Description:** Deletes a lesson from a section owned by the instructor.
  If the lesson is of type VIDEO, the associated video file will also be deleted. also delete video file from cloud storage.
- **Request:**
  - **Method:** `DELETE`
  - **Path:** `/api/instructor/sections/:sectionId/lessons/:lessonId`
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**
  - **Success (200):**
    ```json
    {
      "statusCode": 200,
      "message": "Lesson deleted successfully"
    }
    ```
- **Business Rules:**
  - Only the instructor who owns the section can delete the lesson.
  - If the lesson is of type VIDEO, the associated video file will also be deleted from cloud storage.
- **Controller:** Create `InstructorLessonController` with a `deleteLesson` endpoint.
- **Service:** Implement `deleteLesson` in `LessonService`.
- **Security:** Requires `INSTRUCTOR` role and section ownership.
- **Testing:**
  - Delete lesson successfully
  - Unauthorized access (expect 401)
  - Instructor deletes lesson in section they do not own (expect 403)
  - Verify video file deletion if lesson type is VIDEO
- **Error Handling:** If the lesson does not exist, return 404 Not Found.

### 4.17. `PATCH /api/instructor/sections/:sectionId/lessons/reorder`

- **Description:** Reorders lessons within a section owned by the instructor.
- **Request:**
  - **Method:** `PATCH`
  - **Path:** `/api/instructor/sections/:sectionId/lessons/reorder`
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Body:**
    ```json
    {
      "lessonOrder": ["lessonId1", "lessonId2", "lessonId3"]
    }
    ```
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Lessons reordered successfully",
      "data": null
    }
    ```
- **Business Rules:**
  - Only the instructor who owns the section can reorder lessons.
  - The `lessonOrder` array must include all lesson IDs of the section in their intended order.
  - Any missing or duplicate IDs will result in a 400 Bad Request.
- **Controller:** Create `InstructorLessonController` with a `reorderLessons` endpoint.
- **Service:** Implement `reorderLessons` in `LessonService`.
- **Security:** Requires `INSTRUCTOR` role and section ownership.
- **Testing:** Test lesson reordering with valid and invalid inputs.

### 4.18. `POST /api/instructor/sections/:sectionId/lessons/:lessonId/complete`

- **Description:** Allows an instructor to mark their own lesson as reviewed or completed. This helps instructors track which lessons have been finalized or verified during course creation or update.
- **Request:**
  - **Method:** `POST`
  - **Path:** `/api/instructor/sections/:sectionId/lessons/:lessonId/complete`
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Lesson marked as completed",
      "data": null
    }
    ```
- **Business Rules:**
  - Only the instructor who owns the section can mark the lesson as completed.
    - Only the instructor who owns the section can mark the lesson as completed.
  - The `lessonId` must belong to the given `sectionId`.
  - Completion state should be stored in the `lesson_completion` table.
  - Duplicate marking should be idempotent
- **Controller:** Create `InstructorLessonController` with a `completeLesson` endpoint.
- **Service:** Implement `completeLesson` in `LessonService`.
- **Security:** Requires `INSTRUCTOR` role and section ownership.
- **Testing:**
  - Successfully mark lesson as completed.
  - Try marking a lesson not in the instructor’s section (expect 403).
  - Unauthorized user (expect 401).
  - Multiple completions should not duplicate data or throw error.

### 4.19. `GET /api/instructor/earnings`

- **Description:** Retrieves a paginated list of all earnings for the instructor with filtering and sorting options.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/instructor/earnings`
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Query Params:**
    - `page` (number, optional, default: 0): Page number for pagination
    - `size` (number, optional, default: 10): Number of items per page
    - `status` (string, optional): Filter by earning status (`PENDING`, `AVAILABLE`, `PAID`)
    - `courseId` (string, optional): Filter by specific course
    - `sort` (string, optional, default: `createdAt,desc`): Sort criteria
    - `dateFrom` (string, optional): Filter earnings from date (ISO format)
    - `dateTo` (string, optional): Filter earnings to date (ISO format)
- **Response:**
  - **Success (200 OK):** (Standard Paginated Response)
    ```json
    {
      "statusCode": 200,
      "message": "Earnings retrieved successfully",
      "data": {
        "content": [
          {
            "id": "earning-uuid-1",
            "courseId": "course-uuid-1",
            "courseTitle": "Advanced Spring Boot",
            "courseThumbnailUrl": "https://res.cloudinary.com/.../course-thumb.jpg",
            "paymentId": "payment-uuid-1",
            "amount": 24.99,
            "platformCut": 5.0,
            "instructorShare": 19.99,
            "status": "PAID",
            "paidAt": "2025-07-15T14:30:00Z"
          }
        ],
        "page": 0,
        "size": 10,
        "totalPages": 3,
        "totalElements": 28,
        "summary": {
          "totalEarnings": 2459.72,
          "pendingAmount": 199.96,
          "availableAmount": 859.8,
          "paidAmount": 1399.96,
          "totalTransactions": 28
        }
      }
    }
    ```
- **Controller:** Create `InstructorEarningController` with a `getEarnings` endpoint.
- **Service:** Implement `getEarnings` in `InstructorEarningService` to fetch earnings for the current instructor.
- **Repository:** Create custom queries in `InstructorEarningRepository` for filtering and pagination.
- **Security:** Requires `INSTRUCTOR` role.
- **Testing:** Test pagination, filtering by status, course, and date ranges. - Test detailed earning retrieval and ownership validation.
  - No query params Defaults to page 0, size 10, sorted by createdAt asc
  - Filter by status=PAID Returns only paid earnings
  - Filter by courseId Returns only earnings for that course
  - Filter by date range Returns earnings within the date range
  - No earnings found Empty content, summary values are zero

### 4.20. `GET /api/instructor/earnings/{id}`

- **Description:** Retrieves detailed information about a specific earning record that belongs to the currently authenticated instructor.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/instructor/earnings/{id}`
  - **Path Params:** `id` (string): Earning record ID
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**

  - **Success (200 OK):**

    ```json
    {
      "statusCode": 200,
      "message": "Earning details retrieved successfully",
      "data": {
        "id": "earning-uuid-1",
        "courseId": "course-uuid-1",
        "courseTitle": "Advanced Spring Boot",
        "courseDescription": "Learn advanced Spring Boot concepts...",
        "courseThumbnailUrl": "https://res.cloudinary.com/.../course-thumb.jpg",
        "paymentId": "payment-uuid-1",
        "amount": 24.99,
        "platformCut": 5.0,
        "platformCutPercentage": 20,
        "instructorShare": 19.99,
        "status": "PAID",
        "paidAt": "2025-07-15T14:30:00Z"
      }
    }
    ```

- **Controller:** Add a `getEarningDetails` endpoint to `InstructorEarningController`.
- **Service:** Implement `getEarningDetails` in `InstructorEarningService`.
- **Business Rules**:

  - Only the instructor who owns the earning record can view its details.
  - If the earning record does not exist or does not belong to the instructor, return a 404 Not Found error.
  - Include course and payment information, but do not expose student or payment owner data — only amounts.
  - Ensure that the response includes all relevant details about the earning record, such as course title, description, thumbnail, payment ID, amounts, and status.

- **Security:**:
  - Requires authentication with the INSTRUCTOR role.
  - Ownership check is mandatory: instructors can only access their own earnings.
    - Return:
      - 403 Forbidden if not the owner.
      - 404 Not Found if the earning does not exist.
- **Testing:**
  - Valid request returns correct data.
  - Invalid ID returns 404.
  - Access to other instructors’ records returns 403.
  - Unauthorized access returns 401.

### 4.21. `GET /api/instructor/earnings/summary`

- **Description:** Retrieves earnings summary statistics for the instructor's dashboard.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/instructor/earnings/summary`
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Query Params:**
    - `period` (string, optional): Time period for statistics (`week`, `month`, `quarter`, `year`, `all`)
    - `courseId` (string, optional): Filter summary by specific course
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Earnings summary retrieved successfully",
      "data": {
        "period": "month",
        "dateRange": {
          "from": "2025-07-01T00:00:00Z",
          "to": "2025-07-31T23:59:59Z"
        },
        "totalEarnings": 2459.72,
        "pendingAmount": 199.96,
        "availableAmount": 859.8,
        "paidAmount": 1399.96,
        "totalTransactions": 28,
        "averageEarningPerSale": 87.85,
        "topPerformingCourses": [
          {
            "courseId": "course-uuid-1",
            "courseTitle": "Advanced Spring Boot",
            "totalEarnings": 899.75,
            "salesCount": 12,
            "averageRating": 4.8
          },
          {
            "courseId": "course-uuid-2",
            "courseTitle": "React Fundamentals",
            "totalEarnings": 759.92,
            "salesCount": 8,
            "averageRating": 4.6
          }
        ],
        "monthlyTrend": [
          {
            "month": "2025-07",
            "earnings": 2459.72,
            "transactions": 28
          },
          {
            "month": "2025-06",
            "earnings": 1899.45,
            "transactions": 22
          }
        ],
        "statusBreakdown": {
          "pending": {
            "amount": 199.96,
            "count": 3
          },
          "available": {
            "amount": 859.8,
            "count": 12
          },
          "paid": {
            "amount": 1399.96,
            "count": 13
          }
        }
      }
    }
    ```
- **Controller:** Add a `getEarningsSummary` endpoint to `InstructorEarningController`.
- **Service:** Implement complex aggregation queries in `InstructorEarningService`.
- **Repository:** Create custom aggregate queries for statistics calculation.
- **Security:** Requires `INSTRUCTOR` role.
- **Testing:** Test summary calculations with different time periods and filters.

### 4.22. `GET /api/instructor/earnings/analytics`

- **Description:** Retrieves detailed analytics and charts data for instructor earnings visualization.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/instructor/earnings/analytics`
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Query Params:**
    - `period` (string, optional, default: `month`): Time period (`week`, `month`, `quarter`, `year`)
    - `granularity` (string, optional, default: `daily`): Data granularity (`daily`, `weekly`, `monthly`)
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Earnings analytics retrieved successfully",
      "data": {
        "period": "month",
        "granularity": "daily",
        "chartsData": {
          "earningsOverTime": [
            {
              "date": "2025-07-01",
              "earnings": 125.5,
              "transactions": 3,
              "newStudents": 3
            },
            {
              "date": "2025-07-02",
              "earnings": 89.97,
              "transactions": 2,
              "newStudents": 2
            }
          ],
          "coursesPerformance": [
            {
              "courseId": "course-uuid-1",
              "courseTitle": "Advanced Spring Boot",
              "earnings": 899.75,
              "salesCount": 12,
              "conversionRate": 15.5,
              "averageRating": 4.8,
              "enrollmentTrend": "increasing"
            }
          ],
          "paymentMethods": [
            {
              "method": "stripe",
              "earnings": 1999.89,
              "percentage": 81.3,
              "transactionCount": 24
            },
            {
              "method": "paypal",
              "earnings": 459.83,
              "percentage": 18.7,
              "transactionCount": 4
            }
          ],
          "studentGeography": [
            {
              "country": "United States",
              "earnings": 1200.45,
              "studentCount": 15,
              "percentage": 48.8
            },
            {
              "country": "United Kingdom",
              "earnings": 650.3,
              "studentCount": 8,
              "percentage": 26.4
            }
          ]
        },
        "insights": [
          "Your earnings increased by 23% compared to last month",
          "Advanced Spring Boot is your top-performing course",
          "Peak sales occur on weekends"
        ]
      }
    }
    ```
- **Controller:** Add a `getEarningsAnalytics` endpoint to `InstructorEarningController`.
- **Service:** Implement analytics calculations in `InstructorEarningService`.
- **Repository:** Create complex analytical queries with joins across multiple tables.
- **Security:** Requires `INSTRUCTOR` role.
- **Testing:** Test analytics data accuracy and performance with large datasets.

### 4.23. `POST /api/instructor/earnings/withdraw-request`

- **Description:** Creates a withdrawal request for available earnings.
- **Request:**
  - **Method:** `POST`
  - **Path:** `/api/instructor/earnings/withdraw-request`
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Body:**
    ```json
    {
      "amount": 500.0,
      "paymentMethod": "bank_transfer",
      "bankDetails": {
        "accountNumber": "1234567890",
        "routingNumber": "021000021",
        "accountHolderName": "John Doe",
        "bankName": "Example Bank"
      },
      "notes": "Monthly withdrawal request"
    }
    ```
- **Response:**
  - **Success (201 Created):**
    ```json
    {
      "statusCode": 201,
      "message": "Withdrawal request submitted successfully",
      "data": {
        "id": "withdrawal-uuid-1",
        "amount": 500.0,
        "status": "PENDING",
        "requestedAt": "2025-07-26T10:30:00Z",
        "estimatedProcessingTime": "3-5 business days",
        "paymentMethod": "bank_transfer",
        "trackingNumber": "WD2025072601"
      }
    }
    ```
- **Business Rules:**
  - Only `AVAILABLE` earnings can be withdrawn
  - Minimum withdrawal amount (e.g., $50)
  - Maximum withdrawal per month (e.g., $10,000)
  - Instructor must have verified payment details
- **Controller:** Add a `requestWithdrawal` endpoint to `InstructorEarningController`.
- **Service:** Implement withdrawal validation and request creation in `InstructorEarningService`.
- **Security:** Requires `INSTRUCTOR` role and verified account status.
- **Testing:** Test withdrawal validation rules and request creation.

### 4.24. `GET /api/instructor/earnings/withdrawals`

- **Description:** Retrieves a list of withdrawal requests made by the instructor.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/instructor/earnings/withdrawals`
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Query Params:**
    - `page` (number, optional, default: 0): Page number
    - `size` (number, optional, default: 10): Items per page
    - `status` (string, optional): Filter by status (`PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`)
- **Response:**
  - **Success (200 OK):** (Standard Paginated Response)
    ```json
    {
      "statusCode": 200,
      "message": "Withdrawal requests retrieved successfully",
      "data": {
        "content": [
          {
            "id": "withdrawal-uuid-1",
            "amount": 500.0,
            "status": "COMPLETED",
            "requestedAt": "2025-07-20T10:30:00Z",
            "processedAt": "2025-07-23T15:45:00Z",
            "paymentMethod": "bank_transfer",
            "trackingNumber": "WD2025072001",
            "processingFee": 2.5,
            "netAmount": 497.5
          }
        ],
        "page": 0,
        "size": 10,
        "totalPages": 2,
        "totalElements": 15
      }
    }
    ```
- **Controller:** Add a `getWithdrawals` endpoint to `InstructorEarningController`.
- **Service:** Implement withdrawal history retrieval in `InstructorEarningService`.
- **Security:** Requires `INSTRUCTOR` role.
- **Testing:** Test withdrawal history pagination and filtering.

### 4.25. `GET /api/instructor/earnings/tax-documents`

- **Description:** Generates and retrieves tax documents for instructor earnings (1099 forms, earning statements).
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/instructor/earnings/tax-documents`
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Query Params:**
    - `year` (number, required): Tax year (e.g., 2025)
    - `format` (string, optional, default: `pdf`): Document format (`pdf`, `csv`)
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Tax documents generated successfully",
      "data": {
        "year": 2025,
        "documents": [
          {
            "type": "1099-NEC",
            "description": "Nonemployee Compensation",
            "url": "https://res.cloudinary.com/.../1099-nec-2025.pdf",
            "generatedAt": "2025-07-26T10:30:00Z",
            "validUntil": "2026-01-31T23:59:59Z"
          },
          {
            "type": "earning-summary",
            "description": "Annual Earnings Summary",
            "url": "https://res.cloudinary.com/.../earnings-summary-2025.pdf",
            "generatedAt": "2025-07-26T10:30:00Z",
            "validUntil": "2026-01-31T23:59:59Z"
          }
        ],
        "summary": {
          "totalEarnings": 15750.45,
          "totalWithdrawn": 12000.0,
          "pendingAmount": 3750.45,
          "transactionCount": 156,
          "coursesCount": 8
        }
      }
    }
    ```
- **Controller:** Add a `getTaxDocuments` endpoint to `InstructorEarningController`.
- **Service:** Implement tax document generation in `InstructorEarningService`.
- **Business Logic:**
  - Generate PDF documents with official formatting
  - Include all required tax information
  - Store generated documents securely
  - Implement document expiration and regeneration
- **Security:** Requires `INSTRUCTOR` role and document access validation.
- **Testing:** Test document generation and security access controls.

### 4.26. `GET /api/instructor/earnings/export`

- **Description:** Exports earnings data in various formats for accounting and reporting purposes.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/instructor/earnings/export`
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Query Params:**
    - `format` (string, required): Export format (`csv`, `excel`, `pdf`)
    - `dateFrom` (string, optional): Start date for export (ISO format)
    - `dateTo` (string, optional): End date for export (ISO format)
    - `status` (string, optional): Filter by earning status
    - `courseId` (string, optional): Filter by specific course
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Earnings data exported successfully",
      "data": {
        "exportId": "export-uuid-1",
        "format": "csv",
        "filename": "instructor-earnings-2025-07.csv",
        "downloadUrl": "https://res.cloudinary.com/.../earnings-export.csv",
        "generatedAt": "2025-07-26T10:30:00Z",
        "validUntil": "2025-07-29T10:30:00Z",
        "recordCount": 156,
        "fileSize": "24.5 KB"
      }
    }
    ```
- **Controller:** Add an `exportEarnings` endpoint to `InstructorEarningController`.
- **Service:** Implement data export functionality in `InstructorEarningService`.
- **Features:**
  - Support multiple export formats (CSV, Excel, PDF)
  - Include filtering and date range options
  - Generate downloadable files with expiration
  - Include summary statistics in exports
- **Security:** Requires `INSTRUCTOR` role.
- **Testing:** Test export functionality with different formats and filters.

### 4.27 `POST /api/instructor/quizzes`

- **Description:** Creates a new quiz for a course owned by the instructor.
- **Request:**
  - **Method:** `POST`
  - **Path:**`/api/instructor/quizzes`
  - \*\*Headers
  - \*\*Authorization
  - \*\*Bearer `<accessToken>`
  - **Body (`CreateQuizDto`):**
    ```json
    {
      "title": "Quiz Title",
      "description": "Quiz Description",
      "lessonId": "lesson-uuid",
      "questions": [
        {
          "questionText": "What is Spring Boot?",
          "options": {
            "A": "framework",
            "B": "library",
            "C": "language",
            "D": "database"
          },
          "correctAnswer": "A",
          "explanation": "Spring Boot is a framework for building Java applications."
        },
        {
          "questionText": "What is Spring Boot?",
          "options": {
            "A": "framework",
            "B": "library",
            "C": "language",
            "D": "database"
          },
          "correctAnswer": "A",
          "explanation": "Spring Boot is a framework for building Java applications."
        }
      ]
    }
    ```
- **Response:**
  - **Success (201 Created):**
    ```json
    {
      "statusCode": 201,
      "message": "Quiz created successfully",
      "data": {
        "id": "quiz-uuid",
        "title": "Quiz Title",
        "description": "Quiz Description",
        "courseId": "course-uuid",
        "questions": [
          {
            "id": "question-uuid-1",
            "questionText": "What is Spring Boot?",
            "options": {
              "A": "framework",
              "B": "library",
              "C": "language",
              "D": "database"
            },
            "correctAnswer": "A",
            "explanation": "Spring Boot is a framework for building Java applications."
          }
        ],
        "createdAt": "2025-07-26T10:30:00Z"
      }
    }
    ```
- **Controller:** Create `InstructorQuizController` with a `createQuiz` endpoint.
- **Service:** Implement `createQuiz` in `QuizService`.
- **Business Rules:**
  - Only instructors can create quizzes for their courses.
  - Each quiz must have a title, description, and at least one question.
  - Questions must include options and a correct answer.
- **Security:** Requires `INSTRUCTOR` role and course ownership.

### 4.28 `POST /api/instructor/lessons/with-quiz`

- **Description:** Allows instructors to create a new lesson and attach a quiz (with questions) in a single API call. Typically used when the instructor builds a lesson that requires immediate assessment.
- **Request:**
  - **Method:** `POST`
  - **Path:** `/api/instructor/lessons/with-quiz`
  - **Headers:**
    - `Authorization: Bearer <accessToken>`
  - **Body (`CreateLessonWithQuizDto`):**
    ```json
    {
      "title": "Lesson Title",
      "description": "Lesson Description",
      "type": "QUIZ",
      "createdAt": "2025-07-26T10:30:00Z",
      "quiz": {
        "title": "Quiz Title",
        "description": "Quiz Description",
        "questions": [
          {
            "questionText": "What is Spring Boot?",
            "options": {
              "A": "framework",
              "B": "library",
              "C": "language",
              "D": "database"
            },
            "correctAnswer": "A",
            "explanation": "Spring Boot is a framework for building Java applications."
          }
        ]
      }
    }
    ```
- **Response:**
  - **Success (201 Created):**
    ```json
    {
      "statusCode": 201,
      "message": "Lesson and quiz created successfully",
      "data": {
        "lesson": {
          "id": "lesson-uuid",
          "title": "Lesson Title",
          "description": "Lesson Description",
          "quizId": "quiz-uuid",
          "createdAt": "2025-07-26T10:30:00Z"
        },
        "quiz": {
          "id": "quiz-uuid",
          "title": "Quiz Title",
          "description": "Quiz Description",
          "questions": [
            {
              "id": "question-uuid-1",
              "questionText": "What is Spring Boot?",
              "options": {
                "A": "framework",
                "B": "library",
                "C": "language",
                "D": "database"
              },
              "correctAnswer": "A",
              "explanation": "Spring Boot is a framework for building Java applications."
            }
          ]
        }
      }
    }
    ```
- **Controller:** Create `InstructorLessonController` with a `createLessonWithQuiz` endpoint.
- **Service:** Implement `createLessonWithQuiz` in `LessonService`.
- **Business Rules:**
  - Only instructors can create lessons with quizzes.
  - Each lesson must have a title and description.
  - The quiz must have a title, description, and at least one question.
  - Questions must include options and a correct answer.
    title and description of lesson and quiz must be non-empty.
  - At least one valid question is required.
  - Each question must:
    - Include all 4 options (A to D).
    - Have a correctAnswer that matches one of the options.
    - Include an explanation.
  - The operation is transactional — if any part fails, nothing is saved.
- **Security:** Requires INSTRUCTOR role, Instructor must own the course/section identified by sectionId.

### 4.28 `PUT /api/instructor/sections/:sectionId/lessons/:lessonId/quiz`

- **Description:** Updates (replaces) the quiz associated with a specific lesson owned by the instructor.
  ⚠️ This is a full replacement — questions not included in the request will be deleted.
- **Request:**

  - **Method:** `PUT`
  - **Path:** `/api/instructor/sections/:sectionId/lessons/:lessonId/quiz`
  - **Headers:**
    - `Authorization: Bearer <accessToken>`
  - **Body (`UpdateQuizDto`):**

    ```json
    {
      "questions": [
        {
          "id": "question-uuid-1",
          "questionText": "What is Spring Boot?",
          "options": {
            "A": "framework",
            "B": "library",
            "C": "language",
            "D": "database"
          },
          "correctAnswer": "A",
          "explanation": "Spring Boot is a framework for building Java applications."
        }
      ]
    }
    ```

- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Quiz updated successfully",
      "data": {
        "questions": [
          {
            "id": "question-uuid-1",
            "questionText": "What is Spring Boot?",
            "options": {
              "A": "framework",
              "B": "library",
              "C": "language",
              "D": "database"
            },
            "correctAnswer": "A",
            "explanation": "Spring Boot is a framework for building Java applications."
          }
        ],
        "updatedAt": "2025-07-26T10:30:00Z"
      }
    }
    ```
- **Controller:** Create `InstructorQuizController` with an `updateQuiz` endpoint.
- **Service:** Implement `updateQuiz` in `QuizService`.
- **Business Rules:**
  Questions:

  - Must be between 1 and 20 per request.
  - Each question must:
    - Have non-empty questionText.
    - Have an options object with exactly 4 keys: A, B, C, and D.
    - Have correctAnswer in ["A", "B", "C", "D"].
    - explanation is optional but recommended for educational feedback.

- **Security:** Requires `INSTRUCTOR` role and section ownership.
- **Testing:** Test quiz update with valid and invalid inputs.

### 4.29 `PUT /api/student/sections/:sectionId/lessons/:lessonId/submit`

- **Description:** Submits quiz answers for a specific lesson. If a result already exists for the current user and lesson, it will be overwritten (overridden).
- **Request:**
  - **Method:** `PUT`
  - **Path:** `/api/student/sections/:sectionId/lessons/:lessonId/submit`
  - **Headers:**
    - `Authorization: Bearer <accessToken>`
  - **Body:**
    ```json
    {
      "answers": {
        "questionId-1": "A",
        "questionId-2": "D",
        "questionId-3": "B"
      }
    }
    ```
  - **Path Params:**
    - `sectionId` (string): The ID of the section containing the lesson.
    - `lessonId` (string): The ID of the lesson being submitted.
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Quiz submitted successfully",
      "data": {
        "score": 85,
        "totalQuestions": 10,
        "correctAnswers": 8,
        "feedback": "Great job! You answered 8 out of 10 questions correctly.",
        "submittedAt": "2025-07-26T10:30:00Z"
      }
    }
    ```
- **Controller:** Create `StudentLessonController` with a `submitQuiz` endpoint.
- **Service:** Implement `submitQuiz` in `LessonService`.
- **Business Rules:**
  - Students can only submit quizzes for lessons they are enrolled in.
  - Answers must match the question IDs in the quiz.
  - Calculate score based on correct answers.
  - Provide feedback based on performance.
  - If the student has already submitted this quiz before:
    - The previous result will be overwritten with the new one.
      The system updates the existing record instead of creating a new one.
  - Only one result per user per lesson is stored.
- **Security:** Requires `STUDENT` role and enrollment in the section.

## 5. Admin Role

### 5.1. `GET /api/admin/users`

- **Description:** Retrieves a paginated list of all users.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/admin/users`
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**
  - **Success (200 OK):** (Standard Paginated Response)
    ```json
    {
      "statusCode": 200,
      "message": "Users retrieved successfully",
      "data": {
        "content": [],
        "page": 0,
        "size": 10,
        "totalPages": 1,
        "totalElements": 1
      }
    }
    ```
- **Controller:** Create `AdminUserController` with a `findAllUsers` endpoint.
- **Service:** Implement `findAllUsers` in `UserService`.
- **Security:** Requires `ADMIN` role.
- **Testing:** Test user listing with pagination.

### 5.2. `GET /api/admin/courses`

- **Description:** Retrieves a list of courses. Supports filtering by course status, pagination, and sorting
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/admin/courses`
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Query Params:**
    - `page` (number, optional, default: 0): Page number for pagination
    - `size` (number, optional, default: 10): Number of items per page
    - `status` (string, optional): Filter by course status (`PENDING`, `APPROVED`)
    - `sort` (string, optional, default: `createdAt,asc`): Sort criteria,
    - `categoryId` (string, optional): Filter by category ID
    - `search` (string, optional): Search by course title or description, instructor name
    - `minPrice` (number, optional): Filter by minimum course price
    - `maxPrice` (number, optional): Filter by maximum course price
    - `level` (string, optional): Filter by course level (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`)
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Courses retrieved successfully",
      "data": {
        "content": [
          {
            "id": "course-uuid",
            "title": "Course Title",
            "description": "Course Description",
            "thumbnailUrl": "https://res.cloudinary.com/.../course-thumb.jpg",
            "instructor": {
              "id": "instructor-uuid",
              "name": "Instructor Name",
              "email": "instructor@example.com",
              "avatar": "https://res.cloudinary.com/.../instructor-profile.jpg"
            },
            "isApproved": true,
            "isPublished": false,
            "level": "BEGINNER",
            "price": 49.99,
            "enrollmentCount": 100,
            "averageRating": 4.5,
            "ratingCount": 25,
            "sectionCount": 5,
            "category": {
              "id": "category-uuid",
              "name": "Category Name"
            },
            "createdAt": "2025-07-01T10:30:00Z",
            "updatedAt": "2025-07-02T12:00:00Z"
          }
        ],
        "page": {
          "number": 0,
          "size": 10,
          "totalPages": 1,
          "totalElements": 1,
          "first": true,
          "last": true
        }
      },
      "timeStamp": "2025-07-26T10:30:00Z"
    }
    ```
- **Business Rules:**
  - Admin can filter courses by status (PENDING, APPROVED).
  - Pagination and sorting are supported.
  - Course details include instructor information.
- **Controller:** Create `AdminCourseController` with a `findCoursesForAdmin ` endpoint.
- **Service:** Implement `findCoursesForAdmin ` to fetch all courses.
- **Security:** Requires `ADMIN` role.
- **Testing:** Test retrieval of all courses.

### 5.3. `PATCH /api/admin/courses/:id/approve`

- **Description:** Approves a pending course.
- **Request:**
  - **Method:** `PATCH`
  - **Path:** `/api/admin/courses/:id/approve`
  - **Path Params:** `id` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Course approved successfully",
      "data": {
        "id": "course-uuid",
        "title": "Course Title",
        "isApproved": true,
        "approvedAt": "2025-07-26T10:30:00Z"
      }
    }
    ```
- **Business Rules:**
  - Only courses in `PENDING` status can be approved.
  - Once approved, course status is set to `APPROVED`.
  - Admin ID and timestamp should be recorded.
- **Controller:** Add an `approveCourse` endpoint.
- **Service:** Implement `approveCourse` to set `is_approved` to true.
- **Security:** Requires `ADMIN` role.
- **Testing:**
  - Approve valid PENDING course
  - Try approving non-existent course
  - Try approving already approved course

### 5.10. `GET /api/admin/courses/:id`

- **Description:** Retrieves full metadata, sections, and lessons (with content) of a course for admin review.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/admin/courses/:id`
  - **Path Params:** `id` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Sections retrieved successfully",
      "data": [
        {
          "id": "section-uuid",
          "title": "Introduction",
          "order_index": 0,
          "lessonCount": 3,
          "lessons": [
            {
              "id": "lesson-uuid",
              "title": "Getting Started",
              "type": "VIDEO",
              "order_index": 0,
              "video": {
                "id": "video-uuid",
                "url": "https://res.cloudinary.com/.../video.mp4",
                "duration": 300
              }
            },
            {
              "id": "lesson-uuid-2",
              "title": "Course Overview",
              "type": "QUIZ",
              "order_index": 1,
              "quiz": {
                "questions": [
                  {
                    "id": "question-id-1",
                    "questionText": "What is Java?",
                    "options": [
                      "A. Language",
                      "B. Framework",
                      "C. OS",
                      "D. Compiler"
                    ],
                    "correctAnswer": "A",
                    "explanation": "Java is a programming language."
                  },
                  {
                    "id": "question-id-2",
                    "questionText": "What is JVM?",
                    "options": ["A", "B", "C", "D"],
                    "correctAnswer": "C",
                    "explanation": null
                  }
                ]
              }
            }
          ]
        },
        {
          "id": "section-uuid-2",
          "title": "Advanced Concepts",
          "order_index": 1,
          "lessonCount": 0,
          "lessons": []
        }
      ]
    }
    ```
- **Controller:** Create `AdminCourseController` with a `getSections` endpoint.
- **Service:** Implement `getSections` in `AdminCourseService`.
  - Verify the user is an admin.
  - Validate course exists.
  - Include course metadata, sections, and lessons.
- **Security:** Requires `ADMIN` role.
- **Testing:**:
  - Course with multiple sections and mixed content.
  - Course with no lessons.
  - Course not found (404).
  - Unauthorized access (401/403).
- **Error Handling:** If the course does not exist, return 404 Not Found.

### 5.4. `GET /api/admin/instructors/applications`

- **Description:** Retrieves all instructor applications.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/admin/instructors/applications`
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Instructor applications retrieved successfully",
      "data": []
    }
    ```
- **Controller:** Create `AdminInstructorApplicationController` with a `findAllApplications` endpoint.
- **Service:** Implement `findAllApplications` in `InstructorApplicationService`.
- **Security:** Requires `ADMIN` role.
- **Testing:** Test retrieval of applications.

### 5.5. `POST /api/admin/instructors/applications/:id/approve`

- **Description:** Approves an instructor application.
- **Request:**
  - **Method:** `POST`
  - **Path:** `/api/admin/instructors/applications/:id/approve`
  - **Path Params:** `id` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Instructor application approved",
      "data": null
    }
    ```
- **Controller:** Add an `approveApplication` endpoint.
- **Service:** Implement `approveApplication`.
- Update the application status to `APPROVED`.
- Assign the `INSTRUCTOR` role to the user in the `USER_ROLE` table.
- **Security:** Requires `ADMIN` role.
- **Testing:** Test the full application approval flow.

## 6. System Logs

### 6.1. `GET /api/admin/logs`

- **Description:** Retrieves system logs for monitoring and debugging purposes.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/admin/logs`
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "System logs retrieved successfully",
      "data": []
    }
    ```
- **Controller:** Create `AdminLogController` with a `getLogs` endpoint.
- **Service:** Implement `getLogs` in `LogService`.
- **Security:** Requires `ADMIN` role.
- **Testing:** Test retrieval of logs.
- **Error Handling:** If no logs are found, return an empty array.
- **Business Rules:**
  - Logs should include timestamps, log levels, and messages.
  - Support filtering by date range and log level (INFO, WARN, ERROR).

### 6.2 `POST /api/admin/logs`

- **Description:** Creates a new system log entry to track admin actions.
- **Request:**

  - **Method:** `POST`
  - **Path:** `/api/admin/logs`
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Body:**

    ```json
    {
      "userId": "user-uuid",
      "action": "CREATE",
      "entityType": "COURSE",
      "entityId": "course-uuid",
      "oldValues": {
        "title": "Old Title"
      },
      "newValues": {
        "title": "New Course Title",
        "description": "Updated course description"
      }
    }
    ```

    **Note**:

  - userId is required (system_logs.user column is non-nullable).
  - action must be one of: "CREATE", "UPDATE", "DELETE".
  - oldValues and newValues must be valid JSON objects (or null).

- **Response:**
  ```json
  {
    "statusCode": 201,
    "message": "Log entry created successfully",
    "data": {
      "id": "log-entry-id"
    }
  }
  ```
- **Controller:** Create `AdminLogController` with a `createLog` endpoint.
- **Service:** Implement `createLog` in `LogService`.
- **Security:** Requires `ADMIN` role.
- **Testing:** Test log creation.
- **Error Handling:** If the log entry is invalid, return a 400 Bad Request with error details. 403 Forbidden: Invalid token or user does not have ADMIN role. 500 Internal Server Error: Internal server/database error.
