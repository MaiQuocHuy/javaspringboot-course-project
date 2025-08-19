# RBAC with ABAC-style Filter Rules - Implementation Guide

## Overview

This implementation provides a comprehensive role-based access control (RBAC) system with attribute-based filter rules (ABAC-style) for Spring Boot applications using Spring Security and JPA.

## Architecture Components

### 1. Core Enums

#### EffectiveFilter

```java
public enum EffectiveFilter {
    DENIED(0),           // No access
    PUBLISHED_ONLY(1),   // Published content only
    OWN(2),             // User's own content
    ALL(3)              // All content (admin)
}
```

**Priority-based conflict resolution**: Higher priority values override lower ones.

### 2. Authorization Service

#### AuthorizationService

- **Purpose**: Central permission evaluation and filter resolution
- **Key Method**: `getEffectiveFilter(User user, String permissionKey)`
- **Logic**:
  1. Queries user's role-based filter rules
  2. Applies priority-based conflict resolution
  3. Returns highest priority effective filter

```java
@Service
public class AuthorizationService {
    public EffectiveFilter getEffectiveFilter(User user, String permissionKey) {
        // Implementation combines multiple role filter rules using priority
    }
}
```

### 3. Custom Permission Evaluator

#### CustomPermissionEvaluator

- **Integration**: Spring Security method-level security
- **Usage**: `@PreAuthorize("hasPermission(#targetId, 'EntityType', 'permission:ACTION')")`
- **Context**: Sets thread-local context for repository queries

```java
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    @Override
    public boolean hasPermission(Authentication auth, Object target, Object permission) {
        // 1. Extract user from authentication
        // 2. Evaluate permission using AuthorizationService
        // 3. Set EffectiveFilterContext for repository layer
        // 4. Return access decision
    }
}
```

### 4. JPA Specifications for Dynamic Filtering

#### EffectiveFilterSpecifications

- **Purpose**: Apply effective filters to JPA queries
- **Usage**: Repository methods use specifications to filter data

```java
public class EffectiveFilterSpecifications {
    public static Specification<Course> applyCourseFilter() {
        return (root, query, cb) -> {
            EffectiveFilter filter = EffectiveFilterContext.getCurrentFilter();
            return switch (filter) {
                case ALL -> cb.conjunction();
                case OWN -> cb.equal(root.get("instructor").get("id"), currentUser.getId());
                case PUBLISHED_ONLY -> cb.and(
                    cb.isTrue(root.get("isPublished")),
                    cb.isTrue(root.get("isApproved"))
                );
                case DENIED -> cb.disjunction();
            };
        };
    }
}
```

## Usage Patterns

### 1. Controller Method Security

```java
@RestController
public class CourseController {

    @GetMapping("/courses/{id}")
    @PreAuthorize("hasPermission(#courseId, 'Course', 'course:READ')")
    public ResponseEntity<Course> getCourse(@PathVariable String courseId) {
        // Permission check happens automatically
        // EffectiveFilterContext is set by PermissionEvaluator

        Specification<Course> spec = EffectiveFilterSpecifications.applyCourseFilter();
        return courseRepository.findOne(spec.and(idEquals(courseId)))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

### 2. Service Layer Usage

```java
@Service
public class CourseService {

    public Page<Course> getAccessibleCourses(User user, Pageable pageable) {
        // Get effective filter for user
        EffectiveFilter filter = authorizationService.getEffectiveFilter(user, "course:READ");

        if (filter == EffectiveFilter.DENIED) {
            return Page.empty(pageable);
        }

        // Set context for specifications
        EffectiveFilterContext.setCurrentFilter(filter);
        EffectiveFilterContext.setCurrentUser(user);

        try {
            Specification<Course> spec = EffectiveFilterSpecifications.applyCourseFilter();
            return courseRepository.findAll(spec, pageable);
        } finally {
            EffectiveFilterContext.clear();
        }
    }
}
```

### 3. Repository Integration

```java
@Repository
public interface CourseRepository extends JpaRepository<Course, String>,
                                        JpaSpecificationExecutor<Course> {
    // Inherits findAll(Specification<Course> spec) method
}
```

## Configuration

### 1. Security Configuration

```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(customPermissionEvaluator);
        return handler;
    }
}
```

### 2. Web Configuration

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(filterContextCleanupInterceptor)
                .addPathPatterns("/**");
    }
}
```

## Filter Resolution Examples

### Scenario 1: Admin User

- **Roles**: ADMIN
- **Filter Rules**: course:READ → ALL
- **Result**: Access to all courses without restrictions

### Scenario 2: Instructor User

- **Roles**: INSTRUCTOR
- **Filter Rules**: course:READ → OWN
- **Result**: Access only to courses where `instructor.id = user.id`

### Scenario 3: Student User

- **Roles**: STUDENT
- **Filter Rules**: course:READ → PUBLISHED_ONLY
- **Result**: Access only to courses where `isPublished = true AND isApproved = true`

### Scenario 4: Multi-Role User

- **Roles**: INSTRUCTOR + CONTENT_REVIEWER
- **Filter Rules**:
  - INSTRUCTOR: course:READ → OWN
  - CONTENT_REVIEWER: course:READ → PUBLISHED_ONLY
- **Conflict Resolution**: OWN (priority 2) > PUBLISHED_ONLY (priority 1)
- **Result**: Access to own courses only

## Database Schema

### Role Filter Rules Table

```sql
CREATE TABLE role_filter_rules (
    id VARCHAR(36) PRIMARY KEY,
    role_permission_id VARCHAR(36) NOT NULL,
    filter_type ENUM('ALL', 'OWN', 'PUBLISHED_ONLY') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_permission_id) REFERENCES role_permissions(id)
);
```

### Sample Data

```sql
-- Admin role can access all courses
INSERT INTO role_filter_rules (id, role_permission_id, filter_type) VALUES
('rule-1', 'admin-course-read-perm', 'ALL');

-- Instructor role can access own courses
INSERT INTO role_filter_rules (id, role_permission_id, filter_type) VALUES
('rule-2', 'instructor-course-read-perm', 'OWN');

-- Student role can access published courses only
INSERT INTO role_filter_rules (id, role_permission_id, filter_type) VALUES
('rule-3', 'student-course-read-perm', 'PUBLISHED_ONLY');
```

## Benefits

1. **Clean Separation**: Permission logic separated from business logic
2. **Extensible**: Easy to add new filter types with priority
3. **Declarative**: Use `@PreAuthorize` annotations for method security
4. **Reusable**: Specifications can be combined with business filters
5. **Thread-Safe**: Thread-local context prevents cross-request contamination
6. **Auditable**: Clear permission evaluation with logging

## Thread Safety

- **EffectiveFilterContext**: Uses ThreadLocal storage
- **Cleanup**: Automatic cleanup via interceptor prevents memory leaks
- **Isolation**: Each request thread has isolated context

## Error Handling

- **No Permission**: Returns DENIED filter → empty results
- **Missing Context**: Specifications return deny-all predicate
- **Invalid Data**: Graceful fallback to most restrictive access
