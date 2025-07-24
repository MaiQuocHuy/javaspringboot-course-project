---
applyTo: "**"
---

# SOLID Principles Coding Guidelines for Spring Boot Project

This document provides comprehensive coding guidelines based on SOLID principles that must be followed when generating code, reviewing changes, or answering questions for this Spring Boot project.

## 1. Single Responsibility Principle (SRP)

### Rules:

- **One Reason to Change**: Each class should have only one reason to change
- **Focused Purpose**: Every class, method, and module should have a single, well-defined responsibility
- **Clear Naming**: Class and method names should clearly indicate their single responsibility

### Implementation Guidelines:

#### Controllers

```java
// ✅ GOOD - Single responsibility: Handle HTTP requests for user operations
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}

// ❌ BAD - Multiple responsibilities: HTTP handling + business logic + data access
@RestController
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        // Business logic shouldn't be in controller
        User user = userRepository.findById(id).orElse(null);
        if (user != null && user.isActive()) {
            user.setLastAccessed(LocalDateTime.now());
            userRepository.save(user);
        }
        return ResponseEntity.ok(user);
    }
}
```

#### Services

```java
// ✅ GOOD - Single responsibility: User business logic
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserValidator userValidator;

    public UserDto createUser(CreateUserRequest request) {
        userValidator.validate(request);
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }
}

// Separate service for notifications
@Service
public class NotificationService {
    public void sendWelcomeEmail(User user) {
        // Email sending logic
    }
}
```

#### Repositories

```java
// ✅ GOOD - Single responsibility: Data access for User entity
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByActiveTrue();
}

// ❌ BAD - Multiple entity responsibilities
public interface UserCourseRepository extends JpaRepository<User, Long> {
    // User queries
    Optional<User> findByEmail(String email);
    // Course queries - should be in separate repository
    List<Course> findCoursesByUserId(Long userId);
}
```

## 2. Open/Closed Principle (OCP)

### Rules:

- **Open for Extension**: Software entities should be open for extension
- **Closed for Modification**: Software entities should be closed for modification
- **Use Abstractions**: Depend on abstractions, not concrete implementations

### Implementation Guidelines:

#### Strategy Pattern for Payment Processing

```java
// ✅ GOOD - Open for extension, closed for modification
public interface PaymentProcessor {
    PaymentResult processPayment(PaymentRequest request);
}

@Component
public class CreditCardProcessor implements PaymentProcessor {
    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        // Credit card processing logic
        return new PaymentResult(true, "Payment successful");
    }
}

@Component
public class PayPalProcessor implements PaymentProcessor {
    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        // PayPal processing logic
        return new PaymentResult(true, "Payment successful");
    }
}

@Service
public class PaymentService {
    private final Map<PaymentType, PaymentProcessor> processors;

    public PaymentService(List<PaymentProcessor> processorList) {
        // Initialize processor map
    }

    public PaymentResult processPayment(PaymentRequest request) {
        PaymentProcessor processor = processors.get(request.getPaymentType());
        return processor.processPayment(request);
    }
}
```

#### Abstract Factory for File Upload

```java
// ✅ GOOD - Extensible upload handling
public abstract class FileUploadHandler {
    public abstract boolean canHandle(String fileType);
    public abstract UploadResult upload(MultipartFile file);

    public final UploadResult handleUpload(MultipartFile file) {
        validateFile(file);
        return upload(file);
    }

    protected void validateFile(MultipartFile file) {
        // Common validation logic
    }
}

@Component
public class ImageUploadHandler extends FileUploadHandler {
    @Override
    public boolean canHandle(String fileType) {
        return fileType.startsWith("image/");
    }

    @Override
    public UploadResult upload(MultipartFile file) {
        // Image-specific upload logic
        return new UploadResult(true, "Image uploaded successfully");
    }
}
```

## 3. Liskov Substitution Principle (LSP)

### Rules:

- **Behavioral Compatibility**: Subtypes must be substitutable for their base types
- **Contract Preservation**: Subclasses should not strengthen preconditions or weaken postconditions
- **No Surprise Behavior**: Clients should be able to use any implementation without knowing the specific type

