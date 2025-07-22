# API Design Principles for Spring Boot Course Management System

## 1. General API Design Principles

### RESTful Standards

- **HTTP Methods**: Use appropriate HTTP methods for actions:
  - `GET` for reading data (e.g., `/api/courses`, `/api/courses/{id}`)
  - `POST` for creating new resources (e.g., `/api/courses`, `/api/auth/register`)
  - `PUT` for full updates (e.g., `/api/courses/{id}`)
  - `PATCH` for partial updates (e.g., `/api/users/{id}/profile`)
  - `DELETE` for removing resources (e.g., `/api/courses/{id}`)

### Path Design

- **Meaningful Hierarchies**: Use clear, hierarchical paths
  - `/api/courses/{courseId}/lessons/{lessonId}`
  - `/api/courses/{courseId}/enroll`
  - `/api/instructor/courses/{courseId}/students`
- **Path Parameters**: Use for specific resource IDs
- **Query Parameters**: Use for filtering, pagination, and optional data
  - `?page=0&size=10&sort=createdAt,desc`
  - `?categoryId=123&status=ACTIVE`

### API Versioning

- Start all endpoints with `/api/` prefix
- Plan for future versioning: `/api/v1/`, `/api/v2/`
- Maintain backward compatibility during transitions

### DTOs (Data Transfer Objects)

- **Always use DTOs** for request/response bodies
- **Input DTOs**: `RegisterUserDto`, `CreateCourseDto`, `UpdateProfileDto`
- **Output DTOs**: `UserResponseDto`, `CourseResponseDto`, `LessonResponseDto`
- **Never expose** database entities directly
- Include validation annotations on DTO fields

### Endpoint Categorization by Role

- **Public endpoints** (no authentication): `/api/courses` (view), `/api/auth/register`
- **Student endpoints**: `/api/student/enrollments`, `/api/student/progress`
- **Instructor endpoints**: `/api/instructor/courses`, `/api/instructor/earnings`
- **Admin endpoints**: `/api/admin/users`, `/api/admin/courses/approval`

## 2. Response Format Principles

### Standard Response Structure

All API responses MUST follow this consistent format:

```json
{
  "statusCode": 200,
  "message": "Operation completed successfully",
  "data": {
    /* actual response data */
  },
  "timestamp": "2025-07-22T00:06:05.123+07:00"
}
```

### Success Response Types

#### Simple Success Response

```json
{
  "statusCode": 201,
  "message": "User registered successfully",
  "data": {
    "id": "90bcafeb-adb5-40b4-a3dc-f007aecc9cae",
    "email": "user@example.com",
    "name": "John Doe",
    "roles": ["STUDENT"]
  },
  "timestamp": "2025-07-22T00:06:05.123+07:00"
}
```

#### Paginated Response

```json
{
  "statusCode": 200,
  "message": "Courses retrieved successfully",
  "data": {
    "content": [
      {
        "id": "course-123",
        "title": "Spring Boot Fundamentals",
        "description": "Learn Spring Boot basics"
      }
    ],
    "page": {
      "number": 0,
      "size": 10,
      "totalElements": 25,
      "totalPages": 3,
      "first": true,
      "last": false
    }
  },
  "timestamp": "2025-07-22T00:06:05.123+07:00"
}
```

#### Empty Data Response

```json
{
  "statusCode": 200,
  "message": "Operation completed successfully",
  "data": null,
  "timestamp": "2025-07-22T00:06:05.123+07:00"
}
```

### Pagination Standards

- **Default page size**: 10 items
- **Maximum page size**: 100 items
- **Page numbering**: 0-based indexing
- **Sort parameter format**: `sort=field,direction` (e.g., `sort=createdAt,desc`)
- **Always include pagination metadata** in response

## 3. Error Handling Principles

### HTTP Status Codes

- **200 OK**: Successful GET, PUT, PATCH requests
- **201 Created**: Successful POST requests (resource creation)
- **204 No Content**: Successful DELETE requests
- **400 Bad Request**: Invalid input data, validation errors
- **401 Unauthorized**: Missing or invalid authentication
- **403 Forbidden**: Valid authentication but insufficient permissions
- **404 Not Found**: Resource doesn't exist
- **409 Conflict**: Resource already exists, business rule violation
- **422 Unprocessable Entity**: Valid JSON but business logic errors
- **500 Internal Server Error**: Unexpected server errors

### Error Response Structure

```json
{
  "statusCode": 400,
  "message": "Validation failed",
  "error": "Bad Request",
  "details": {
    "field": "email",
    "rejectedValue": "invalid-email",
    "message": "Email format is invalid"
  },
  "timestamp": "2025-07-22T00:06:05.123+07:00",
  "path": "/api/auth/register"
}
```

### Validation Error Response (Multiple Fields)

