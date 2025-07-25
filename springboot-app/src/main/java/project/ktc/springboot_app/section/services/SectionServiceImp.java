package project.ktc.springboot_app.section.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.entity.Lesson;
import project.ktc.springboot_app.entity.QuizQuestion;
import project.ktc.springboot_app.entity.VideoContent;
import project.ktc.springboot_app.lesson.repositories.LessonRepository;
import project.ktc.springboot_app.quiz.repositories.QuizQuestionRepository;
import project.ktc.springboot_app.section.dto.LessonDto;
import project.ktc.springboot_app.section.dto.QuizDto;
import project.ktc.springboot_app.section.dto.QuizQuestionDto;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;
import project.ktc.springboot_app.section.dto.VideoDto;
import project.ktc.springboot_app.section.entity.Section;
import project.ktc.springboot_app.section.interfaces.SectionService;
import project.ktc.springboot_app.section.repositories.SectionRepository;
import project.ktc.springboot_app.video.repositories.VideoContentRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class SectionServiceImp implements SectionService {

    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final VideoContentRepository videoContentRepository;
    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ResponseEntity<ApiResponse<List<SectionWithLessonsDto>>> getCourseSections(
            String courseId,
            String instructorId) {

        log.info("Getting course sections for courseId: {}, instructorId: {}", courseId, instructorId);

        try {
            // Verify course exists and instructor owns it
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                log.warn("Course not found with ID: {}", courseId);
                return ApiResponseUtil.notFound("Course not found");
            }

            // Check ownership
            if (!course.getInstructor().getId().equals(instructorId)) {
                log.warn("Instructor {} does not own course {}", instructorId, courseId);
                return ApiResponseUtil.forbidden("You are not allowed to access this course's sections");
            }

            // Get all sections for the course
            List<Section> sections = sectionRepository.findSectionsByCourseIdOrderByOrder(courseId);

            // Convert to DTOs with lessons
            List<SectionWithLessonsDto> sectionDtos = sections.stream()
                    .map(this::convertToSectionWithLessonsDto)
                    .collect(Collectors.toList());

            log.info("Retrieved {} sections for course {}", sectionDtos.size(), courseId);
            return ApiResponseUtil.success(sectionDtos, "Sections retrieved successfully");

        } catch (Exception e) {
            log.error("Error getting course sections: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve course sections. Please try again later.");
        }
    }

    private SectionWithLessonsDto convertToSectionWithLessonsDto(Section section) {
        // Get lessons for this section
        List<Lesson> lessons = lessonRepository.findLessonsBySectionIdOrderByOrder(section.getId());

        // Convert lessons to DTOs
        List<LessonDto> lessonDtos = lessons.stream()
                .map(this::convertToLessonDto)
                .collect(Collectors.toList());

        return SectionWithLessonsDto.builder()
                .id(section.getId())
                .title(section.getTitle())
                .order(section.getOrderIndex())
                .lessonCount(lessons.size())
                .lessons(lessonDtos)
                .build();
    }

    private LessonDto convertToLessonDto(Lesson lesson) {
        LessonDto.LessonDtoBuilder builder = LessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .type(lesson.getType())
                .order(lesson.getOrderIndex());

        // Add type-specific data based on content_id
        if (lesson.getContentId() != null) {
            // If content_id is not null, this is a VIDEO lesson
            VideoDto videoDto = getVideoContent(lesson.getContentId());
            builder.video(videoDto);
        } else {
            // If content_id is null, this is a QUIZ lesson
            QuizDto quizDto = getQuizContent(lesson.getId());
            builder.quiz(quizDto);
        }

        return builder.build();
    }

    private VideoDto getVideoContent(String contentId) {
        try {
            VideoContent videoContent = videoContentRepository.findById(contentId).orElse(null);
            if (videoContent != null) {
                return VideoDto.builder()
                        .id(videoContent.getId())
                        .url(videoContent.getUrl())
                        .duration(videoContent.getDuration())
                        .build();
            }
        } catch (Exception e) {
            log.warn("Could not fetch video content for ID: {}", contentId, e);
        }
        return null;
    }

    private QuizDto getQuizContent(String lessonId) {
        try {
            List<QuizQuestion> questions = quizQuestionRepository.findQuestionsByLessonId(lessonId);
            log.info("Retrieved {} questions for lesson {}", questions.size(), lessonId);
            List<QuizQuestionDto> questionDtos = questions.stream()
                    .map(this::convertToQuizQuestionDto)
                    .collect(Collectors.toList());

            return QuizDto.builder()
                    .questions(questionDtos)
                    .build();

        } catch (Exception e) {
            log.warn("Could not fetch quiz content for lesson ID: {}", lessonId, e);
            return QuizDto.builder()
                    .questions(new ArrayList<>())
                    .build();
        }
    }

    private QuizQuestionDto convertToQuizQuestionDto(QuizQuestion question) {
        // Parse options from JSON string
        List<String> options = new ArrayList<>();
        try {
            if (question.getOptions() != null) {
                options = objectMapper.readValue(question.getOptions(), new TypeReference<List<String>>() {
                });
            }
        } catch (Exception e) {
            log.warn("Could not parse options for question {}: {}", question.getId(), e.getMessage());
        }

        return QuizQuestionDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .options(options)
                .correctAnswer(question.getCorrectAnswer())
                .explanation(question.getExplanation())
                .build();
    }
}
