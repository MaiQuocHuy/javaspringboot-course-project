# KTC Learning Platform - Project Structure

This document outlines a clean, reusable, and scalable project structure for both the backend (Spring Boot) and frontend (Next.js) applications.

---

## Backend (Spring Boot)

The backend follows a modular structure, grouping code by feature. Within each feature module, it is organized by layer (Controller, Service, Repository, DTOs).

```
backend/
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── ktc/
        │           └── learning/
        │               ├── KtcLearningApplication.java       # Main application entry point
        │               │
        │               ├── common/                           # Cross-cutting concerns
        │               │   ├── dto/                          # Common DTOs (e.g., ApiResponse, PageResponse)
        │               │   ├── exception/                    # Global exception handlers and custom exceptions
        │               │   └── model/                        # Common models or enums not tied to a specific entity
        │               │
        │               ├── config/                           # Centralized configuration
        │               │   ├── SecurityConfig.java           # Spring Security configuration
        │               │   ├── WebConfig.java                # Web-related beans (CORS, etc.)
        │               │   └── JpaConfig.java                # JPA and data source configuration
        │               │
        │               ├── modules/                          # Main application modules (features)
        │               │   ├── auth/
        │               │   │   ├── AuthController.java
        │               │   │   ├── AuthService.java
        │               │   │   ├── dto/                      # DTOs specific to authentication
        │               │   │   │   ├── LoginDto.java
        │               │   │   │   └── RegisterDto.java
        │               │   │   └── security/                 # JWT and security implementation details
        │               │   │       ├── JwtTokenProvider.java
        │               │   │       ├── JwtAuthenticationFilter.java
        │               │   │       └── CustomUserDetailsService.java
        │               │   │
        │               │   ├── user/
        │               │   │   ├── UserController.java
        │               │   │   ├── UserService.java
        │               │   │   ├── UserRepository.java
        │               │   │   ├── User.java                 # JPA Entity
        │               │   │   └── dto/
        │               │   │
        │               │   ├── course/
        │               │   │   ├── CourseController.java
        │               │   │   ├── CourseService.java
        │               │   │   ├── CourseRepository.java
        │               │   │   ├── Course.java               # JPA Entity
        │               │   │   └── dto/
        │               │   │
        │               │   ├── enrollment/
        │               │   │   ├── EnrollmentController.java
        │               │   │   ├── EnrollmentService.java
        │               │   │   ├── EnrollmentRepository.java
        │               │   │   └── Enrollment.java           # JPA Entity
        │               │   │
        │               │   └── payment/
        │               │       ├── PaymentController.java
        │               │       ├── PaymentService.java
        │               │       ├── PaymentRepository.java
        │               │       └── Payment.java              # JPA Entity
        │               │
        │               └── util/                             # Utility classes (e.g., Mappers, Helpers)
        │
        └── resources/
            ├── application.properties                        # Main application configuration
            ├── application-dev.properties                    # Environment-specific properties
            ├── application-prod.properties
            └── db/
                └── migration/                                # Database migration scripts (Flyway/Liquibase)
```

---

## Frontend (Next.js)

The frontend uses the Next.js App Router for routing and a feature-based organization for components, services, and hooks.

```
client/
├── app/
│   ├── (auth)/                                 # Route group for auth pages (e.g., /login)
│   │   ├── login/page.tsx
│   │   └── register/page.tsx
│   │
│   ├── (dashboard)/                            # Route group for protected dashboard pages
│   │   ├── layout.tsx                          # Layout specific to the dashboard
│   │   └── courses/
│   │       ├── page.tsx                        # /dashboard/courses
│   │       └── [courseId]/
│   │           ├── page.tsx                    # /dashboard/courses/123
│   │           └── lessons/
│   │               └── [lessonId]/page.tsx     # /dashboard/courses/123/lessons/456
│   │
│   ├── api/                                    # Next.js API routes (e.g., for NextAuth)
│   │   └── auth/
│   │       └── [...nextauth]/route.ts
│   │
│   ├── globals.css                             # Global styles
│   ├── layout.tsx                              # Root layout
│   └── page.tsx                                # Home page
│
├── components/
│   ├── ui/                                     # Reusable, generic UI components (e.g., Button, Input from Shadcn/ui)
│   ├── common/                                 # Common application components (e.g., Header, Footer, Sidebar)
│   └── features/                               # Components specific to a feature
│       ├── courses/
│       │   ├── CourseCard.tsx
│       │   └── CourseList.tsx
│       └── auth/
│           ├── LoginForm.tsx
│           └── RegisterForm.tsx
│
├── hooks/                                      # Custom React hooks (e.g., useAuth, useCourses)
│
├── lib/
│   ├── api.ts                                  # Central API client (e.g., Axios instance with interceptors)
│   ├── auth.ts                                 # NextAuth configuration
│   └── utils.ts                                # Utility functions
│
├── services/                                   # Functions for fetching/mutating data from the backend API
│   ├── authService.ts
│   ├── courseService.ts
│   └── paymentService.ts
│
├── types/                                      # TypeScript type definitions
│   ├── index.ts
│   └── api.ts
│
├── .env.local                                  # Environment variables
├── next.config.js
├── package.json
└── tsconfig.json
```