```json
{
  "statusCode": 400,
  "message": "Validation failed for multiple fields",
  "error": "Validation Error",
  "details": [
    {
      "field": "email",
      "rejectedValue": "invalid-email",
      "message": "Email format is invalid"
    },
    {
      "field": "password",
      "rejectedValue": "",
      "message": "Password cannot be empty"
    }
  ],
  "timestamp": "2025-07-22T00:06:05.123+07:00",
  "path": "/api/auth/register"
}
```

### Business Logic Error Response

```json
{
  "statusCode": 409,
  "message": "User is already enrolled in this course",
  "error": "Conflict",
  "details": {
    "courseId": "course-123",
    "userId": "user-456",
    "enrollmentDate": "2025-07-15T10:30:00.000+07:00"
  },
  "timestamp": "2025-07-22T00:06:05.123+07:00",
  "path": "/api/courses/course-123/enroll"
}
```

### Authentication Error Response

```json
{
  "statusCode": 401,
  "message": "Invalid or expired token",
  "error": "Unauthorized",
  "details": {
    "tokenType": "Bearer",
    "expiredAt": "2025-07-21T23:30:00.000+07:00"
  },
  "timestamp": "2025-07-22T00:06:05.123+07:00",
  "path": "/api/student/profile"
}
```

### Spring Boot Implementation Guidelines

#### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        // Handle validation errors with detailed field information
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        // Handle database constraint violations
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        // Handle authorization errors
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex, HttpServletRequest request) {
        // Handle resource not found errors
    }
}
```

#### Response Wrapper Utility

```java
@Component
public class ResponseWrapper {

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(ApiResponse.<T>builder()
            .statusCode(200)
            .message(message)
            .data(data)
            .timestamp(LocalDateTime.now())
            .build());
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.<T>builder()
                .statusCode(201)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
```

## 4. Security Principles

### JWT Authentication & Authorization

- **Access Tokens**: Short-lived (15-30 minutes), contain user ID and roles
- **Refresh Tokens**: Long-lived (30 days), stored securely, single-use
- **Token Format**: `Authorization: Bearer <access_token>`
- **Role-Based Access Control (RBAC)**: Use Spring Security with roles
  - `STUDENT`: Can enroll, view progress, submit reviews
  - `INSTRUCTOR`: Can create courses, manage content, view earnings
  - `ADMIN`: Full system access, user management, course approval

### Password Security

- **Hashing**: Use BCrypt with minimum 12 rounds
- **Password Requirements**: Minimum 8 characters, mix of letters/numbers/symbols
- **Reset Tokens**: Time-limited (1 hour), single-use, cryptographically secure

### Ownership Verification

```java
@PreAuthorize("hasRole('INSTRUCTOR') and @courseService.isOwner(#courseId, authentication.name)")
public ResponseEntity<ApiResponse<CourseDto>> updateCourse(
    @PathVariable String courseId,
    @RequestBody UpdateCourseDto dto) {
    // Implementation
}
```

### Input Validation & Sanitization

- Use `@Valid` annotation with DTOs
- Implement custom validators for business rules
- Sanitize user input to prevent XSS attacks
- Validate file uploads (size, type, content)

## 5. Performance and Maintainability Principles

### Layer Architecture

```
Controller Layer (REST endpoints)
    ↓
Service Layer (Business logic)
    ↓
Repository Layer (Data access)
    ↓
Database Layer
```

### Service Layer Best Practices

```java
@Service
@Transactional
@Slf4j
public class CourseService {

    public CourseResponseDto createCourse(CreateCourseDto dto, String instructorEmail) {
        log.info("Creating course: {} by instructor: {}", dto.getTitle(), instructorEmail);

        // 1. Validate business rules
        validateCourseData(dto);

        // 2. Check instructor permissions
        User instructor = userService.findByEmailAndRole(instructorEmail, "INSTRUCTOR");

        // 3. Create and save entity
        Course course = courseMapper.toEntity(dto);
        course.setInstructor(instructor);
        course.setStatus(CourseStatus.DRAFT);

        Course savedCourse = courseRepository.save(course);

        // 4. Return DTO
        return courseMapper.toResponseDto(savedCourse);
    }
}
```

### Repository Layer with Custom Queries

```java
@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    @Query("SELECT c FROM Course c WHERE c.instructor.id = :instructorId AND c.status = :status")
    Page<Course> findByInstructorIdAndStatus(
        @Param("instructorId") String instructorId,
        @Param("status") CourseStatus status,
        Pageable pageable
    );

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
    Long countEnrollmentsByCourseId(@Param("courseId") String courseId);
}
```

## 6. Input Validation & DTO Design

### Request DTOs with Validation

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseDto {

    @NotBlank(message = "Course title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 1000, message = "Description must be between 20 and 1000 characters")
    private String description;

    @NotNull(message = "Category ID is required")
    @ValidCategoryId
    private String categoryId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @DecimalMax(value = "999.99", message = "Price cannot exceed 999.99")
    private BigDecimal price;

    @Valid
    @NotEmpty(message = "At least one lesson is required")
    @Size(max = 50, message = "Cannot have more than 50 lessons")
    private List<CreateLessonDto> lessons;
}
```

