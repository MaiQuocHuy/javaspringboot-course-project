# Step-by-Step Full-Stack Development Plan

This document provides a comprehensive, step-by-step guide for developing the KTC Learning Platform, from initial setup to final deployment.

---

### Phase 1: Foundation & Project Setup (Week 1)

The goal of this phase is to establish the development environment, project skeletons, and core infrastructure.

- **Step 1: Environment Setup**

  - [ ] Install required tools:
    - Java JDK 24+
    - Maven or Gradle
    - Node.js 18+
    - Docker and Docker Compose
    - IDEs: IntelliJ IDEA for backend, VS Code for frontend.
    - Git

- **Step 2: Version Control**

  - [ ] Initialize a new Git repository.
  - [ ] Create a main `README.md` at the root level.
  - [ ] Create `develop` and `main` branches. `develop` will be the primary branch for integration.
  - [ ] Configure `.gitignore` files for both the `backend` and `client` directories.

- **Step 3: Backend Project Initialization**

  - [ ] Go to [start.spring.io](https://start.spring.io).
  - [ ] Generate a new Spring Boot project with the following dependencies:
    - Spring Web
    - Spring Security
    - Spring Data JPA
    - MySQL Driver
    - Lombok
    - Validation
  - [ ] Set up the project inside the `backend` directory.
  - [ ] Implement the directory structure as defined in `project_structure.md`.

- **Step 4: Frontend Project Initialization**

  - [ ] Inside the `client` directory, run `npx create-next-app@latest`.
  - [ ] Choose TypeScript, ESLint, Tailwind CSS, and the App Router.
  - [ ] Implement the directory structure as defined in `project_structure.md`.

- **Step 5: Database Setup**
  - [ ] Create a `docker-compose.yml` file at the project root to run a MySQL 8.0 container.
  - [ ] Configure the database name, user, and password.
  - [ ] Run `docker-compose up -d` to start the database.
  - [ ] Connect the backend application to the database by configuring `application-dev.properties`.

---

### Phase 2: Backend - Authentication & Core Models (Week 2-3)

Focus on building the security foundation and core data structures.

- **Step 1: Create JPA Entities**

  - [ ] Based on `database.md`, create all JPA entity classes (`User`, `Course`, `Lesson`, etc.) under `src/main/java/com/ktc/learning/modules/*/`.
  - [ ] Define relationships (`@OneToMany`, `@ManyToOne`, etc.) between entities.

- **Step 2: Implement Authentication & Authorization**

  - [ ] Configure `SecurityConfig.java` to define public/protected routes.
  - [ ] Implement `CustomUserDetailsService` to load user data for Spring Security.
  - [ ] Implement `JwtTokenProvider` to generate and validate JWTs.
  - [ ] Create `JwtAuthenticationFilter` to process tokens on incoming requests and add it to the security filter chain.

- **Step 3: Build Authentication API**

  - [ ] Following `tasklist.md`, create `AuthController`.
  - [ ] Implement the `POST /api/auth/register` endpoint.
  - [ ] Implement the `POST /api/auth/login` endpoint.
  - [ ] Create request/response DTOs for these endpoints.
  - [ ] Implement the `AuthService` to handle the business logic (password hashing, user creation, token generation).

- **Step 4: Build User Profile API**
  - [ ] Create `UserController` and `UserService`.
  - [ ] Implement the `GET /api/users/me` endpoint to fetch the current authenticated user's profile.

---

### Phase 3: Frontend - UI Foundation & Auth Flow (Week 4-5)

Connect the frontend to the backend and build the initial user-facing screens.

- **Step 1: Setup UI Components & Styling**

  - [ ] Install and configure a UI component library like **Shadcn/ui**.
  - [ ] Create common layout components (`Header`, `Footer`, `Sidebar`).
  - [ ] Define the root layout in `app/layout.tsx`.

- **Step 2: API Integration Layer**

  - [ ] Create a centralized API client using Axios or `fetch`.
  - [ ] Configure interceptors to automatically attach the JWT `accessToken` to requests.
  - [ ] Implement logic to handle token refresh if an `accessToken` expires.

- **Step 3: State Management & Auth Context**

  - [ ] Create a global state management solution (React Context or Zustand) for authentication.
  - [ ] The state should store the user object, tokens, and authentication status.

- **Step 4: Implement Auth Pages**
  - [ ] Build the `Register` page and form (`/register`).
  - [ ] Build the `Login` page and form (`/login`).
  - [ ] Connect the forms to the backend API endpoints via the API service layer.
  - [ ] Implement protected routes that redirect unauthenticated users to the login page.

---

### Phase 4: Full-Stack Feature Development (Iterative, Week 6-10)

Develop features module by module in an iterative cycle. For each module (Courses, Enrollments, Payments, etc.):

- **Backend Module Development**

  - [ ] **Controller**: Create the `*Controller.java` with endpoint stubs.
  - [ ] **Service**: Implement the `*Service.java` with core business logic.
  - [ ] **Repository**: Define the `*Repository.java` interface extending `JpaRepository`.
  - [ ] **DTOs**: Create Data Transfer Objects for API requests and responses.
  - [ ] **Testing**: Write unit tests for the service layer and integration tests for the controller.

- **Frontend Module Development**
  - [ ] **API Service**: Add functions to the relevant service file (e.g., `courseService.ts`) to call the new backend endpoints.
  - [ ] **Pages**: Create new pages under the App Router for the feature.
  - [ ] **Components**: Build feature-specific components (e.g., `CourseCard`, `LessonPlayer`).
  - [ ] **State & Hooks**: Create custom hooks (`useCourses`) to fetch and manage data.
  - [ ] **Integration**: Connect components to the API services and render the data.

---

### Phase 5: Advanced Features & Polishing (Week 11-12)

- **Step 1: Implement Advanced Authentication**

  - [ ] **Backend**: Build the "Forgot Password" and "Reset Password" APIs.
  - [ ] **Frontend**: Create the UI flows for password recovery.
  - [ ] **Backend**: Implement Social Login (OAuth 2.0) with Spring Security.
  - [ ] **Frontend**: Add "Login with Google/GitHub" buttons and handle the client-side flow.

- **Step 2: Payment Integration**

  - [ ] **Backend**: Integrate a payment provider like Stripe. Create APIs for creating payment intents and confirming payments.
  - [ ] **Frontend**: Build the checkout page and payment form using Stripe Elements.

- **Step 3: Testing**
  - [ ] Write comprehensive end-to-end tests using Cypress or Playwright.
  - [ ] Perform cross-browser testing and ensure responsiveness.
  - [ ] Conduct a final round of manual user acceptance testing (UAT).

---

### Phase 6: Deployment & Go-Live (Week 13)

- **Step 1: Dockerization**

  - [ ] Write a `Dockerfile` for the Spring Boot application to create a production-ready image.
  - [ ] Write a multi-stage `Dockerfile` for the Next.js application to build and serve the app.

- **Step 2: CI/CD Pipeline**

  - [ ] Set up a GitHub Actions workflow.
  - [ ] The workflow should trigger on pushes to the `develop` branch.
  - [ ] **Jobs**:
    - 1. Build and test backend.
    - 2. Build and test frontend.
    - 3. Build and push Docker images to a container registry (e.g., Docker Hub, GitHub Container Registry).

- **Step 3: Production Deployment**

  - [ ] Choose a cloud provider (e.g., AWS, Vercel).
  - [ ] **Backend**: Deploy the backend container (e.g., on Amazon ECS or a VM).
  - [ ] **Frontend**: Deploy the frontend (Vercel is highly recommended for Next.js).
  - [ ] **Database**: Use a managed database service (e.g., Amazon RDS).
  - [ ] Configure production environment variables and secrets securely.
  - [ ] Set up DNS and HTTPS.

- **Step 4: Monitoring**
  - [ ] Set up logging and monitoring services (e.g., Sentry, Datadog) to track application health and errors.
