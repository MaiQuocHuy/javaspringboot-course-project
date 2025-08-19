# RBAC Usage Guide for Developers

## 🚀 Quick Start

### Step 1: Add Permission Check to Controller

```java
@GetMapping("/my-endpoint")
@PreAuthorize("hasPermission(null, 'Resource', 'resource:ACTION')")
public ResponseEntity<List<MyEntity>> getMyData() {
    // Your business logic here
    return ResponseEntity.ok(myService.getData());
}
```

### Step 2: Apply Security Filter in Repository Call

```java
@Service
public class MyService {

    public List<MyEntity> getData() {
        // Apply effective filter automatically
        Specification<MyEntity> securitySpec = EffectiveFilterSpecifications.applyMyEntityFilter();
        return myEntityRepository.findAll(securitySpec);
    }
}
```

### Step 3: Create Entity-Specific Specification (if needed)

```java
public static Specification<MyEntity> applyMyEntityFilter() {
    return (root, query, cb) -> {
        EffectiveFilter filter = EffectiveFilterContext.getCurrentFilter();
        User currentUser = EffectiveFilterContext.getCurrentUser();

        return switch (filter) {
            case ALL -> cb.conjunction();
            case OWN -> cb.equal(root.get("ownerId"), currentUser.getId());
            case PUBLISHED_ONLY -> cb.isTrue(root.get("isPublished"));
            case DENIED -> cb.disjunction();
        };
    };
}
```

## 📋 Permission Naming Convention

### Standard Format: `{resource}:{action}`

| Resource  | Actions                                | Examples                          |
| --------- | -------------------------------------- | --------------------------------- |
| `course`  | `READ`, `WRITE`, `DELETE`, `ANALYTICS` | `course:READ`, `course:WRITE`     |
| `user`    | `READ`, `WRITE`, `DELETE`, `MANAGE`    | `user:READ`, `user:MANAGE`        |
| `payment` | `READ`, `WRITE`, `PROCESS`, `REFUND`   | `payment:READ`, `payment:PROCESS` |
| `report`  | `READ`, `GENERATE`, `EXPORT`           | `report:READ`, `report:GENERATE`  |

### Permission Hierarchy

```
resource:READ     → Basic read access
resource:WRITE    → Create/update access (includes READ)
resource:DELETE   → Delete access (includes READ/WRITE)
resource:MANAGE   → Full administrative access (includes all above)
```

## 🎯 Filter Type Usage Guide

### When to Use Each Filter Type

#### `ALL` Filter - Administrative Access

**Use Case**: System administrators, super users
**Query Result**: No restrictions - sees all data
**Example Roles**: ADMIN, SYSTEM_MANAGER

```java
// Database result: All records regardless of ownership or status
SELECT * FROM courses;
```

#### `OWN` Filter - Personal Data Access

**Use Case**: Users accessing their own data
**Query Result**: Only records owned/created by the user
**Example Roles**: INSTRUCTOR (own courses), STUDENT (own enrollments)

```java
// Database result: Only user's own records
SELECT * FROM courses WHERE instructor_id = :currentUserId;
```

#### `PUBLISHED_ONLY` Filter - Public Data Access

**Use Case**: Limited access to approved/published content
**Query Result**: Only published and approved records
**Example Roles**: STUDENT (browsing courses), GUEST (public access)

```java
// Database result: Only public-facing data
SELECT * FROM courses
WHERE is_published = true
  AND is_approved = true
  AND is_deleted = false;
```

#### `DENIED` Filter - No Access

**Use Case**: Explicitly denied access or invalid permissions
**Query Result**: Empty result set
**Example**: Suspended users, invalid permissions

```java
// Database result: No records returned
SELECT * FROM courses WHERE 1=0;
```

## 🔧 Common Implementation Patterns

### Pattern 1: Simple Resource Protection

```java
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @GetMapping
    @PreAuthorize("hasPermission(null, 'Course', 'course:READ')")
    public ResponseEntity<List<Course>> getAllCourses() {
        Specification<Course> spec = EffectiveFilterSpecifications.applyCourseFilter();
        List<Course> courses = courseRepository.findAll(spec);
        return ResponseEntity.ok(courses);
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'Course', 'course:WRITE')")
    public ResponseEntity<Course> createCourse(@RequestBody CourseCreateDto dto) {
        Course course = courseService.createCourse(dto);
        return ResponseEntity.ok(course);
    }
}
```

### Pattern 2: Combined Business Logic + Security

