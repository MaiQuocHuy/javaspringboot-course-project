# KTC Learning Platform - System Design

This document outlines the architectural and technical design for the KTC Learning Platform, based on the established requirements and database schema.

## 1. System Architecture

We will adopt a **Monolithic Architecture** for the backend, which will be a single, comprehensive application serving a RESTful API. The frontend will be a separate **Single-Page Application (SPA)** that communicates with the backend API.

- **Backend:** A Java application built with the **Spring Boot** framework. This choice provides a robust, scalable, and widely-supported ecosystem for building enterprise-grade applications.
- **Frontend:** A **Next.js** application for a modern, server-side rendered user experience, which is beneficial for SEO and initial page load performance.
- **Database:** A **MySQL** database, as defined in `database.md`.

This architecture is straightforward to develop, deploy, and maintain for a project of this scope, while still offering a clear separation of concerns between the frontend and backend.

## 2. API Design

The API will be versioned (e.g., `/api/v1/...`) and will follow RESTful principles. It will use JSON for all request and response bodies.

### 2.1. Authentication & User Management

- `POST /api/auth/register`: Register a new user.
- `POST /api/auth/login`: Log in and receive JWT tokens.
- `POST /api/auth/refresh`: Obtain a new access token using a refresh token.
- `POST /api/auth/forgot-password`: Request a password reset link.
- `POST /api/auth/reset-password`: Reset the password using a token.
- `GET /api/users/profile`: Get the current user's profile.
- `PUT /api/users/profile`: Update the current user's profile.

### 2.2. Course & Content (Public)

- `GET /api/courses`: Browse all published courses.
- `GET /api/courses/:id`: View details of a specific course.
- `GET /api/categories`: List all course categories.

### 2.3. Student Role

- `POST /api/courses/:id/enroll`: Enroll in a course (requires payment for non-free courses).
- `GET /api/enrollments/my-courses`: View all enrolled courses.
- `GET /api/courses/:courseId/progress`: View progress for an enrolled course.
- `POST /api/lessons/:id/complete`: Mark a lesson as completed.
- `POST /api/courses/:id/reviews`: Submit a review for a course.
- `PUT /api/reviews/:id`: Update a student's own review.

### 2.4. Instructor Role

- `GET /api/instructor/courses`: View courses created by the instructor.
- `POST /api/instructor/courses`: Create a new course.
- `PUT /api/instructor/courses/:id`: Update a course.
- `POST /api/instructor/courses/:courseId/sections`: Create a new section.
- `PUT /api/instructor/sections/:id`: Update a section.
- `POST /api/instructor/sections/:sectionId/lessons`: Create a new lesson.
- `PUT /api/instructor/lessons/:id`: Update a lesson.
- `POST /api/instructor/upload/video`: Upload video content for a lesson.

### 2.5. Admin Role

- `GET /api/admin/users`: Manage all users.
- `GET /api/admin/courses/pending`: View courses pending approval.
- `POST /api/admin/courses/:id/approve`: Approve a course.
- `POST /api/admin/courses/:id/reject`: Reject a course.
- `GET /api/admin/instructors/applications`: Manage instructor applications.
- `POST /api/admin/instructors/applications/:id/approve`: Approve an instructor application.

## 3. Database Design

The database design is detailed in the `database.md` file. It uses MySQL and features a relational structure with foreign key constraints to ensure data integrity.

## 4. Authentication and Authorization

- **Authentication:** The system will use JSON Web Tokens (JWT). Upon successful login, the API will issue an `accessToken` (short-lived, e.g., 15 minutes) and a `refreshToken` (long-lived, e.g., 7 days). The `accessToken` will be sent with each API request in the `Authorization` header. The `refreshToken` will be used to obtain a new `accessToken` when it expires.
- **Authorization:** A role-based access control (RBAC) mechanism will be implemented using **Spring Security**. API endpoints will be protected based on the user's role (`STUDENT`, `INSTRUCTOR`, `ADMIN`), which is encoded in the JWT payload.

### 4.1. Password Reset Flow

