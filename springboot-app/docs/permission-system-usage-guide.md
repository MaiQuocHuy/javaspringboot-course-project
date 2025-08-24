# Spring Security RBAC + ABAC Permission System - Production Usage Guide

## Overview

This document provides comprehensive examples and best practices for using the enhanced Spring Security permission system with `CustomPermissionEvaluator` and `ResourceOwnershipService`.

## System Architecture

```
┌─────────────────────┐    ┌─────────────────────┐    ┌─────────────────────┐
│   @PreAuthorize     │───▶│ CustomPermission    │───▶│ AuthorizationService │
│   Annotations       │    │ Evaluator           │    │                     │
└─────────────────────┘    └─────────────────────┘    └─────────────────────┘
                                        │                         │
                                        ▼                         │
                           ┌─────────────────────┐                │
                           │ ResourceOwnership   │                │
                           │ Service             │                │
                           └─────────────────────┘                │
                                                                  │
                                        ┌─────────────────────────┘
                                        ▼
                           ┌─────────────────────┐
                           │ EffectiveFilter     │
                           │ Specifications      │
                           └─────────────────────┘
```

## Permission Types

### 1. Collection-Level Permissions

Used when accessing lists/collections of resources without specific IDs.

```java
// Basic collection access
@PreAuthorize("hasPermission(null, 'Course', 'course:READ')")
@GetMapping("/api/courses")
public Page<Course> getAllCourses(Pageable pageable) {
    // EffectiveFilterSpecifications automatically applies filtering
    // based on user's effective filter type (ALL/OWN/DENIED)
    return courseRepository.findAll(pageable);
}

// Admin collection access
@PreAuthorize("hasPermission(null, 'Course', 'course:ADMIN')")
@GetMapping("/api/admin/courses")
public Page<Course> getCoursesForAdmin(Pageable pageable) {
    return courseRepository.findAll(pageable);
}

// Creating new resources
@PreAuthorize("hasPermission(null, 'Course', 'course:WRITE')")
@PostMapping("/api/courses")
public Course createCourse(@RequestBody Course course) {
    return courseRepository.save(course);
}
```

### 2. Instance-Level Permissions

Used when accessing specific resources by ID.

```java
// Reading specific resource
@PreAuthorize("hasPermission(#courseId, 'Course', 'course:READ')")
@GetMapping("/api/courses/{courseId}")
public Course getCourseById(@PathVariable Long courseId) {
    return courseRepository.findById(courseId).orElseThrow();
}

// Updating specific resource
@PreAuthorize("hasPermission(#courseId, 'Course', 'course:WRITE')")
@PutMapping("/api/courses/{courseId}")
public Course updateCourse(@PathVariable Long courseId, @RequestBody Course course) {
    // Ownership is already validated by @PreAuthorize
    Course existing = courseRepository.findById(courseId).orElseThrow();
    // Update logic...
    return courseRepository.save(existing);
}

// Deleting specific resource
@PreAuthorize("hasPermission(#courseId, 'Course', 'course:DELETE')")
@DeleteMapping("/api/courses/{courseId}")
public void deleteCourse(@PathVariable Long courseId) {
    courseRepository.deleteById(courseId);
}
```

## Filter Type Behavior

### ALL Filter

User can access all resources of this type.

```java
// User with ALL filter for course:READ can see all courses
@PreAuthorize("hasPermission(null, 'Course', 'course:READ')")
public Page<Course> getAllCourses() {
    // Returns all courses without filtering
}
```

### OWN Filter

User can only access resources they own or have relationships with.

```java
// User with OWN filter for course:READ can only see:
// - Courses they instruct
// - Courses they are enrolled in
@PreAuthorize("hasPermission(null, 'Course', 'course:READ')")
public Page<Course> getMyCourses() {
    // EffectiveFilterSpecifications applies ownership filtering
}

// Instance-level check with OWN filter
@PreAuthorize("hasPermission(#courseId, 'Course', 'course:READ')")
public Course getCourse(@PathVariable Long courseId) {
    // Only succeeds if user is instructor or enrolled
}
```

### DENIED Filter

User cannot access any resources of this type.

```java
// User with DENIED filter is blocked at permission level
@PreAuthorize("hasPermission(null, 'Course', 'course:READ')")
public Page<Course> getCourses() {
    // Returns 403 Forbidden immediately
}
```

## Complex Permission Scenarios

### 1. Nested Resource Access