```java
@GetMapping("/search")
@PreAuthorize("hasPermission(null, 'Course', 'course:READ')")
public ResponseEntity<List<Course>> searchCourses(
    @RequestParam String keyword,
    @RequestParam CourseLevel level) {

    // Combine security filter with business logic
    Specification<Course> securityFilter = EffectiveFilterSpecifications.applyCourseFilter();
    Specification<Course> businessFilter = Specification
        .where(hasKeyword(keyword))
        .and(hasLevel(level));

    Specification<Course> combinedSpec = EffectiveFilterSpecifications.and(
        securityFilter,
        businessFilter
    );

    List<Course> courses = courseRepository.findAll(combinedSpec);
    return ResponseEntity.ok(courses);
}

// Helper methods for business logic
private Specification<Course> hasKeyword(String keyword) {
    return (root, query, cb) -> cb.like(
        cb.lower(root.get("title")),
        "%" + keyword.toLowerCase() + "%"
    );
}

private Specification<Course> hasLevel(CourseLevel level) {
    return (root, query, cb) -> cb.equal(root.get("level"), level);
}
```

### Pattern 3: Service Layer Protection

```java
@Service
@Transactional(readOnly = true)
public class CourseAnalyticsService {

    @PreAuthorize("hasPermission(null, 'Course', 'course:ANALYTICS')")
    public CourseStatistics getStatistics(@RequestParam String timeframe) {
        // Security filter automatically applied
        Specification<Course> spec = EffectiveFilterSpecifications.applyCourseFilter();
        List<Course> accessibleCourses = courseRepository.findAll(spec);

        return CourseStatistics.builder()
            .totalCourses(accessibleCourses.size())
            .averageRating(calculateAverageRating(accessibleCourses))
            .totalEnrollments(calculateTotalEnrollments(accessibleCourses))
            .build();
    }
}
```

### Pattern 4: Individual Resource Access

```java
@GetMapping("/{courseId}")
@PreAuthorize("hasPermission(#courseId, 'Course', 'course:READ')")
public ResponseEntity<Course> getCourse(@PathVariable String courseId) {
    // For individual resources, you might want additional checks
    Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

    // Optional: Additional ownership verification
    if (EffectiveFilterContext.getCurrentFilter() == EffectiveFilter.OWN) {
        User currentUser = EffectiveFilterContext.getCurrentUser();
        if (!course.getInstructor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Not your course");
        }
    }

    return ResponseEntity.ok(course);
}
```

## ⚡ Performance Best Practices

### 1. Use Specifications for All Security-Sensitive Queries

```java
// ✅ Good: Security-aware query
public List<Course> findCourses() {
    Specification<Course> spec = EffectiveFilterSpecifications.applyCourseFilter();
    return courseRepository.findAll(spec);
}

// ❌ Bad: Bypasses security
public List<Course> findCourses() {
    return courseRepository.findAll(); // No security filtering!
}
```

### 2. Combine Filters Efficiently

```java
// ✅ Good: Single combined specification
Specification<Course> combinedSpec = EffectiveFilterSpecifications.and(
    EffectiveFilterSpecifications.applyCourseFilter(),
    hasCategory(categoryId),
    isNotDeleted()
);
List<Course> courses = courseRepository.findAll(combinedSpec);

// ❌ Bad: Multiple separate queries
List<Course> allCourses = courseRepository.findAll(securitySpec);
List<Course> filtered = allCourses.stream()
    .filter(course -> course.getCategory().getId().equals(categoryId))
    .filter(course -> !course.isDeleted())
    .collect(Collectors.toList());
```

### 3. Use Pagination for Large Datasets

```java
@GetMapping
@PreAuthorize("hasPermission(null, 'Course', 'course:READ')")
public ResponseEntity<Page<Course>> getCourses(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size) {

    Specification<Course> spec = EffectiveFilterSpecifications.applyCourseFilter();
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Course> courses = courseRepository.findAll(spec, pageable);

    return ResponseEntity.ok(courses);
}
```

## 🔍 Debugging and Troubleshooting

### Enable Debug Logging

```properties
# application.properties
logging.level.project.ktc.springboot_app.filter_rule=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### Common Debug Checkpoints

```java
// 1. Check if permission evaluator is called
@Component("rbacPermissionEvaluator")
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication auth, Object target, Object permission) {
        log.debug("=== Permission Check ===");
        log.debug("User: {}", auth.getName());
        log.debug("Target: {}", target);
        log.debug("Permission: {}", permission);

        // ... rest of method
    }
}

// 2. Check effective filter resolution
@Service
public class AuthorizationService {

