package project.ktc.springboot_app.enrollment.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.cache.services.domain.CoursesCacheService;
import project.ktc.springboot_app.cache.services.infrastructure.CacheInvalidationService;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.enrollment.dto.EnrollmentResponseDto;
import project.ktc.springboot_app.enrollment.dto.MyEnrolledCourseDto;
import project.ktc.springboot_app.enrollment.dto.StudentActivityDto;
import project.ktc.springboot_app.enrollment.dto.StudentDashboardStatsDto;
import project.ktc.springboot_app.enrollment.entity.Enrollment;
import project.ktc.springboot_app.enrollment.interfaces.EnrollmentService;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;
import project.ktc.springboot_app.notification.utils.NotificationHelper;
import project.ktc.springboot_app.payment.repositories.PaymentRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.utils.SecurityUtil;
import project.ktc.springboot_app.utils.StringUtil;
import project.ktc.springboot_app.lesson.repositories.LessonCompletionRepository;
import project.ktc.springboot_app.quiz.repositories.QuizResultRepository;
import project.ktc.springboot_app.entity.LessonCompletion;
import project.ktc.springboot_app.entity.QuizResult;

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
    private final NotificationHelper notificationHelper;
    private final CoursesCacheService coursesCacheService;
    private final CacheInvalidationService cacheInvalidationService;
    private final LessonCompletionRepository lessonCompletionRepository;
    private final QuizResultRepository quizResultRepository;

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

        // Check and invalidate cache if enrollment changes exceed 5%
        long previousEnrollmentCount = enrollmentRepository.countByCourseId(courseId) - 1; // Subtract the current
                                                                                           // enrollment
        long currentEnrollmentCount = enrollmentRepository.countByCourseId(courseId);
        coursesCacheService.checkAndInvalidateForEnrollmentChange(courseId, course.getSlug(),
                previousEnrollmentCount, currentEnrollmentCount);

        EnrollmentResponseDto enrollmentResponse = EnrollmentResponseDto.builder()
                .courseId(course.getId())
                .title(course.getTitle())
                .enrollmentDate(savedEnrollment.getEnrolledAt())
                .build();
        return ApiResponseUtil.success(enrollmentResponse, "Successfully enrolled in the course");
    }

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<MyEnrolledCourseDto>>> getMyCourses(
            Enrollment.CompletionStatus status,
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

    @Override
    public ResponseEntity<ApiResponse<List<MyEnrolledCourseDto>>> getMyCourses(Enrollment.CompletionStatus status) {
        log.info("Fetching all enrolled courses for current user with status: {}", status);

        // Get current authenticated user ID
        String currentUserId = SecurityUtil.getCurrentUserId();

        // Fetch all enrollments without pagination
        List<Enrollment> enrollments = enrollmentRepository.findByUserIdWithCourseAndInstructor(currentUserId, status);

        // Convert to DTOs with progress calculation
        List<MyEnrolledCourseDto> enrolledCourses = enrollments.stream()
                .map(this::mapToMyEnrolledCourseDto)
                .collect(Collectors.toList());

        return ApiResponseUtil.success(enrolledCourses, "Enrolled courses retrieved successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<List<MyEnrolledCourseDto>>> getRecentCourses() {
        try {
            String currentUserId = SecurityUtil.getCurrentUserId();
            log.info("Fetching 3 most recent enrolled courses for user: {}", currentUserId);

            List<Enrollment> recentEnrollments = enrollmentRepository.findTop3RecentEnrollmentsByUserId(currentUserId)
                    .stream()
                    .limit(3)
                    .collect(Collectors.toList());

            List<MyEnrolledCourseDto> recentCourses = recentEnrollments.stream()
                    .map(this::mapToMyEnrolledCourseDto)
                    .collect(Collectors.toList());

            log.info("Successfully fetched {} recent courses for user: {}", recentCourses.size(), currentUserId);
            return ApiResponseUtil.success(recentCourses, "Recent courses retrieved successfully");

        } catch (Exception e) {
            log.error("Error fetching recent courses for current user: {}", e.getMessage());
            return ApiResponseUtil.error(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to fetch recent courses");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<StudentDashboardStatsDto>> getDashboardStats() {
        try {
            String currentUserId = SecurityUtil.getCurrentUserId();
            log.info("Fetching dashboard statistics for user: {}", currentUserId);

            // Get total courses enrolled
            Long totalCourses = enrollmentRepository.countTotalEnrollmentsByUserId(currentUserId);

            // Get completed courses
            Long completedCourses = enrollmentRepository.countEnrollmentsByUserIdAndStatus(
                    currentUserId, Enrollment.CompletionStatus.COMPLETED);

            // Get in-progress courses
            Long inProgressCourses = enrollmentRepository.countEnrollmentsByUserIdAndStatus(
                    currentUserId, Enrollment.CompletionStatus.IN_PROGRESS);

            // Get total lessons completed across all enrolled courses
            Long lessonsCompleted = enrollmentRepository.countTotalCompletedLessonsByUserId(currentUserId);

            // Get total lessons in all enrolled courses
            Long totalLessons = enrollmentRepository.countTotalLessonsInEnrolledCoursesByUserId(currentUserId);

            // Calculate overall progress
            Double overallProgress = 0.0;
            if (totalLessons != null && totalLessons > 0 && lessonsCompleted != null) {
                overallProgress = BigDecimal.valueOf((double) lessonsCompleted / totalLessons)
                        .setScale(4, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            StudentDashboardStatsDto stats = StudentDashboardStatsDto.builder()
                    .totalCourses(totalCourses != null ? totalCourses : 0L)
                    .completedCourses(completedCourses != null ? completedCourses : 0L)
                    .inProgressCourses(inProgressCourses != null ? inProgressCourses : 0L)
                    .lessonsCompleted(lessonsCompleted != null ? lessonsCompleted : 0L)
                    .totalLessons(totalLessons != null ? totalLessons : 0L)
                    .build();

            log.info(
                    "Successfully fetched dashboard statistics for user: {} - {} total courses, {} completed, {} in progress, {} lessons completed out of {}",
                    currentUserId, stats.getTotalCourses(), stats.getCompletedCourses(),
                    stats.getInProgressCourses(), stats.getLessonsCompleted(), stats.getTotalLessons());

            return ApiResponseUtil.success(stats, "Dashboard statistics retrieved successfully");

        } catch (Exception e) {
            log.error("Error fetching dashboard statistics for current user: {}", e.getMessage());
            return ApiResponseUtil.error(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to fetch dashboard statistics");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<List<StudentActivityDto>>> getRecentActivities(Integer limit) {
        try {
            String currentUserId = SecurityUtil.getCurrentUserId();
            log.info("Fetching recent activities for user: {} with limit: {}", currentUserId, limit);

            int activityLimit = limit != null && limit > 0 ? limit : 20; // Default to 20 activities
            int perTypeLimit = Math.max(1, activityLimit / 3); // Distribute equally among 3 activity types

            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0,
                    perTypeLimit);

            // Fetch recent enrollments
            List<Enrollment> recentEnrollments = enrollmentRepository.findRecentEnrollmentsByUserId(currentUserId,
                    pageable);
            List<StudentActivityDto> enrollmentActivities = recentEnrollments.stream()
                    .map(this::mapEnrollmentToActivity)
                    .collect(Collectors.toList());

            // Fetch recent lesson completions
            List<LessonCompletion> recentCompletions = lessonCompletionRepository
                    .findRecentCompletionsByUserId(currentUserId, pageable);
            List<StudentActivityDto> completionActivities = recentCompletions.stream()
                    .map(this::mapLessonCompletionToActivity)
                    .collect(Collectors.toList());

            // Fetch recent quiz submissions
            List<QuizResult> recentSubmissions = quizResultRepository.findRecentSubmissionsByUserId(currentUserId,
                    pageable);
            List<StudentActivityDto> submissionActivities = recentSubmissions.stream()
                    .map(this::mapQuizResultToActivity)
                    .collect(Collectors.toList());

            // Combine all activities
            List<StudentActivityDto> allActivities = new java.util.ArrayList<>();
            allActivities.addAll(enrollmentActivities);
            allActivities.addAll(completionActivities);
            allActivities.addAll(submissionActivities);

            // Sort by timestamp (most recent first) and limit
            List<StudentActivityDto> sortedActivities = allActivities.stream()
                    .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                    .limit(activityLimit)
                    .collect(Collectors.toList());

            log.info("Successfully fetched {} recent activities for user: {}", sortedActivities.size(), currentUserId);
            return ApiResponseUtil.success(sortedActivities, "Recent activities retrieved successfully");

        } catch (Exception e) {
            log.error("Error fetching recent activities for current user: {}", e.getMessage());
            return ApiResponseUtil.error(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to fetch recent activities");
        }
    }

    private StudentActivityDto mapEnrollmentToActivity(Enrollment enrollment) {
        return StudentActivityDto.builder()
                .activityType(StudentActivityDto.ActivityType.ENROLLMENT.name())
                .title(StudentActivityDto.ActivityType.ENROLLMENT.getDisplayName())
                .description("Enrolled in " + enrollment.getCourse().getTitle())
                .timestamp(enrollment.getEnrolledAt())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .build();
    }

    private StudentActivityDto mapLessonCompletionToActivity(LessonCompletion completion) {
        return StudentActivityDto.builder()
                .activityType(StudentActivityDto.ActivityType.LESSON_COMPLETION.name())
                .title(StudentActivityDto.ActivityType.LESSON_COMPLETION.getDisplayName())
                .description("Completed lesson: " + completion.getLesson().getTitle())
                .timestamp(completion.getCompletedAt())
                .courseId(completion.getLesson().getSection().getCourse().getId())
                .courseTitle(completion.getLesson().getSection().getCourse().getTitle())
                .lessonId(completion.getLesson().getId())
                .lessonTitle(completion.getLesson().getTitle())
                .build();
    }

    private StudentActivityDto mapQuizResultToActivity(QuizResult quizResult) {
        double scorePercentage = quizResult.getScore() != null ? quizResult.getScore().doubleValue() : 0.0;
        return StudentActivityDto.builder()
                .activityType(StudentActivityDto.ActivityType.QUIZ_SUBMISSION.name())
                .title(StudentActivityDto.ActivityType.QUIZ_SUBMISSION.getDisplayName())
                .description("Submitted quiz for lesson: " + quizResult.getLesson().getTitle())
                .timestamp(quizResult.getCompletedAt())
                .courseId(quizResult.getLesson().getSection().getCourse().getId())
                .courseTitle(quizResult.getLesson().getSection().getCourse().getTitle())
                .lessonId(quizResult.getLesson().getId())
                .lessonTitle(quizResult.getLesson().getTitle())
                .score(scorePercentage)
                .build();
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
                .enrolledAt(enrollment.getEnrolledAt())
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
            long previousEnrollmentCount = enrollmentRepository.countByCourseId(courseId) - 1; // Subtract the current
            // enrollment
            long currentEnrollmentCount = enrollmentRepository.countByCourseId(courseId);

            coursesCacheService.checkAndInvalidateForEnrollmentChange(courseId, course.getSlug(),
                    previousEnrollmentCount, currentEnrollmentCount);
            cacheInvalidationService.invalidateInstructorStatisticsOnEnrollment(course.getInstructor().getId());

            notificationHelper.createInstructorNewStudentEnrollmentNotification(
                    enrollment.getCourse().getInstructor().getId(), enrollment.getCourse().getId(),
                    enrollment.getCourse().getTitle(), enrollment.getUser().getName(), enrollment.getUser().getId(),
                    enrollment.getId());

            log.info("Enrollment successfully created for user {} in course {}", userId, courseId);

        } catch (Exception e) {
            log.error("Error creating enrollment from webhook for user {} and course {}: {}",
                    userId, courseId, e.getMessage(), e);
            throw new RuntimeException("Failed to create enrollment from webhook", e);
        }
    }
}