1.  **Request:** A user submits their email address to the `POST /api/auth/forgot-password` endpoint.
2.  **Token Generation:** The backend generates a secure, single-use, time-limited token (e.g., a UUID) and stores it, associating it with the user's account.
3.  **Email Notification:** The system sends an email to the user containing a link to the frontend's password reset page (e.g., `https://<frontend-url>/reset-password?token=<reset-token>`).
4.  **Verification:** The user clicks the link and is directed to a form to enter a new password.
5.  **Submission:** The frontend sends the new password along with the reset token to the `POST /api/auth/reset-password` endpoint.
6.  **Update:** The backend validates the token, updates the user's password with a new hash, and invalidates the reset token to prevent reuse.

### 4.2. Social Login (OAuth 2.0)

The system will support social logins (e.g., Google, GitHub) using the OAuth 2.0 protocol, integrated via Spring Security's OAuth2 client.

1.  **Initiation:** The user clicks a "Login with Google" button on the frontend, which redirects them to a backend endpoint like `GET /oauth2/authorization/google`.
2.  **Redirect to Provider:** Spring Security redirects the user to the provider's (Google's) authorization page.
3.  **User Consent:** The user logs in with their Google account and grants permission.
4.  **Callback:** Google redirects the user back to the application's configured callback URL (e.g., `/oauth2/callback/google`) with an authorization code.
5.  **Token Exchange & User Info:** Spring Security handles the callback, exchanges the code for an access token, and fetches the user's profile information from Google.
6.  **Account Handling:**
    - If a user with the social profile's email already exists, the social account is linked.
    - If no user exists, a new `USER` account is created.
7.  **JWT Issuance:** The backend issues its own JWT `accessToken` and `refreshToken` for the user, standardizing the session management regardless of the login method.

### 4.3. Brute-Force Protection

To mitigate brute-force attacks on the login endpoint, a request-limiting mechanism will be implemented.

- **Tracking:** Failed login attempts will be tracked per IP address or username. An in-memory cache (like Caffeine) or a distributed cache (like Redis) can be used for this.
- **Lockout:** After a certain number of consecutive failed attempts (e.g., 5), the account or IP will be temporarily locked out for a configurable duration (e.g., 10 minutes).

## 5. Key Feature Implementation Details

### 5.1. Course and Content Management

- Course and lesson management will be handled through dedicated services and controllers in the Spring Boot backend.
- Video content will be uploaded to a cloud storage service (e.g., AWS S3, Cloudinary) to avoid storing large files on the application server. The `VIDEO_CONTENT` table will store the URL and metadata.

### 5.2. Quiz System

- The `QUIZ_QUESTION` and `QUIZ_RESULT` tables will power the quiz functionality.
- When an instructor creates a quiz, the questions and options (stored as JSON) are saved.
- When a student submits a quiz, the backend will process the `answers` (JSON), compare them against the `correct_answer` for each question, calculate the `score`, and save it in the `QUIZ_RESULT` table.

### 5.3. Payment Flow

1.  A student initiates a payment for a course on the frontend.
2.  The frontend communicates with a payment gateway (e.g., Stripe, PayPal) to process the payment.
3.  Upon successful payment, the payment gateway sends a confirmation to the backend (via webhook or frontend callback).
4.  The backend creates a record in the `PAYMENT` table with a `COMPLETED` status.
5.  A new record is created in the `ENROLLMENT` table, granting the student access to the course.
6.  A record is created in the `INSTRUCTOR_EARNING` table to credit the instructor for the sale.

## 6. Technology Stack

- **Backend:**
  - **Framework:** Spring Boot
  - **Language:** Java
  - **Database ORM:** Spring Data JPA (with Hibernate)
  - **Authentication:** Spring Security
- **Frontend:**
  - **Framework:** Next.js
  - **Language:** TypeScript
  - **Styling:** Tailwind CSS or a component library like Shadcn/ui
- **Database:**
  - MySQL 8.0
- **Deployment:**
  - The backend and frontend applications can be containerized using **Docker** and deployed on a cloud provider like AWS, Vercel, or DigitalOcean.