    public EffectiveFilter getEffectiveFilter(User user, String permissionKey) {
        log.debug("=== Filter Resolution ===");
        log.debug("User: {}", user.getEmail());
        log.debug("Permission: {}", permissionKey);

        List<RoleFilterRule> rules = roleFilterRuleRepository
            .findActiveFilterRulesByUserAndPermission(user.getId(), permissionKey);

        log.debug("Found {} filter rules", rules.size());
        rules.forEach(rule ->
            log.debug("Rule: {} -> {}", rule.getId(), rule.getFilterType()));

        // ... rest of method
    }
}

// 3. Check context availability in specifications
public static Specification<Course> applyCourseFilter() {
    return (root, query, cb) -> {
        EffectiveFilter filter = EffectiveFilterContext.getCurrentFilter();
        User user = EffectiveFilterContext.getCurrentUser();

        log.debug("=== Specification Application ===");
        log.debug("Filter: {}", filter);
        log.debug("User: {}", user != null ? user.getEmail() : "null");
        log.debug("Has Context: {}", EffectiveFilterContext.hasContext());

        // ... rest of method
    };
}
```

### Troubleshooting Checklist

#### Problem: Permission Always Denied

- [ ] Check if user is authenticated (`authentication.isAuthenticated()`)
- [ ] Verify user has required role in database
- [ ] Confirm permission exists in database
- [ ] Check if filter rules are active (`is_active = true`)
- [ ] Verify @PreAuthorize annotation syntax

#### Problem: Wrong Data Returned

- [ ] Check effective filter type in logs
- [ ] Verify specification logic for each filter type
- [ ] Confirm database foreign key relationships
- [ ] Test with single role user first
- [ ] Check for data inconsistencies

#### Problem: Context Not Available

- [ ] Ensure method has @PreAuthorize annotation
- [ ] Verify CustomPermissionEvaluator is registered
- [ ] Check interceptor registration in WebConfig
- [ ] Confirm thread-local context timing

## 📚 Testing Examples

### Unit Test Examples

```java
@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private RoleFilterRuleRepository repository;

    @InjectMocks
    private AuthorizationService authorizationService;

    @Test
    void testGetEffectiveFilter_AdminUser_ReturnsAllFilter() {
        // Arrange
        User admin = createUser("admin", "ADMIN");
        RoleFilterRule rule = createFilterRule(RoleFilterRule.FilterType.ALL);
        when(repository.findActiveFilterRulesByUserAndPermission(admin.getId(), "course:READ"))
            .thenReturn(List.of(rule));

        // Act
        EffectiveFilter result = authorizationService.getEffectiveFilter(admin, "course:READ");

        // Assert
        assertEquals(EffectiveFilter.ALL, result);
    }

    @Test
    void testGetEffectiveFilter_MultipleRoles_ReturnsHighestPriority() {
        // Arrange
        User user = createUser("instructor", "INSTRUCTOR", "CONTENT_REVIEWER");
        List<RoleFilterRule> rules = List.of(
            createFilterRule(RoleFilterRule.FilterType.OWN),           // Priority 2
            createFilterRule(RoleFilterRule.FilterType.PUBLISHED_ONLY)  // Priority 1
        );
        when(repository.findActiveFilterRulesByUserAndPermission(user.getId(), "course:READ"))
            .thenReturn(rules);

        // Act
        EffectiveFilter result = authorizationService.getEffectiveFilter(user, "course:READ");

        // Assert
        assertEquals(EffectiveFilter.OWN, result); // Higher priority wins
    }
}
```

### Integration Test Examples

```java
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class RbacIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Order(1)
    void testAdminUserCanAccessAllCourses() {
        // Arrange
        String adminToken = authenticateUser("admin@test.com", "password");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Act
        ResponseEntity<List> response = restTemplate.exchange(
            "/api/courses", HttpMethod.GET, entity, List.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Should see all courses including unpublished ones
        assertTrue(response.getBody().size() > 0);
    }

    @Test
    @Order(2)
    void testStudentUserSeesOnlyPublishedCourses() {
        // Arrange
        String studentToken = authenticateUser("student@test.com", "password");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(studentToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Act
        ResponseEntity<List> response = restTemplate.exchange(
            "/api/courses", HttpMethod.GET, entity, List.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Should only see published courses
        // Verify by checking specific course properties if needed
    }

    @Test
    @Order(3)
    void testUnauthorizedUserGetsAccessDenied() {
        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/courses", String.class);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
```

This comprehensive guide should help developers effectively use and troubleshoot the RBAC system in their daily development work.