### Response DTOs

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDto {
    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private CourseStatus status;
    private InstructorSummaryDto instructor;
    private CategoryDto category;
    private Integer totalLessons;
    private Integer enrollmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## 7. Testing Principles

### Controller Testing

```java
@WebMvcTest(CourseController.class)
@Import(TestSecurityConfig.class)
class CourseControllerTest {

    @MockBean
    private CourseService courseService;

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void createCourse_Success() throws Exception {
        // Given
        CreateCourseDto dto = CreateCourseDto.builder()
            .title("Test Course")
            .description("Test Description")
            .price(BigDecimal.valueOf(99.99))
            .build();

        CourseResponseDto response = CourseResponseDto.builder()
            .id("course-123")
            .title("Test Course")
            .build();

        when(courseService.createCourse(any(), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/instructor/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.title").value("Test Course"));
    }
}
```

### Service Testing

```java
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CourseService courseService;

    @Test
    void createCourse_ThrowsException_WhenInstructorNotFound() {
        // Given
        CreateCourseDto dto = CreateCourseDto.builder().build();
        when(userService.findByEmailAndRole(any(), any()))
            .thenThrow(new EntityNotFoundException("Instructor not found"));

        // When & Then
        assertThrows(EntityNotFoundException.class, () ->
            courseService.createCourse(dto, "instructor@example.com"));
    }
}
```

## 8. Logging and Monitoring

### Structured Logging

```java
@Slf4j
@Service
public class AuthService {

    public void register(RegisterUserDto dto) {
        log.info("User registration started",
            kv("email", dto.getEmail()),
            kv("action", "user_registration_start"));

        try {
            // Registration logic
            log.info("User registration completed successfully",
                kv("email", dto.getEmail()),
                kv("userId", savedUser.getId()),
                kv("action", "user_registration_success"));
        } catch (Exception e) {
            log.error("User registration failed",
                kv("email", dto.getEmail()),
                kv("error", e.getMessage()),
                kv("action", "user_registration_error"), e);
            throw e;
        }
    }
}
```

### Application Metrics

- Track API response times
- Monitor database query performance
- Log business events (enrollments, course completions)
- Implement health checks for dependencies

## 9. Configuration and Environment Management

### Application Properties Structure

```properties
# Database Configuration
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/course_management}
spring.datasource.username=${DB_USERNAME:course_user}
spring.datasource.password=${DB_PASSWORD:password}

# JWT Configuration
app.jwt.secret=${JWT_SECRET:your-secret-key}
app.jwt.access-token-expiration=${JWT_ACCESS_EXPIRATION:1800000}
app.jwt.refresh-token-expiration=${JWT_REFRESH_EXPIRATION:2592000000}

# File Upload Configuration
app.upload.max-file-size=${MAX_FILE_SIZE:10MB}
app.upload.allowed-types=${ALLOWED_TYPES:image/jpeg,image/png,video/mp4}

# Email Configuration
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.username=${MAIL_USERNAME:noreply@example.com}
spring.mail.password=${MAIL_PASSWORD:app-password}
```

## 10. Business Logic and User Experience

### Enrollment Workflow

1. **Check Prerequisites**: User must be authenticated with STUDENT role
2. **Validate Course**: Course must exist, be published, not already enrolled
3. **Process Payment**: Handle payment if course is paid
4. **Create Enrollment**: Record enrollment with timestamp
5. **Send Notifications**: Email confirmation to student and instructor

### Course Completion Logic

```java
@Service
public class ProgressService {

    @Transactional
    public void markLessonComplete(String userId, String courseId, String lessonId) {
        // 1. Verify enrollment
        Enrollment enrollment = enrollmentService.getEnrollment(userId, courseId);

        // 2. Validate lesson exists in course
        Lesson lesson = lessonService.findByIdAndCourseId(lessonId, courseId);

        // 3. Check prerequisites (previous lessons completed)
        validatePrerequisites(userId, lesson);

        // 4. Mark lesson complete
        LessonCompletion completion = LessonCompletion.builder()
            .user(enrollment.getUser())
            .lesson(lesson)
            .completedAt(LocalDateTime.now())
            .build();
        lessonCompletionRepository.save(completion);

        // 5. Check if course is now complete
        if (isCourseComplete(userId, courseId)) {
            updateCourseCompletion(enrollment);
        }
    }
}
```

This comprehensive API design ensures consistency, security, and maintainability while providing clear guidelines for Spring Boot development.