```java
// Accessing reviews of a specific course
@PreAuthorize("hasPermission(#courseId, 'Course', 'course:READ') and hasPermission(null, 'Review', 'review:READ')")
@GetMapping("/api/courses/{courseId}/reviews")
public Page<Review> getCourseReviews(@PathVariable Long courseId) {
    // User must have access to both the course AND review permissions
}
```

### 2. Multiple Permission Checks

```java
// User needs either admin role OR specific permission
@PreAuthorize("hasRole('ADMIN') or hasPermission(#enrollmentId, 'Enrollment', 'enrollment:READ')")
@GetMapping("/api/enrollments/{enrollmentId}")
public Enrollment getEnrollment(@PathVariable Long enrollmentId) {
    // Admins bypass ownership checks, others need proper permissions
}
```

### 3. Business Rule Integration

```java
// Combine permissions with custom business logic
@PreAuthorize("hasPermission(#courseId, 'Course', 'course:READ') and @enrollmentService.canUserEnroll(authentication.principal, #courseId)")
@PostMapping("/api/courses/{courseId}/enroll")
public Enrollment enrollInCourse(@PathVariable Long courseId) {
    // Permission check + custom business validation
}
```

### 4. Request Body Parameter Validation

```java
// Check permission against resource ID in request body
@PreAuthorize("hasPermission(#request.courseId, 'Course', 'course:WRITE')")
@PostMapping("/api/lessons")
public Lesson createLesson(@RequestBody CreateLessonRequest request) {
    // Validates user can modify the course specified in request
}
```

## Ownership Relationships

The `ResourceOwnershipService` defines these ownership rules:

### Course Ownership

- **Instructor**: User who created/teaches the course
- **Enrolled Student**: User enrolled in the course
- **Relationship Check**: Either instructor OR enrolled

### Review Ownership

- **Author**: User who wrote the review
- **Course Instructor**: Instructor of the reviewed course
- **Relationship Check**: Either author OR course instructor

### Enrollment Ownership

- **Student**: User who owns the enrollment
- **Course Instructor**: Instructor of the enrolled course
- **Relationship Check**: Either student OR course instructor

### User Profile Ownership

- **Self**: User can access their own profile
- **Relationship Check**: Current user == target user

## Database Schema Requirements

### Permission Keys

Use UPPERCASE format in database:

```sql
-- Correct format
INSERT INTO permissions (permission_key) VALUES ('COURSE:READ');
INSERT INTO permissions (permission_key) VALUES ('COURSE:WRITE');
INSERT INTO permissions (permission_key) VALUES ('COURSE:DELETE');
INSERT INTO permissions (permission_key) VALUES ('COURSE:ADMIN');
```

### Filter Types

Each role-permission mapping must specify a filter type:

```sql
INSERT INTO role_permissions (role_id, permission_id, filter_type_id)
VALUES (
    (SELECT id FROM roles WHERE name = 'STUDENT'),
    (SELECT id FROM permissions WHERE permission_key = 'COURSE:READ'),
    (SELECT id FROM filter_types WHERE type_name = 'OWN')
);
```

## Production Configuration

### 1. Enable Method Security

```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            CustomPermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }
}
```

### 2. Repository Integration

```java
@Repository
public class CourseRepository extends JpaRepository<Course, Long> {

    // Use EffectiveFilterSpecifications for filtered queries
    public Page<Course> findAllWithFilter(Pageable pageable) {
        Specification<Course> spec = EffectiveFilterSpecifications.applyCourseFilter();
        return findAll(spec, pageable);
    }
}
```

### 3. Service Layer Integration

```java
@Service
@Transactional
public class CourseService {

    @PreAuthorize("hasPermission(null, 'Course', 'course:READ')")
    public Page<Course> findAll(Pageable pageable) {
        // Method-level security with automatic filtering
        return courseRepository.findAllWithFilter(pageable);
    }

    @PreAuthorize("hasPermission(#courseId, 'Course', 'course:WRITE')")
    public Course update(Long courseId, Course updates) {
        // Instance-level security with ownership validation
        Course existing = courseRepository.findById(courseId).orElseThrow();
        // Update logic...
        return courseRepository.save(existing);
    }
}
```

## Performance Considerations

### 1. Collection Filtering

- Uses database-level filtering via JPA Specifications
- Efficient for large datasets
- Single query with appropriate WHERE clauses