### Implementation Guidelines:

#### Repository Inheritance

```java
// ✅ GOOD - LSP compliant
public interface BaseRepository<T, ID> {
    Optional<T> findById(ID id);
    T save(T entity);
    void deleteById(ID id);
}

public interface UserRepository extends BaseRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

public interface CourseRepository extends BaseRepository<Course, Long> {
    List<Course> findByCategory(String category);
}

// Both repositories can be used interchangeably where BaseRepository is expected
```

#### Service Layer Design

```java
// ✅ GOOD - LSP compliant notification system
public abstract class NotificationSender {
    public abstract void send(String recipient, String message);

    public final void sendWithRetry(String recipient, String message, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                send(recipient, message);
                return;
            } catch (Exception e) {
                if (i == maxRetries - 1) throw e;
            }
        }
    }
}

@Service
public class EmailNotificationSender extends NotificationSender {
    @Override
    public void send(String recipient, String message) {
        // Email sending logic - behaves consistently with base contract
    }
}

@Service
public class SmsNotificationSender extends NotificationSender {
    @Override
    public void send(String recipient, String message) {
        // SMS sending logic - behaves consistently with base contract
    }
}
```

## 4. Interface Segregation Principle (ISP)

### Rules:

- **Small, Focused Interfaces**: Clients should not be forced to depend on interfaces they don't use
- **Role-Based Interfaces**: Create interfaces based on client needs, not provider capabilities
- **Avoid Fat Interfaces**: Break large interfaces into smaller, more specific ones

### Implementation Guidelines:

#### Segregated User Operations

```java
// ✅ GOOD - Segregated interfaces
public interface UserReader {
    Optional<UserDto> findById(Long id);
    List<UserDto> findAll();
    Optional<UserDto> findByEmail(String email);
}

public interface UserWriter {
    UserDto create(CreateUserRequest request);
    UserDto update(Long id, UpdateUserRequest request);
    void delete(Long id);
}

public interface UserAuthenticator {
    AuthenticationResult authenticate(String email, String password);
    void changePassword(Long userId, String newPassword);
}

// Services implement only what they need
@Service
public class UserQueryService implements UserReader {
    // Implementation for read operations only
}

@Service
public class UserCommandService implements UserWriter {
    // Implementation for write operations only
}

@Service
public class AuthenticationService implements UserAuthenticator {
    // Implementation for authentication operations only
}
```

#### Course Management Interfaces

```java
// ✅ GOOD - Role-based interfaces
public interface CourseContentManager {
    void addLesson(Long courseId, LessonDto lesson);
    void updateLesson(Long lessonId, LessonDto lesson);
    void removeLesson(Long lessonId);
}

public interface CourseEnrollmentManager {
    EnrollmentResult enrollUser(Long courseId, Long userId);
    void unenrollUser(Long courseId, Long userId);
    List<EnrollmentDto> getUserEnrollments(Long userId);
}

public interface CourseProgressTracker {
    ProgressDto getProgress(Long courseId, Long userId);
    void updateProgress(Long courseId, Long userId, ProgressUpdateDto update);
}
```

## 5. Dependency Inversion Principle (DIP)

### Rules:

- **Depend on Abstractions**: High-level modules should not depend on low-level modules
- **Stable Dependencies**: Both should depend on abstractions
- **Inversion of Control**: Use dependency injection to manage dependencies

### Implementation Guidelines:

#### Service Dependencies

