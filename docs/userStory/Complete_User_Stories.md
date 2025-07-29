# Complete User Stories Document

## Table of Contents

1. [General Platform Features](#general-platform-features)
2. [Student User Stories](#student-user-stories)
3. [Instructor User Stories](#instructor-user-stories)
4. [Admin User Stories](#admin-user-stories)

---

## General Platform Features

### **1. Global Components**

**User Story**:  
As a user, I want to interact with a consistent interface across the platform, so that I can navigate easily and have a unified experience.

**Acceptance Criteria**:

- The platform must have a designed Header visible across all pages.
- The platform must have a designed Footer visible across all pages.

**Priority**: Medium

---

### **2. Landing Page**

**User Story**:  
As a visitor, I want to explore the landing page to get an overview of the platform, so that I can decide to engage with the courses offered.

**Acceptance Criteria**:

- The landing page must include a HeroSection with an image, slogan, and CTA button.
- The page must display a top courses list using CourseList.
- The page must include mock data sections for introduction, feedback, and partners if available.

**Priority**: High

---

### **3. Course Catalog**

**User Story**:  
As a learner, I want to browse the course catalog with filtering and pagination, so that I can find and select courses that match my interests.

**Acceptance Criteria**:

- The course catalog must feature a sidebar filter for category, price, and rating.
- The page must display a CourseList in grid format showing course details (image, name, instructor, rating, price).
- The page must include pagination.
- The page must show loading, empty, and error states.
- The design must be responsive.

**Priority**: High

---

### **4. Course Detail**

**User Story**:  
As a learner, I want to view detailed course information, so that I can decide to enroll or buy the course.

**Acceptance Criteria**:

- The course detail page must display a CourseHeader with image, name, instructor, rating, price, and Enroll/Buy button.
- The page must include CourseContent with curriculum (sections, lessons, preview).
- The page must show an InstructorProfile.
- The page must display a ReviewsList with average rating.
- The page must handle enrolled/unenrolled states.
- The design must be responsive.

**Priority**: High

---

### **5. Checkout Page**

**User Story**:  
As a learner, I want to complete the checkout process smoothly, so that I can purchase a course securely.

**Acceptance Criteria**:

- The checkout page must show an OrderSummary with course info, price, and VAT.
- The page must include a PaymentForm for payment details with Stripe integration (or mock).
- The page must handle API calls to create payment and process responses.
- The page must display loading, error, and success states.
- The page must show a notification after successful payment.

**Priority**: High

---

### **6. Search Page**

**User Story**:  
As a learner, I want to search for courses using keywords, so that I can quickly find relevant content.

**Acceptance Criteria**:

- The search page must have a SearchInput for keyword entry and trigger search.
- The page must display results in a CourseList.
- The page must handle API calls for course search.
- The page must show loading, empty, and error states.

**Priority**: Medium

---

### **7. Authentication - Login Page**

**User Story**:  
As a user, I want to log in to the platform, so that I can access my personalized content and features.

**Acceptance Criteria**:

- The login page must include a form for email, password, and remember me option.
- The form must validate email format and require password.
- The page must handle API login with JWT session storage.
- The page must display errors for failed logins.
- The page must link to register and forgot password pages.

**Priority**: High

---

### **8. Authentication - Register Page**

**User Story**:  
As a new user, I want to register on the platform, so that I can create an account and start using the services.

**Acceptance Criteria**:

- The register page must include a form for name, email, password, confirm password, and role selection (student/instructor).
- The form must validate email uniqueness, strong password, and confirm password match.
- The page must handle API registration and process responses.
- The page must display errors for duplicate emails or weak passwords.
- The page must redirect on successful registration.

**Priority**: High

---

### **9. Authentication - Forgot Password**

**User Story**:  
As a user, I want to reset my password if forgotten, so that I can regain access to my account.

**Acceptance Criteria**:

- The forgot password page must include a form for email input.
- The page must handle API calls to send a reset email.
- The page must display success or failure notifications.

**Priority**: Medium

---

### **10. Authentication - Reset Password**

**User Story**:  
As a user, I want to set a new password using a reset link, so that I can secure my account.

**Acceptance Criteria**:

- The reset password page must include a form for new password and confirm password.
- The form must validate strong password and confirm password match.
- The page must handle API reset with token.
- The page must display success or failure notifications.

**Priority**: Medium

---

### **11. User Profile Page**

**User Story**:  
As a user, I want to view and update my profile information, so that I can keep my details current.

**Acceptance Criteria**:

- The profile page must display user info (avatar, name, email).
- The page must allow form updates for name and email with uniqueness validation.
- The page must handle API updates.
- The page must show success or error notifications.

**Priority**: Medium

---

### **12. User Settings Page**

**User Story**:  
As a user, I want to manage my account settings, so that I can customize my profile and security.

**Acceptance Criteria**:

- The settings page must include tabs for password and avatar changes.
- The password change must include a form with old, new, and confirm fields, with validation, and API call.
- The avatar change must allow upload with preview and API call.
- The page must show success or error notifications.

**Priority**: Medium

---

## Student User Stories

### **13. Student Dashboard Overview**

**User Story**:  
As a student, I want to see an overview of my dashboard, so that I can monitor my learning progress and activities.

**Acceptance Criteria**:

- The dashboard must include sidebar navigation.
- The page must show a DashboardHeader with user greeting and progress overview.
- The page must display an EnrolledCoursesSummary and ActivityFeed.
- The page must handle API calls for enrolled courses, progress, and activity.

**Priority**: High

---

### **14. Student My Courses**

**User Story**:  
As a student, I want to view my enrolled courses, so that I can continue my learning.

**Acceptance Criteria**:

- The page must display a list of enrolled courses with names, progress, and continue buttons.
- The page must handle API calls for enrolled courses.

**Priority**: High

---

### **15. Learning Page**

**User Story**:  
As a student, I want to access and complete course lessons, so that I can advance my learning.

**Acceptance Criteria**:

- The learning page must include a sidebar with section and lesson list.
- The page must feature a VideoPlayer for lesson videos.
- The page must show a LessonList within the section.
- The page must display a CourseProgressBar.
- The page must include a "Mark as complete" button with API call to update progress.
- If a quiz is available, it must display the quiz, allow submission, and show results.
- The page must handle API calls for curriculum, lesson content, progress, and quiz.

**Priority**: High

---

### **16. Student Payments**

**User Story**:  
As a student, I want to view my payment history, so that I can track my transactions.

**Acceptance Criteria**:

- The payments page must display a PaymentsTable with transaction details, status, amount, and date.
- The page must handle API calls for payment history.

**Priority**: Medium

---

### **17. Student Quiz Results**

**User Story**:  
As a student, I want to review my quiz results, so that I can assess my performance.

**Acceptance Criteria**:

- The quiz results page must show a QuizResultsList with scores and statuses.
- The page must allow detailed result views for each quiz.
- The page must handle API calls for quiz results.

**Priority**: Medium

---

### **18. Student Reviews**

**User Story**:  
As a student, I want to manage my course reviews, so that I can share and update my feedback.

**Acceptance Criteria**:

- The reviews page must display a ReviewsList with sent reviews and statuses.
- The page must allow edit and delete actions with API calls.
- The page must handle API calls for user reviews.

**Priority**: Medium

---

## Instructor User Stories

### **19. Instructor Dashboard**

**User Story**:  
As an instructor, I want to view my dashboard overview, so that I can monitor my teaching performance and earnings.

**Acceptance Criteria**:

- The instructor dashboard must include sidebar navigation.
- The page must show a DashboardHeader with greeting and overview of revenue and course count.
- The page must display a RevenueSummary and CourseStats.
- The page must handle API calls for earnings and stats.

**Priority**: High

---

### **20. Create Course**

**User Story**:  
As an Instructor, I want to create a new course with lessons and multimedia content, so that I can submit it for admin approval and offer it to learners.

**Acceptance Criteria**:

- Instructor can access the "Create Course" form from the dashboard.
- The form includes fields: title, description, category, image (required), intro video (optional), price.
- Instructor can add multiple lessons with video, quiz, and attachments.
- System validates inputs before proceeding.
- Upon submission, system sends approval request to admin.
- Instructor gets notified whether the course is approved or rejected (with reasons).

**Priority**: High

---

### **21. View Course List**

**User Story**:  
As an Instructor, I want to view a list of my created courses, so that I can manage their status and performance.

**Acceptance Criteria**:

- Instructor can view all created courses in a summary format (title, description, status, student count, rating count, price, created date,...).
- If no courses are available, a message is shown.

**Priority**: Medium

---

### **22. View Course Details**

**User Story**:  
As an Instructor, I want to view the detailed content of a course, so that I can check or review the course structure and contents.

**Acceptance Criteria**:

- Instructor can access course details including basic information and lessons.

**Priority**: Medium

---

### **23. Search Course**

**User Story**:  
As an Instructor, I want to search for my courses using filters, so that I can find a specific course efficiently.

**Acceptance Criteria**:

- Instructor can search by course title, description or ID.
- Filters available: course status, category, rating, price, created date.
- Results are displayed accordingly.
- No results or invalid input triggers appropriate alerts.

**Priority**: Medium

---

### **24. Edit Course**

**User Story**:  
As an Instructor, I want to edit a course that has no enrolled students, so that I can update the content before publishing.

**Acceptance Criteria**:

- Instructor can update title, description, price, category and lessons.
- Changes are validated and submitted for admin re-approval (if previously approved).
- System displays success or failure messages.

**Priority**: High

---

### **25. Delete Course**

**User Story**:  
As an Instructor, I want to delete or hide a course under certain conditions, so that I can manage obsolete or invalid content.

**Acceptance Criteria**:

- Instructor can delete courses that are not approved and have no students.
- The system asks for confirmation before deletion.
- If the course has been approved and no enrolled students, it becomes hidden.
- If the course has enrolled students then it is marked as "Discontinued". Who are already enrolled can still access the content.

**Priority**: Medium

---

### **26. Manage Content**

**User Story**:  
As an instructor, I want to manage course content, so that I can structure and update my lessons.

**Acceptance Criteria**:

- The manage content page must include a ContentEditor for section and lesson management (add, edit, delete, reorder).
- The page must allow adding video lessons and quiz lessons.
- The page must support uploading video or documents (mock or integrated).
- The page must handle API calls for CRUD and uploads.

**Priority**: High

---

### **27. View Student List**

**User Story**:  
As an Instructor, I want to view the list of students enrolled in my courses, so that I can track and support their learning.

**Acceptance Criteria**:

- Instructor can view a list of students in the "Students" section.
- If no students are enrolled, a notification is shown.

**Priority**: Medium

---

### **28. View Student Details**

**User Story**:  
As an Instructor, I want to view the profile and progress of a student, so that I can understand their learning engagement.

**Acceptance Criteria**:

- Clicking a student name opens their detailed profile.

**Priority**: Medium

---

### **29. Search Student**

**User Story**:  
As an Instructor, I want to search for students using name, email, or ID, so that I can find and interact with specific learners.

**Acceptance Criteria**:

- Instructor can enter search queries in a search box.
- Matching students are listed.
- The system shows a message if no result is found.

**Priority**: Low

---

### **30. Student Progress Monitoring**

**User Story**:  
As an instructor, I want to monitor student progress, so that I can support their learning.

**Acceptance Criteria**:

- The student progress page must display a ProgressReport for each student.
- The page must handle API calls for progress data.

**Priority**: Medium

---

### **31. Message Students**

**User Story**:  
As an Instructor, I want to send messages to my students, so that I can provide guidance and respond to their questions.

**Acceptance Criteria**:

- The instructor can access the messaging interface.
- The instructor selects a student, writes a message, and sends it.
- The system shows sent messages to both parties.
- Connection issues display an error message.

**Priority**: Medium

---

### **32. Send Email to Students**

**User Story**:  
As an Instructor, I want to send emails to one or more students, so that I can communicate important updates or information.

**Acceptance Criteria**:

- Instructor can select multiple verified students.
- Email includes a subject and body.
- The system processes the email and shows delivery status.
- If the email fails, the system shows an error message.

**Priority**: Medium

---

### **33. View Revenue Statistics**

**User Story**:  
As an Instructor, I want to see revenue statistics from my courses, so that I can evaluate my financial performance.

**Acceptance Criteria**:

- The instructor can access the earnings page.
- Statistics shown include: total revenue, monthly revenue, revenue per course.
- If no data is available, the system shows a notice.

**Priority**: High

---

### **34. Withdraw Earnings**

**User Story**:  
As an Instructor, I want to withdraw available earnings, so that I can receive the money I've earned from course sales.

**Acceptance Criteria**:

- The instructor can enter a withdrawal amount.
- The system checks if the account is configured and balance is sufficient.
- The system processes withdrawal and provides status.

**Priority**: High

---

### **35. View Transaction History**

**User Story**:  
As an Instructor, I want to see my payout transaction history, so that I can track past withdrawals and income.

**Acceptance Criteria**:

- Instructor can view a list of past transactions with amount, date, and status.
- If no transactions exist, the system shows a message.

**Priority**: Low

---

### **36. Instructor Earnings Management**

**User Story**:  
As an instructor, I want to view my earnings, so that I can manage my finances.

**Acceptance Criteria**:

- The earnings page must include an EarningsChart for revenue over time.
- The page must show a PayoutsTable with payout history and statuses.
- The page must handle API calls for earnings and payout history.

**Priority**: Medium

---

### **37. Receive Notification When Student Asks a Question**

**User Story**:  
As an Instructor, I want to be notified when a student asks a question, so that I can respond promptly and support their learning.

**Acceptance Criteria**:

- The system show notifications in the "Announcements" section.
- Clicking the notification opens the Q&A page.
- If there are system issues, the event is saved and resent later.

**Priority**: Medium

---

## Admin User Stories

### **38. Admin Dashboard**

**User Story**:  
As an admin, I want to manage the platform's users, courses, instructors, payments, and refunds, so that I can ensure smooth operation.

**Acceptance Criteria**:

- The admin dashboard must initialize a React project with base configuration.
- The dashboard must include StatCards (users, courses, revenue) and Charts.
- The page must feature a UserTable for user management (ban, edit, assign roles) with API calls.
- The page must include a CourseApprovalQueue for course approval/rejection with API calls.
- The page must show an ApplicationList and ApplicationDetailView for instructor applications with approval/rejection options and API calls.
- The page must display a PaymentsTable with API calls.
- The page must include a RefundQueue for refund processing with API calls.

**Priority**: High

---
