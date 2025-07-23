---
applyTo: "**"
---

# KTC Learning Platform Requirements

This document outlines the functional and non-functional requirements for the KTC Learning Platform, derived from the database schema.

## 1. Functional Requirements

### 1.1. User Management

- **R1.1.1:** The system shall allow users to register with a name, unique email, and a hashed password.
- **R1.1.2:** Users shall be assigned one or more roles: `STUDENT`, `INSTRUCTOR`, `ADMIN`.
- **R1.1.3:** The system shall support user authentication using JWT, including a refresh token mechanism for persistent sessions.
- **R1.1.4:** Administrators shall be able to activate or deactivate user accounts.
- **R1.1.5:** A user's profile information (name, email) shall be updatable.

### 1.2. Course Management

- **R1.2.1:** Instructors shall be able to create, update, and delete courses.
- **R1.2.2:** Courses shall have a title, description, price, and thumbnail.
- **R1.2.3:** Administrators shall be able to approve or reject courses submitted by instructors.
- **R1.2.4:** Instructors shall be able to publish or unpublish their approved courses.
- **R1.2.5:** The system shall support course categorization. A course can belong to multiple categories.
- **R1.2.6:** Administrators shall be able to create, update, and delete course categories.

### 1.3. Course Content Management

- **R1.3.1:** Instructors shall be able to structure a course by creating, updating, and deleting sections.
- **R1.3.2:** Sections shall contain one or more lessons, which can be reordered.
- **R1.3.3:** Instructors shall be able to create, update, and delete lessons within a section.
- **R1.3.4:** Lessons can be of different types, starting with `VIDEO` and `QUIZ`.
- **R1.3.5:** For video lessons, instructors shall be able to upload video content, including a URL and duration.

### 1.4. Student Enrollment and Learning Progress

- **R1.4.1:** Students shall be able to enroll in courses.
- **R1.4.2:** The system shall track a student's enrollment status (`IN_PROGRESS`, `COMPLETED`).
- **R1.4.3:** The system shall track a student's progress by recording completed lessons.

### 1.5. Reviews and Ratings

- **R1.5.1:** Enrolled students shall be able to submit a rating (1 to 5) and a text review for a course.
- **R1.5.2:** Students shall be able to update or delete their own reviews.
- **R1.5.3:** All users shall be able to view the reviews for a course.

### 1.6. Payment and Financial System

- **R1.6.1:** The system shall process payments for course enrollments.
- **R1.6.2:** Payments shall have a status (`PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`).
- **R1.6.3:** The system shall support refund requests for payments.
- **R1.6.4:** The system shall calculate and track earnings for instructors based on course sales.
- **R1.6.5:** Instructor earnings shall have a status (`PENDING`, `AVAILABLE`, `PAID`).

### 1.7. Instructor Application Process

- **R1.7.1:** Users shall be able to apply to become an instructor.
- **R1.7.2:** Applications shall include supporting documents.
- **R1.7.3:** Administrators shall be able to review, approve, or reject instructor applications.

### 1.8. Quiz System

- **R1.8.1:** Instructors shall be able to create quizzes for lessons.
- **R1.8.2:** A quiz shall consist of multiple questions, each with text, a set of options, a correct answer, and an optional explanation.
- **R1.8.3:** Students shall be able to take quizzes and submit their answers.
- **R1.8.4:** The system shall store the student's answers, calculate a score, and show the results.

### 1.9. Authentication

- **R1.9.1:** Users must be able to log in using their email and password.
- **R1.9.2:** The system must provide a "Forgot Password" feature that allows users to reset their password securely, for example, via an email link.
- **R1.9.3:** To prevent brute-force attacks, the system should temporarily lock a user's account after a configurable number of failed login attempts (e.g., 5 attempts).
- **R1.9.4:** The system should support social login options (e.g., Google, GitHub) to provide users with alternative, convenient ways to register and sign in.
- **R1.9.5:** The system must implement a "Remember Me" feature (e.g., using the refresh token) to keep users logged in across browser sessions if they choose to do so.

## 2. Non-Functional Requirements

### 2.1. Security

- **NFR2.1.1:** All user passwords must be securely hashed before being stored in the database.
- **NFR2.1.2:** The system must be protected against SQL injection attacks.
- **NFR2.1.3:** Access to different system functionalities must be restricted based on user roles (RBAC).

### 2.2. Data Integrity

- **NFR2.2.1:** Deleting a user should cascade to delete all their related data, such as roles, enrollments, and reviews.
- **NFR2.2.2:** Deleting a course should cascade to delete its associated content like sections, lessons, and enrollments.
- **NFR2.2.3:** If an instructor's user account is deleted, their courses should be disassociated (set instructor_id to NULL) but not deleted.

### 2.3. Performance

- **NFR2.3.1:** The database shall be indexed appropriately on frequently queried columns (e.g., user emails, course titles, foreign keys) to ensure fast data retrieval.

### 2.4. Scalability

- **NFR2.4.1:** The system should be designed to handle a growing number of users, courses, and concurrent activities.
