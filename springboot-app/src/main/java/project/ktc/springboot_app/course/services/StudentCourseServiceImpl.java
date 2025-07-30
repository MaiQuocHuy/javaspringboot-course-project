package project.ktc.springboot_app.course.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.interfaces.StudentCourseService;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;
import project.ktc.springboot_app.entity.QuizQuestion;
import project.ktc.springboot_app.lesson.entity.Lesson;
import project.ktc.springboot_app.lesson.repositories.InstructorLessonRepository;
import project.ktc.springboot_app.lesson.repositories.LessonCompletionRepository;
import project.ktc.springboot_app.quiz.repositories.QuizQuestionRepository;
import project.ktc.springboot_app.section.dto.*;
import project.ktc.springboot_app.section.entity.Section;
import project.ktc.springboot_app.section.repositories.InstructorSectionRepository;
import project.ktc.springboot_app.utils.SecurityUtil;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentCourseServiceImpl implements StudentCourseService {

    private final InstructorSectionRepository sectionRepository;
    private final InstructorLessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonCompletionRepository lessonCompletionRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ResponseEntity<ApiResponse<List<SectionWithLessonsDto>>> getCourseSections(String courseId) {
        log.info("Fetching sections for course: {} for student", courseId);

        try {
            String currentUserId = SecurityUtil.getCurrentUserId();

            // Verify user is enrolled in the course
            boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(currentUserId, courseId);
            if (!isEnrolled) {
                log.warn("User {} is not enrolled in course {}", currentUserId, courseId);
                return ApiResponseUtil.forbidden("You are not enrolled in this course");
            }

            // Fetch all sections for the course
            List<Section> sections = sectionRepository.findSectionsByCourseIdOrderByOrder(courseId);

            // Convert to DTOs with lessons
            List<SectionWithLessonsDto> sectionDtos = sections.stream()
                    .map(section -> mapToSectionWithLessonsDto(section, currentUserId))
                    .collect(Collectors.toList());

            log.info("Successfully retrieved {} sections for course {}", sectionDtos.size(), courseId);
            return ApiResponseUtil.success(sectionDtos, "Sections retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving sections for course {}: {}", courseId, e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve sections. Please try again later.");
        }
    }

    private SectionWithLessonsDto mapToSectionWithLessonsDto(Section section, String userId) {
        // Get lessons for this section
        List<Lesson> lessons = lessonRepository.findLessonsBySectionIdOrderByOrder(section.getId());

        List<LessonDto> lessonDtos = lessons.stream()
                .map(lesson -> mapToLessonDto(lesson, userId))
                .collect(Collectors.toList());

        return SectionWithLessonsDto.builder()
                .id(section.getId())
                .title(section.getTitle())
                .orderIndex(section.getOrderIndex())
                .lessonCount(lessons.size())
                .lessons(lessonDtos)
                .build();
    }

    private LessonDto mapToLessonDto(Lesson lesson, String userId) {
        // Check if lesson is completed by the user
        boolean isCompleted = lessonCompletionRepository.existsByUserIdAndLessonId(userId, lesson.getId());

        String lessonType = lesson.getLessonType() != null ? lesson.getLessonType().getName() : "UNKNOWN";

        LessonDto.LessonDtoBuilder lessonBuilder = LessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .type(lessonType)
                .order(lesson.getOrderIndex())
                .isCompleted(isCompleted);

        // Add content based on lesson type
        if ("VIDEO".equals(lessonType) && lesson.getContent() != null) {
            VideoDto videoDto = VideoDto.builder()
                    .id(lesson.getContent().getId())
                    .url(lesson.getContent().getUrl())
                    .duration(lesson.getContent().getDuration())
                    .build();
            lessonBuilder.video(videoDto);
        } else if ("QUIZ".equals(lessonType)) {
            List<QuizQuestion> questions = quizQuestionRepository.findQuestionsByLessonId(lesson.getId());
            List<QuizQuestionDto> questionDtos = questions.stream()
                    .map(this::mapToQuizQuestionDto)
                    .collect(Collectors.toList());

            QuizDto quizDto = QuizDto.builder()
                    .questions(questionDtos)
                    .build();
            lessonBuilder.quiz(quizDto);
        }

        return lessonBuilder.build();
    }

    private QuizQuestionDto mapToQuizQuestionDto(QuizQuestion question) {
        // Parse JSON options string to List<String>
        List<String> optionsList = Collections.emptyList();
        try {
            optionsList = objectMapper.readValue(question.getOptions(), new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("Failed to parse options JSON for question {}: {}", question.getId(), e.getMessage());
        }

        return QuizQuestionDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .options(optionsList)
                .correctAnswer(question.getCorrectAnswer())
                .explanation(question.getExplanation())
                .build();
    }
}