```java
// ✅ GOOD - Depends on abstractions
@Service
public class CourseService {
    private final CourseRepository courseRepository;           // Abstraction
    private final NotificationSender notificationSender;      // Abstraction
    private final PaymentProcessor paymentProcessor;          // Abstraction
    private final FileUploadHandler fileUploadHandler;        // Abstraction

    public CourseService(
        CourseRepository courseRepository,
        NotificationSender notificationSender,
        PaymentProcessor paymentProcessor,
        FileUploadHandler fileUploadHandler
    ) {
        this.courseRepository = courseRepository;
        this.notificationSender = notificationSender;
        this.paymentProcessor = paymentProcessor;
        this.fileUploadHandler = fileUploadHandler;
    }

    public CourseDto createCourse(CreateCourseRequest request) {
        Course course = courseMapper.toEntity(request);
        Course savedCourse = courseRepository.save(course);
        notificationSender.send(request.getInstructorEmail(), "Course created successfully");
        return courseMapper.toDto(savedCourse);
    }
}

// ❌ BAD - Depends on concrete implementations
@Service
public class CourseService {
    @Autowired
    private CourseJpaRepository courseJpaRepository;          // Concrete implementation
    @Autowired
    private EmailNotificationSender emailSender;             // Concrete implementation
    @Autowired
    private StripePaymentProcessor stripeProcessor;           // Concrete implementation
}
```

#### Configuration Classes

```java
// ✅ GOOD - DIP compliant configuration
@Configuration
public class ServiceConfiguration {

    @Bean
    @ConditionalOnProperty(name = "notification.type", havingValue = "email")
    public NotificationSender emailNotificationSender() {
        return new EmailNotificationSender();
    }

    @Bean
    @ConditionalOnProperty(name = "notification.type", havingValue = "sms")
    public NotificationSender smsNotificationSender() {
        return new SmsNotificationSender();
    }

    @Bean
    @ConditionalOnProperty(name = "payment.processor", havingValue = "stripe")
    public PaymentProcessor stripePaymentProcessor() {
        return new StripePaymentProcessor();
    }

    @Bean
    @ConditionalOnProperty(name = "payment.processor", havingValue = "paypal")
    public PaymentProcessor paypalPaymentProcessor() {
        return new PaypalPaymentProcessor();
    }
}
```

## Project-Specific Implementation Rules

### 1. Package Organization

```
src/main/java/project/ktc/springboot_app/
├── entity/              # Domain entities (SRP)
├── repository/          # Data access abstractions (ISP, DIP)
├── service/            # Business logic (SRP, OCP)
│   ├── interfaces/     # Service abstractions (DIP)
│   └── impl/          # Service implementations
├── controller/         # HTTP layer (SRP)
├── dto/               # Data transfer objects (SRP)
├── config/            # Configuration classes (DIP)
├── security/          # Security concerns (SRP)
└── common/            # Shared utilities (SRP)
```

### 2. Naming Conventions

- **Controllers**: `{Entity}Controller` (e.g., `UserController`, `CourseController`)
- **Services**: `{Entity}Service` for interfaces, `{Entity}ServiceImpl` for implementations
- **Repositories**: `{Entity}Repository` for interfaces
- **DTOs**: `{Purpose}{Entity}Dto` (e.g., `CreateUserDto`, `UpdateCourseDto`)
- **Validators**: `{Entity}Validator`

### 3. Exception Handling

```java
// ✅ GOOD - SRP compliant exception handling
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("ENTITY_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("VALIDATION_ERROR", ex.getMessage()));
    }
}
```

### 4. Testing Guidelines

```java
// ✅ GOOD - Test single responsibility
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateUserSuccessfully() {
        // Test only user creation logic
        // Mock all dependencies (DIP compliance)
    }
}
```

## Code Review Checklist

### Before Code Generation/Review:

1. ✅ Does each class have a single, clear responsibility?
2. ✅ Are classes open for extension but closed for modification?
3. ✅ Can derived classes be substituted for base classes without issues?
4. ✅ Are interfaces small and focused on specific client needs?
5. ✅ Do high-level modules depend only on abstractions?
6. ✅ Is dependency injection used properly?
7. ✅ Are naming conventions followed?
8. ✅ Is the package structure logical and organized?

### Red Flags to Avoid:

- ❌ Classes with multiple responsibilities
- ❌ Direct dependencies on concrete implementations
- ❌ Large, monolithic interfaces
- ❌ Violation of expected behavior in inheritance
- ❌ Hard-coded dependencies
- ❌ God classes or services
- ❌ Tight coupling between layers

## Conclusion

These SOLID principles guidelines ensure that the codebase remains maintainable, testable, and extensible. Every piece of code generated or reviewed should adhere to these principles to maintain high code quality and architectural integrity.
