package project.ktc.springboot_app.enrollment.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.enrollment.dto.EnrollmentResponseDto;
import project.ktc.springboot_app.enrollment.dto.MyEnrolledCourseDto;
import project.ktc.springboot_app.enrollment.entity.Enrollment;
import project.ktc.springboot_app.enrollment.interfaces.EnrollmentService;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;
import project.ktc.springboot_app.payment.repositories.PaymentRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.utils.SecurityUtil;
import project.ktc.springboot_app.utils.StringUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImp implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public ResponseEntity<ApiResponse<EnrollmentResponseDto>> enroll(String courseId) {

        // 0. Check Authenticated
        String currentUserId = SecurityUtil.getCurrentUserId();

        // 1. Fetch and validate course
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            return ApiResponseUtil.notFound("Course not found");
        }
        Course course = courseOpt.get();
        validateCourse(course);

        // 2. Fetch user
        Optional<User> userOpt = userRepository.findById(currentUserId);
        if (userOpt.isEmpty()) {
            return ApiResponseUtil.notFound("User not found");
        }
        User user = userOpt.get();

        // 3. Check if already enrolled
        if (enrollmentRepository.existsByUserIdAndCourseId(currentUserId, courseId)) {
            return ApiResponseUtil.conflict("User is already enrolled in this course");
        }

        // // 4. Handle payment for paid courses
        // if (course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
        // validatePayment(userId, courseId, course.getPrice());
        // }

        // 5. Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setCompletionStatus(Enrollment.CompletionStatus.IN_PROGRESS);

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Successfully enrolled user {} in course {}", currentUserId, courseId);
        EnrollmentResponseDto enrollmentResponse = EnrollmentResponseDto.builder()
                .courseId(course.getId())
                .title(course.getTitle())
                .enrollmentDate(savedEnrollment.getEnrolledAt())
                .build();
        return ApiResponseUtil.success(enrollmentResponse, "Successfully enrolled in the course");
    }

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<MyEnrolledCourseDto>>> getMyCourses(String status,
            Pageable pageable) {
        log.info("Fetching enrolled courses for current user with status: {} and page: {}", status, pageable);

        // Get current authenticated user ID
        String currentUserId = SecurityUtil.getCurrentUserId();

        // Fetch enrollments with pagination
        Page<Enrollment> enrollmentsPage = enrollmentRepository.findByUserIdWithCourseAndInstructor(
                currentUserId, status, pageable);

        // Convert to DTOs with progress calculation
        List<MyEnrolledCourseDto> enrolledCourses = enrollmentsPage.getContent().stream()
                .map(this::mapToMyEnrolledCourseDto)
                .collect(Collectors.toList());

        // Create paginated response
        PaginatedResponse<MyEnrolledCourseDto> paginatedResponse = PaginatedResponse.<MyEnrolledCourseDto>builder()
                .content(enrolledCourses)
                .page(PaginatedResponse.PageInfo.builder()
                        .number(enrollmentsPage.getNumber())
                        .size(enrollmentsPage.getSize())
                        .totalElements(enrollmentsPage.getTotalElements())
                        .totalPages(enrollmentsPage.getTotalPages())
                        .first(enrollmentsPage.isFirst())
                        .last(enrollmentsPage.isLast())
                        .build())
                .build();

        return ApiResponseUtil.success(paginatedResponse, "Enrolled courses retrieved successfully");
    }

    private MyEnrolledCourseDto mapToMyEnrolledCourseDto(Enrollment enrollment) {
        Course course = enrollment.getCourse();

        // Calculate progress
        Double progress = calculateProgress(enrollment.getUser().getId(), course.getId());

        return MyEnrolledCourseDto.builder()
                .courseId(course.getId())
                .title(course.getTitle())
                .thumbnailUrl(course.getThumbnailUrl())
                .slug(StringUtil.generateSlug(course.getTitle())) // We'll implement this method
                .level(course.getLevel())
                .price(course.getPrice())
                .progress(progress)
                .completionStatus(enrollment.getCompletionStatus().name())
                .instructor(MyEnrolledCourseDto.InstructorSummary.builder()
                        .id(course.getInstructor().getId())
                        .name(course.getInstructor().getName())
                        .avatar(course.getInstructor().getThumbnailUrl())
                        .build())
                .build();
    }

    private Double calculateProgress(String userId, String courseId) {
        try {
            Long completedLessons = enrollmentRepository.countCompletedLessonsByUserAndCourse(userId, courseId);
            Long totalLessons = enrollmentRepository.countTotalLessonsByCourse(courseId);

            if (totalLessons == null || totalLessons == 0) {
                return 0.0;
            }

            double progress = (double) completedLessons / totalLessons;
            return BigDecimal.valueOf(progress)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        } catch (Exception e) {
            log.warn("Failed to calculate progress for user {} and course {}: {}", userId, courseId, e.getMessage());
            return 0.0;
        }
    }

    private void validateCourse(Course course) {
        if (course.getIsDeleted() != null && course.getIsDeleted()) {
            ApiResponseUtil.notFound("Course has been deleted");
        }

        if (course.getIsPublished() != null && !course.getIsPublished()) {
            ApiResponseUtil.badRequest("Course is not published");
        }

        if (course.getIsApproved() != null && !course.getIsApproved()) {
            ApiResponseUtil.badRequest("Course is not approved");
        }
    }

    private void validatePayment(String userId, String courseId, BigDecimal coursePrice) {
        BigDecimal totalPaid = paymentRepository.getTotalPaidAmountByUserAndCourse(userId, courseId);

        if (totalPaid.compareTo(coursePrice) < 0) {
            ApiResponseUtil.badRequest(
                    String.format("Payment required. Course price: %s, Amount paid: %s",
                            coursePrice, totalPaid));
        }
    }

    @Override
    public void createEnrollmentFromWebhook(String userId, String courseId, String stripeSessionId) {
        log.info("Creating enrollment from webhook for user {} in course {} (Stripe session: {})",
                userId, courseId, stripeSessionId);

        try {
            // Fetch user and course
            Optional<User> userOpt = userRepository.findById(userId);
            Optional<Course> courseOpt = courseRepository.findById(courseId);

            if (userOpt.isEmpty()) {
                log.error("User not found: {}", userId);
                throw new RuntimeException("User not found: " + userId);
            }

            if (courseOpt.isEmpty()) {
                log.error("Course not found: {}", courseId);
                throw new RuntimeException("Course not found: " + courseId);
            }

            User user = userOpt.get();
            Course course = courseOpt.get();

            // Check if already enrolled
            if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
                log.warn("User {} is already enrolled in course {}", userId, courseId);
                return; // Don't create duplicate enrollment
            }

            // Create enrollment
            Enrollment enrollment = new Enrollment();
            enrollment.setId(java.util.UUID.randomUUID().toString());
            enrollment.setUser(user);
            enrollment.setCourse(course);
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setCompletionStatus(Enrollment.CompletionStatus.IN_PROGRESS);

            enrollmentRepository.save(enrollment);

            log.info("Enrollment successfully created for user {} in course {}", userId, courseId);

        } catch (Exception e) {
            log.error("Error creating enrollment from webhook for user {} and course {}: {}",
                    userId, courseId, e.getMessage(), e);
            throw new RuntimeException("Failed to create enrollment from webhook", e);
        }
    }
}
