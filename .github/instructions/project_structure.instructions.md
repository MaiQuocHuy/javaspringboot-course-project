---
applyTo: "**"
---

# Spring Boot Course Management Project Structure

## Overview

This project follows SOLID principles and clean architecture patterns with a feature-based organization structure. Each feature is self-contained with its own controllers, services, repositories, and DTOs.

## Directory Structure

```
src/main/java/project/ktc/springboot_app/
├── SpringbootAppApplication.java           # Main application class
│
├── config/                                 # Configuration classes
│   ├── ApplicationConfig.java              # General application configuration
│   ├── DotenvConfig.java                  # Environment configuration
│   ├── OpenApiConfig.java                 # Swagger/OpenAPI configuration
│   └── SecurityConfig.java                # Security configuration
│
├── security/                               # Security-related classes
│   ├── JwtAuthenticationEntryPoint.java   # JWT authentication entry point
│   ├── JwtAuthenticationFilter.java       # JWT filter
│   └── JwtService.java                    # JWT utility service
│
├── auth/                                   # Authentication & Authorization
│   ├── controllers/                       # Auth controllers
│   ├── dto/                              # Auth DTOs
│   ├── entity/                           # Auth entities
│   ├── enums/                            # Auth enums
│   ├── interfaces/                       # Auth interfaces
│   ├── repositories/                     # Auth repositories
│   └── services/                         # Auth services
│
├── user/                                   # User management
│   ├── dto/                              # User DTOs
│   ├── entity/                           # User entities
│   ├── repository/                       # User repositories
│   └── service/                          # User services
│
├── refresh_token/                          # Refresh token management
│   └── repositories/                      # Refresh token repositories
│
├── entity/                                 # Core JPA entities
│   ├── BaseEntity.java                   # Base entity with common fields
│   ├── User.java                         # User entity
│   ├── Course.java                       # Course entity
│   ├── Category.java                     # Category entity
│   ├── Section.java                      # Section entity
│   ├── Lesson.java                       # Lesson entity
│   ├── VideoContent.java                 # Video content entity
│   ├── Enrollment.java                   # Enrollment entity
│   ├── LessonCompletion.java            # Lesson completion entity
│   ├── Review.java                       # Review entity
│   ├── Payment.java                      # Payment entity
│   ├── Refund.java                       # Refund entity
│   ├── InstructorEarning.java           # Instructor earning entity
│   ├── InstructorApplication.java       # Instructor application entity
│   ├── QuizQuestion.java                # Quiz question entity
│   ├── QuizResult.java                  # Quiz result entity
│   └── ...
│
├── repository/                             # Global repository interfaces
│   └── BaseRepository.java               # Base repository interface
│
├── service/                                # Global service layer
│   ├── interfaces/                       # Service interfaces (DIP compliance)
│   └── impl/                            # Service implementations
│
├── controller/                             # REST Controllers organized by access level
│   ├── public/                           # Public endpoints (no authentication)
│   │   ├── PublicCourseController.java   # Public course browsing
│   │   ├── PublicCategoryController.java # Public category browsing
│   │   └── PublicAuthController.java     # Login, register, etc.
│   │
│   ├── student/                          # Student role endpoints
│   │   ├── StudentCourseController.java  # Student course operations
│   │   ├── StudentEnrollmentController.java # Enrollment management
│   │   ├── StudentLessonController.java  # Lesson progress tracking
│   │   ├── StudentReviewController.java  # Course reviews
│   │   └── StudentPaymentController.java # Payment operations
│   │
│   ├── instructor/                       # Instructor role endpoints
│   │   ├── InstructorCourseController.java    # Course creation/management
│   │   ├── InstructorSectionController.java   # Section management
│   │   ├── InstructorLessonController.java    # Lesson management
│   │   ├── InstructorVideoController.java     # Video management
│   │   ├── InstructorQuizController.java      # Quiz management
│   │   ├── InstructorEarningController.java   # Earnings tracking
│   │   └── InstructorApplicationController.java # Application management
│   │
│   └── admin/                            # Admin role endpoints
│       ├── AdminUserController.java      # User management
│       ├── AdminCourseController.java    # Course approval/management
│       ├── AdminCategoryController.java  # Category management
│       ├── AdminPaymentController.java   # Payment oversight
│       ├── AdminRefundController.java    # Refund management
│       └── AdminInstructorController.java # Instructor application approval
│
├── dto/                                    # Global Data Transfer Objects
│   ├── request/                          # Request DTOs
│   │   ├── PaginationRequest.java        # Common pagination request
│   │   ├── SearchRequest.java            # Common search request
│   │   └── ...
│   │
│   └── response/                         # Response DTOs
│       ├── PaginatedResponse.java        # Common paginated response
│       ├── ApiResponse.java              # Standard API response wrapper
│       └── ...
│
├── course/                                 # Course management feature
│   ├── dto/                              # Course-specific DTOs
│   │   ├── CourseCreateRequest.java      # Course creation request
│   │   ├── CourseUpdateRequest.java      # Course update request
│   │   ├── CourseResponse.java           # Course response
│   │   ├── CourseDetailResponse.java     # Detailed course response
│   │   └── CourseSearchRequest.java      # Course search request
│   │
│   ├── service/                          # Course services
│   │   ├── CourseService.java            # Course service interface
│   │   └── CourseServiceImpl.java        # Course service implementation
│   │
│   └── repository/                       # Course repositories
│       ├── CourseRepository.java         # Course repository
│       └── CourseCategoryRepository.java # Course-category relation repository
│
├── enrollment/                             # Enrollment management feature
│   ├── dto/                              # Enrollment DTOs
│   ├── service/                          # Enrollment services
│   └── repository/                       # Enrollment repositories
│
├── category/                               # Category management feature
│   ├── dto/                              # Category DTOs
│   ├── service/                          # Category services
│   └── repository/                       # Category repositories
│
├── lesson/                                 # Lesson management feature
│   ├── dto/                              # Lesson DTOs
│   ├── service/                          # Lesson services
│   └── repository/                       # Lesson repositories
│
├── section/                                # Section management feature
│   ├── dto/                              # Section DTOs
│   ├── service/                          # Section services
│   └── repository/                       # Section repositories
│
├── review/                                 # Review/Rating feature
│   ├── dto/                              # Review DTOs
│   ├── service/                          # Review services
│   └── repository/                       # Review repositories
│
├── payment/                                # Payment processing feature
│   ├── dto/                              # Payment DTOs
│   ├── service/                          # Payment services
│   └── repository/                       # Payment repositories
│
├── instructor/                             # Instructor management feature
│   ├── dto/                              # Instructor DTOs
│   ├── service/                          # Instructor services
│   └── repository/                       # Instructor repositories
│
├── quiz/                                   # Quiz system feature
│   ├── dto/                              # Quiz DTOs
│   ├── service/                          # Quiz services
│   └── repository/                       # Quiz repositories
│
├── video/                                  # Video content feature
│   ├── dto/                              # Video DTOs
│   ├── service/                          # Video services
│   └── repository/                       # Video repositories
│
├── upload/                                 # File upload feature
│   ├── dto/                              # Upload DTOs
│   ├── service/                          # Upload services
│   └── controller/                       # Upload controllers
│
├── common/                                 # Shared utilities and components
│   ├── dto/                              # Common DTOs
│   ├── exception/                        # Custom exceptions
│   └── utils/                            # Utility classes
│
├── exception/                              # Exception handling
│   ├── GlobalExceptionHandler.java       # Global exception handler
│   ├── ResourceNotFoundException.java    # Resource not found exception
│   ├── ValidationException.java          # Validation exception
│   ├── UnauthorizedException.java        # Unauthorized exception
│   └── BusinessLogicException.java       # Business logic exception
│
├── mapper/                                 # Entity-DTO mapping
│   ├── CourseMapper.java                 # Course mapping
│   ├── UserMapper.java                   # User mapping
│   ├── EnrollmentMapper.java            # Enrollment mapping
│   └── ...
│
├── validation/                             # Custom validation
│   ├── validators/                       # Custom validators
│   └── annotations/                      # Custom validation annotations
│
└── utils/                                  # Global utility classes
    ├── Constants.java                    # Application constants
    ├── DateUtils.java                    # Date utilities
    ├── ValidationUtils.java             # Validation utilities
    └── ...
```

