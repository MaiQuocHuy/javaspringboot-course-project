package project.ktc.springboot_app.lesson.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.enrollment.entity.Enrollment;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;
import project.ktc.springboot_app.entity.LessonCompletion;
import project.ktc.springboot_app.lesson.entity.Lesson;
import project.ktc.springboot_app.lesson.interfaces.StudentService;
import project.ktc.springboot_app.lesson.repositories.InstructorLessonRepository;
import project.ktc.springboot_app.lesson.repositories.LessonCompletionRepository;
import project.ktc.springboot_app.section.entity.Section;
import project.ktc.springboot_app.section.repositories.InstructorSectionRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.utils.SecurityUtil;

@RequiredArgsConstructor
@Slf4j
@Service
public class StudentLessonServiceImp implements StudentService {

    private final LessonCompletionRepository lessonCompletionRepository;
    private final InstructorSectionRepository sectionRepository;
    private final InstructorLessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

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

            // Check if lesson is already completed by the student (idempotent operation)
            boolean alreadyCompleted = lessonCompletionRepository.existsByUserIdAndLessonId(currentUserId, lessonId);
            if (alreadyCompleted) {
                log.info("Lesson {} already completed by student {}", lessonId, currentUserId);
                return ApiResponseUtil.success("Lesson completion already recorded",
                        "Lesson marked as complete");
            }

            // Get student user entity
            Optional<User> userOpt = userRepository.findById(currentUserId);
            if (userOpt.isEmpty()) {
                return ApiResponseUtil.notFound("User not found with id: " + currentUserId);
            }
            User student = userOpt.get();

            // Create lesson completion record
            LessonCompletion completion = new LessonCompletion();
            completion.setUser(student);
            completion.setLesson(lesson);
            completion.setCompletedAt(LocalDateTime.now());

            lessonCompletionRepository.save(completion);
            log.info("Successfully recorded lesson completion for student {} on lesson {}", currentUserId, lessonId);

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
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error checking course completion for user {} in course {}: {}",
                    userId, courseId, e.getMessage(), e);
        }
    }

}
