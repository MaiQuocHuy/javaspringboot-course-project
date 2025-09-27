package project.ktc.springboot_app.lesson.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.cache.services.domain.CoursesCacheService;
import project.ktc.springboot_app.certificate.dto.CertificateResponseDto;
import project.ktc.springboot_app.certificate.dto.CreateCertificateDto;
import project.ktc.springboot_app.certificate.interfaces.CertificateService;
import project.ktc.springboot_app.certificate.services.CertificateAsyncService;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.enrollment.entity.Enrollment;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;
import project.ktc.springboot_app.entity.LessonCompletion;
import project.ktc.springboot_app.entity.QuizQuestion;
import project.ktc.springboot_app.entity.QuizResult;
import project.ktc.springboot_app.lesson.entity.Lesson;
import project.ktc.springboot_app.lesson.interfaces.StudentService;
import project.ktc.springboot_app.lesson.repositories.InstructorLessonRepository;
import project.ktc.springboot_app.lesson.repositories.LessonCompletionRepository;
import project.ktc.springboot_app.notification.utils.NotificationHelper;
import project.ktc.springboot_app.quiz.dto.QuizSubmissionResponseDto;
import project.ktc.springboot_app.quiz.dto.SubmitQuizDto;
import project.ktc.springboot_app.quiz.repositories.QuizQuestionRepository;
import project.ktc.springboot_app.quiz.repositories.QuizResultRepository;
import project.ktc.springboot_app.section.entity.Section;
import project.ktc.springboot_app.section.repositories.InstructorSectionRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.utils.SecurityUtil;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
@Slf4j
@Service
public class StudentLessonServiceImp implements StudentService {

    private final LessonCompletionRepository lessonCompletionRepository;
    private final InstructorSectionRepository sectionRepository;
    private final InstructorLessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final ObjectMapper objectMapper;
    private final CertificateService certificateService;
    private final CoursesCacheService coursesCacheService;
    private final CertificateAsyncService certificateAsyncService;
    private final Executor taskExecutor;
    private final NotificationHelper notificationHelper;

