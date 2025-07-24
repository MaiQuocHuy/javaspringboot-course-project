---
applyTo: "**"
---

# KTC Learning Platform - Backend Task List

This document breaks down the development tasks for each API endpoint in the backend system.

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

### 4.3. `POST /api/instructor/courses/:courseId/sections`

- **Description:** Creates a new section in a course.
- **Request:**
  - **Method:** `POST`
  - **Path:** `/api/instructor/courses/:courseId/sections`
  - **Path Params:** `courseId` (string)
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
        "id": "string",
        "title": "string"
      }
    }
    ```
- **Controller:** Create `InstructorSectionController` with a `createSection` endpoint.
- **Service:** Implement `createSection` in `SectionService`.
- **Security:** Requires `INSTRUCTOR` role and course ownership.
- **Testing:** Test section creation.

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

---

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

### 5.2. `GET /api/admin/courses/pending`

- **Description:** Retrieves all courses pending approval.
- **Request:**
  - **Method:** `GET`
  - **Path:** `/api/admin/courses/pending`
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Pending courses retrieved successfully",
      "data": []
    }
    ```
- **Controller:** Create `AdminCourseController` with a `findPendingCourses` endpoint.
- **Service:** Implement `findPendingCourses` to fetch courses where `is_approved` is false.
- **Security:** Requires `ADMIN` role.
- **Testing:** Test retrieval of pending courses.

### 5.3. `POST /api/admin/courses/:id/approve`

- **Description:** Approves a pending course.
- **Request:**
  - **Method:** `POST`
  - **Path:** `/api/admin/courses/:id/approve`
  - **Path Params:** `id` (string)
  - **Headers:** `Authorization: Bearer <accessToken>`
- **Response:**
  - **Success (200 OK):**
    ```json
    {
      "statusCode": 200,
      "message": "Course approved successfully",
      "data": null
    }
    ```
- **Controller:** Add an `approveCourse` endpoint.
- **Service:** Implement `approveCourse` to set `is_approved` to true.
- **Security:** Requires `ADMIN` role.
- **Testing:** Test course approval.

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