## Key Design Principles

### 1. Single Responsibility Principle (SRP)

- Each class has one reason to change
- Controllers only handle HTTP requests/responses
- Services contain business logic
- Repositories handle data access
- DTOs only transfer data

### 2. Open/Closed Principle (OCP)

- Service interfaces allow extension without modification
- Strategy pattern for payment processing
- Plugin architecture for different file upload providers

### 3. Liskov Substitution Principle (LSP)

- All service implementations can replace their interfaces
- Repository implementations are interchangeable

### 4. Interface Segregation Principle (ISP)

- Specific interfaces for different operations
- Role-based controller separation
- Feature-specific service interfaces

### 5. Dependency Inversion Principle (DIP)

- High-level modules depend on abstractions
- Service layer depends on repository interfaces
- Controllers depend on service interfaces

## Feature Organization

Each feature (course, enrollment, payment, etc.) is organized as a self-contained module:

```
feature/
├── dto/                    # Feature-specific DTOs
├── service/               # Feature business logic
└── repository/           # Feature data access
```

This organization provides:

- **High Cohesion**: Related functionality is grouped together
- **Low Coupling**: Features are independent and loosely coupled
- **Easy Testing**: Each feature can be tested in isolation
- **Maintainability**: Changes in one feature don't affect others
- **Scalability**: New features can be added without disrupting existing ones

## Controller Organization by Access Level

### Public Controllers (`/api/public/`)

- No authentication required
- Course browsing, search, categories
- User registration, login

### Student Controllers (`/api/student/`)

- Student role required
- Course enrollment, progress tracking
- Review submission, payment processing

### Instructor Controllers (`/api/instructor/`)

- Instructor role required
- Course creation and management
- Content upload, earnings tracking

### Admin Controllers (`/api/admin/`)

- Admin role required
- User management, course approval
- System configuration, analytics

## DTO Strategy

### Request DTOs

- Input validation using Bean Validation
- Specific DTOs for create/update operations
- Search and filter DTOs for queries

### Response DTOs

- Different levels of detail (summary vs. detailed)
- Exclude sensitive information
- Consistent response structure

## Repository Layer

### Base Repository

- Common CRUD operations
- Pagination and sorting support
- Custom query methods

### Feature Repositories

- Feature-specific queries
- Complex joins and aggregations
- Database-specific optimizations

## Service Layer

### Interface-Implementation Pattern

- Service interfaces in `service/interfaces/`
- Implementations in `service/impl/`
- Enables easy testing and extensibility

### Transaction Management

- `@Transactional` at service level
- Proper exception handling
- Database consistency maintenance

## Exception Handling

### Global Exception Handler

- Centralized error handling
- Consistent error responses
- Proper HTTP status codes

### Custom Exceptions

- Business logic exceptions
- Validation exceptions
- Resource not found exceptions

This structure ensures maintainability, testability, and follows Spring Boot best practices while adhering to SOLID principles.