    /**
     * Mark a lesson as completed by the current student.
     * 
     * This method handles lesson completion with the following logic:
     * - If lesson was previously completed, it updates the completion timestamp
     * - If first completion, it creates a new lesson completion record
     * - Checks and updates course completion status if all lessons are completed
     * - Idempotent operation - can be called multiple times safely
     * 
     * @param sectionId The ID of the section containing the lesson
     * @param lessonId  The ID of the lesson to mark as completed
     * @return ResponseEntity indicating success or failure
     */
    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> completeLesson(String sectionId, String lessonId) {
        log.info("Student completion request for lesson {} in section {}", lessonId, sectionId);

        try {
            String currentUserId = SecurityUtil.getCurrentUserId();

            // Verify section exists
            Optional<Section> sectionOpt = sectionRepository.findById(sectionId);
            if (sectionOpt.isEmpty()) {
                return ApiResponseUtil.notFound("Section not found with id: " + sectionId);
            }

            Section section = sectionOpt.get();
            String courseId = section.getCourse().getId();

            // Check if student is enrolled in the course (not section)
            if (!enrollmentRepository.existsByUserIdAndCourseId(currentUserId, courseId)) {
                return ApiResponseUtil.forbidden("You must be enrolled in the course to complete lessons");
            }

            // Verify lesson exists and belongs to the section
            Optional<Lesson> lessonOpt = lessonRepository.findById(lessonId);
            if (lessonOpt.isEmpty()) {
                return ApiResponseUtil.notFound("Lesson not found with id: " + lessonId);
            }

            Lesson lesson = lessonOpt.get();
            if (!lesson.getSection().getId().equals(sectionId)) {
                return ApiResponseUtil.badRequest("Lesson does not belong to the specified section");
            }

            // Check if lesson is already completed by the student
            Optional<LessonCompletion> existingCompletionOpt = lessonCompletionRepository
                    .findByUserIdAndLessonId(currentUserId, lessonId);

            // Get student user entity
            Optional<User> userOpt = userRepository.findById(currentUserId);
            if (userOpt.isEmpty()) {
                return ApiResponseUtil.notFound("User not found with id: " + currentUserId);
            }
            User student = userOpt.get();

            LessonCompletion completion;
            if (existingCompletionOpt.isPresent()) {
                // Update existing completion time (idempotent operation)
                completion = existingCompletionOpt.get();
                completion.setCompletedAt(LocalDateTime.now());
                log.info("Updating existing lesson completion time for student {} on lesson {}", currentUserId,
                        lessonId);
            } else {
                // Create new lesson completion record
                completion = new LessonCompletion();
                completion.setUser(student);
                completion.setLesson(lesson);
                completion.setCompletedAt(LocalDateTime.now());
                log.info("Creating new lesson completion for student {} on lesson {}", currentUserId, lessonId);
            }

            lessonCompletionRepository.save(completion);
            log.info("Successfully recorded lesson completion for student {} on lesson {}", currentUserId, lessonId);

            // Invalidate course structure cache since lesson completion affects course
            // progress
            try {
                coursesCacheService.invalidateCourseStructure(courseId);
                log.debug("Invalidated course structure cache for course: {}", courseId);
            } catch (Exception e) {
                log.warn("Failed to invalidate course structure cache for course: {}, error: {}", courseId,
                        e.getMessage());
                // Don't fail the request if cache invalidation fails
            }

            // Check if all lessons in the course are completed and update enrollment status
            checkAndUpdateCourseCompletion(currentUserId, courseId);

            return ApiResponseUtil.success("Lesson completion recorded successfully",
                    "Lesson marked as complete");

        } catch (Exception e) {
            log.error("Error completing lesson {} for student: {}", lessonId, e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to record lesson completion: " + e.getMessage());
        }
    }

    private void checkAndUpdateCourseCompletion(String userId, String courseId) {
        try {
            // Count total lessons in the course
            Long totalLessons = enrollmentRepository.countTotalLessonsByCourse(courseId);

            // Count completed lessons by the user in this course
            Long completedLessons = enrollmentRepository.countCompletedLessonsByUserAndCourse(userId, courseId);

            log.debug("Course {} - Total lessons: {}, Completed by user {}: {}",
                    courseId, totalLessons, userId, completedLessons);

            // If all lessons are completed, update enrollment status
            if (totalLessons > 0 && completedLessons.equals(totalLessons)) {
                Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
                if (enrollmentOpt.isPresent()) {
                    Enrollment enrollment = enrollmentOpt.get();
                    if (enrollment.getCompletionStatus() != Enrollment.CompletionStatus.COMPLETED) {
                        enrollment.setCompletionStatus(Enrollment.CompletionStatus.COMPLETED);
                        enrollmentRepository.save(enrollment);
                        log.info("Updated enrollment status to COMPLETED for user {} in course {}", userId, courseId);

                        CreateCertificateDto dto = new CreateCertificateDto();
                        dto.setUserId(userId);
                        dto.setCourseId(courseId);

                        ResponseEntity<ApiResponse<CertificateResponseDto>> createdCertificate = certificateService
                                .createCertificate(dto);

                        try {
                            String courseName = enrollment.getCourse().getTitle();
                            String certificateUrl = "/dashboard/certificates/" + courseId;

                            // ✅ Extract variables để tránh multiple calls
                            ApiResponse<CertificateResponseDto> responseBody = createdCertificate != null
                                    ? createdCertificate.getBody()
                                    : null;
                            CertificateResponseDto certificateData = responseBody != null ? responseBody.getData()
                                    : null;
                            String certificateId = certificateData != null ? certificateData.getId() : null;

                            if (certificateId != null) {
                                notificationHelper.createCertificateNotification(
                                        userId,
                                        certificateId,
                                        courseName,
                                        certificateUrl)
                                        .thenAccept(notification -> log.info(
                                                "✅ Certificate notification created for student {} (course: {}): {}",
                                                userId, courseId, notification.getId()))
                                        .exceptionally(ex -> {
                                            log.error(
                                                    "❌ Failed to create certificate notification for student {} (course: {}): {}",
                                                    userId, courseId, ex.getMessage(), ex);
                                            return null;
                                        });
                            } else {
                                log.error("❌ Certificate creation returned null/invalid response for user {} course {}",
                                        userId, courseId);
                            }

                            log.info("🎓 Student {} completed course {} - certificate created and notification sent",
                                    userId, courseName);

                        } catch (Exception notificationError) {
                            log.error("❌ Failed to create certificate notification: {}",
                                    notificationError.getMessage(), notificationError);
                            // Continue execution even if notification fails
                        }

                    }
                }
            }

            // certificateAsyncService.processCertificateAsync();

        } catch (Exception e) {
            log.error("Error checking course completion for user {} in course {}: {}",
                    userId, courseId, e.getMessage(), e);
        }
    }

    /**
     * Submit quiz answers for a lesson and record completion.
     * 
     * This method handles quiz submission with the following logic:
     * - If quiz was previously submitted, it updates the existing result and
     * completion time
     * - If first submission, it creates new quiz result and lesson completion
     * records
     * - Automatically marks lesson as completed after successful quiz submission
     * - Checks and updates course completion status if all lessons are completed
     * 
     * @param sectionId     The ID of the section containing the lesson
     * @param lessonId      The ID of the quiz lesson
     * @param submitQuizDto DTO containing the submitted answers
     * @return ResponseEntity with quiz submission result and feedback
     */
    @Override
    @Transactional
    public ResponseEntity<ApiResponse<QuizSubmissionResponseDto>> submitQuiz(String sectionId, String lessonId,
            SubmitQuizDto submitQuizDto) {
        log.info("Quiz submission request for lesson {} in section {} by student", lessonId, sectionId);

        try {
            String currentUserId = SecurityUtil.getCurrentUserId();

            // 1. Verify section exists
            Optional<Section> sectionOpt = sectionRepository.findById(sectionId);
            if (sectionOpt.isEmpty()) {
                return ApiResponseUtil.notFound("Section not found with id: " + sectionId);
            }

            Section section = sectionOpt.get();
            String courseId = section.getCourse().getId();

            // 2. Check if student is enrolled in the course
            if (!enrollmentRepository.existsByUserIdAndCourseId(currentUserId, courseId)) {
                return ApiResponseUtil.forbidden("You must be enrolled in the course to submit quizzes");
            }

            // 3. Verify lesson exists and belongs to the section
            Optional<Lesson> lessonOpt = lessonRepository.findById(lessonId);
            if (lessonOpt.isEmpty()) {
                return ApiResponseUtil.notFound("Lesson not found with id: " + lessonId);
            }

            Lesson lesson = lessonOpt.get();
            if (!lesson.getSection().getId().equals(sectionId)) {
                return ApiResponseUtil.badRequest("Lesson does not belong to the specified section");
            }

            // 4. Verify lesson is of type QUIZ
            if (!"type-002".equals(lesson.getLessonType().getId())) {
                return ApiResponseUtil.badRequest("This lesson is not a quiz lesson");
            }

            // 5. Get quiz questions for this lesson
            List<QuizQuestion> quizQuestions = quizQuestionRepository.findByLessonId(lessonId);
            if (quizQuestions.isEmpty()) {
                return ApiResponseUtil.notFound("No quiz questions found for this lesson");
            }

            // 6. Validate submitted answers match question IDs
            Map<String, String> submittedAnswers = submitQuizDto.getAnswers();
            for (QuizQuestion question : quizQuestions) {
                if (!submittedAnswers.containsKey(question.getId())) {
                    return ApiResponseUtil.badRequest("Missing answer for question: " + question.getId());
                }
            }

            // 7. Calculate score
            int totalQuestions = quizQuestions.size();
            int correctAnswers = 0;

            for (QuizQuestion question : quizQuestions) {
                String submittedAnswer = submittedAnswers.get(question.getId());
                if (question.getCorrectAnswer().equals(submittedAnswer)) {
                    correctAnswers++;
                }
            }

            // Calculate score as percentage
            BigDecimal score = BigDecimal.valueOf(correctAnswers)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalQuestions), 2, RoundingMode.HALF_UP);

            // 8. Get or create student user entity
            Optional<User> userOpt = userRepository.findById(currentUserId);
            if (userOpt.isEmpty()) {
                return ApiResponseUtil.notFound("User not found with id: " + currentUserId);
            }
            User student = userOpt.get();

            // 9. Check if quiz result already exists (for overwrite)
            Optional<QuizResult> existingResultOpt = quizResultRepository.findByUserIdAndLessonId(currentUserId,
                    lessonId);

            QuizResult quizResult;
            if (existingResultOpt.isPresent()) {
                // Update existing result - allows multiple quiz attempts
                quizResult = existingResultOpt.get();
                log.info("Overwriting existing quiz result for student {} on lesson {}", currentUserId, lessonId);
            } else {
                // Create new result
                quizResult = new QuizResult();
                quizResult.setUser(student);
                quizResult.setLesson(lesson);
                log.info("Creating new quiz result for student {} on lesson {}", currentUserId, lessonId);
            }

            // 10. Save quiz result
            quizResult.setScore(score);
            quizResult.setAnswers(convertAnswersToJson(submittedAnswers));
            quizResult.setCompletedAt(LocalDateTime.now());

            quizResultRepository.save(quizResult);

            // 11. Generate feedback
            String feedback = generateFeedback(correctAnswers, totalQuestions, score);

            // 12. Create response
            QuizSubmissionResponseDto responseDto = QuizSubmissionResponseDto.builder()
                    .score(score)
                    .totalQuestions(totalQuestions)
                    .correctAnswers(correctAnswers)
                    .feedback(feedback)
                    .submittedAt(quizResult.getCompletedAt())
                    .build();

            log.info("Successfully processed quiz submission for student {} on lesson {}. Score: {}/{}",
                    currentUserId, lessonId, correctAnswers, totalQuestions);

            return ApiResponseUtil.success(responseDto, "Quiz submitted successfully");

        } catch (Exception e) {
            log.error("Error submitting quiz for lesson {} by student: {}", lessonId, e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to submit quiz: " + e.getMessage());
        }
    }

    /**
     * Convert answers map to JSON string
     */
    private String convertAnswersToJson(Map<String, String> answers) {
        try {
            return objectMapper.writeValueAsString(answers);
        } catch (JsonProcessingException e) {
            log.error("Error converting answers to JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to convert answers to JSON", e);
        }
    }

    /**
     * Generate feedback message based on performance
     */
    private String generateFeedback(int correctAnswers, int totalQuestions, BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return String.format("Excellent work! You answered %d out of %d questions correctly.", correctAnswers,
                    totalQuestions);
        } else if (score.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return String.format("Great job! You answered %d out of %d questions correctly.", correctAnswers,
                    totalQuestions);
        } else if (score.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return String.format(
                    "Good effort! You answered %d out of %d questions correctly. Consider reviewing the material.",
                    correctAnswers, totalQuestions);
        } else {
            return String.format(
                    "You answered %d out of %d questions correctly. Please review the material and try again.",
                    correctAnswers, totalQuestions);
        }
    }

}