### 2. Instance-Level Checks

- Performs ownership queries per resource
- Consider caching for frequently accessed relationships
- Batch ownership checks for multiple resources

### 3. Permission Caching

```java
@Service
@CacheConfig(cacheNames = "permissions")
public class AuthorizationService {

    @Cacheable(key = "#user.id + ':' + #permissionKey")
    public AuthorizationResult evaluatePermission(User user, String permissionKey) {
        // Cache permission evaluation results
    }
}
```

## Security Best Practices

### 1. Always Use @PreAuthorize

```java
// ✅ CORRECT: Protected endpoint
@PreAuthorize("hasPermission(#courseId, 'Course', 'course:READ')")
@GetMapping("/api/courses/{courseId}")
public Course getCourse(@PathVariable Long courseId) { }

// ❌ INCORRECT: Unprotected endpoint
@GetMapping("/api/courses/{courseId}")
public Course getCourse(@PathVariable Long courseId) { }
```

### 2. Validate Resource Existence

The system automatically checks resource existence before ownership validation.

### 3. Log Security Events

```java
@Aspect
@Component
public class SecurityAuditAspect {

    @AfterReturning("@annotation(org.springframework.security.access.prepost.PreAuthorize)")
    public void logPermissionGranted(JoinPoint joinPoint) {
        log.info("Permission granted for {}", joinPoint.getSignature());
    }

    @AfterThrowing(value = "@annotation(org.springframework.security.access.prepost.PreAuthorize)",
                   throwing = "ex")
    public void logPermissionDenied(JoinPoint joinPoint, AccessDeniedException ex) {
        log.warn("Permission denied for {}: {}", joinPoint.getSignature(), ex.getMessage());
    }
}
```

### 4. Error Handling

```java
@ControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Access denied"));
    }
}
```

## Testing Permissions

### 1. Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class PermissionTests {

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private ResourceOwnershipService ownershipService;

    @InjectMocks
    private CustomPermissionEvaluator evaluator;

    @Test
    void shouldGrantAccessWithOwnFilter() {
        // Mock permission evaluation
        when(authorizationService.evaluatePermission(user, "course:READ"))
                .thenReturn(AuthorizationResult.allowed(EffectiveFilterType.OWN, user));

        // Mock ownership check
        when(ownershipService.hasResourceRelationship(userId, courseId, "Course"))
                .thenReturn(true);

        boolean result = evaluator.hasPermission(auth, courseId, "Course", "course:READ");
        assertTrue(result);
    }
}
```

### 2. Integration Tests

```java
@SpringBootTest
@AutoConfigureTestDatabase
class PermissionIntegrationTests {

    @Test
    @WithMockUser(username = "instructor@example.com")
    void shouldAllowInstructorToAccessOwnCourse() {
        // Test with actual security context
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/courses/{courseId}", instructorCourseId))
                .andExpect(status().isOk())
                .andReturn().getResponse();
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void shouldDenyStudentFromAccessingOthersCourse() {
        mockMvc.perform(get("/api/courses/{courseId}", otherInstructorCourseId))
                .andExpect(status().isForbidden());
    }
}
```

## Common Patterns

### 1. Resource Creation with Ownership

```java
@PreAuthorize("hasPermission(null, 'Course', 'course:WRITE')")
@PostMapping("/api/courses")
public Course createCourse(@RequestBody Course course, Authentication auth) {
    User currentUser = (User) auth.getPrincipal();
    course.setInstructor(currentUser); // Set ownership
    return courseRepository.save(course);
}
```

### 2. Conditional Access Based on Status

```java
@PreAuthorize("hasPermission(#courseId, 'Course', 'course:READ') and @courseService.isPublished(#courseId)")
@GetMapping("/api/courses/{courseId}")
public Course getPublishedCourse(@PathVariable Long courseId) {
    // Only published courses are accessible
}
```

### 3. Hierarchical Permissions

```java
// Admin can access everything, instructors only their own courses
@PreAuthorize("hasRole('ADMIN') or (hasRole('INSTRUCTOR') and hasPermission(#courseId, 'Course', 'course:READ'))")
@GetMapping("/api/courses/{courseId}/analytics")
public CourseAnalytics getCourseAnalytics(@PathVariable Long courseId) {
    // Analytics access with role hierarchy
}
```

This production-ready permission system provides comprehensive security with clear ownership rules, efficient filtering, and detailed logging for security auditing.
