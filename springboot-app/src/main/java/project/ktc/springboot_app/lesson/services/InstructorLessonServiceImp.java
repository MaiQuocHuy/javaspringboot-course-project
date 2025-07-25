package project.ktc.springboot_app.lesson.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.exception.ResourceNotFoundException;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.lesson.entity.Lesson;
import project.ktc.springboot_app.lesson.interfaces.LessonService;
import project.ktc.springboot_app.lesson.repositories.InstructorLessonRepository;
import project.ktc.springboot_app.entity.QuizQuestion;
import project.ktc.springboot_app.quiz.repositories.QuizQuestionRepository;
import project.ktc.springboot_app.section.dto.LessonDto;
import project.ktc.springboot_app.section.dto.QuizDto;
import project.ktc.springboot_app.section.dto.QuizQuestionDto;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;
import project.ktc.springboot_app.section.dto.VideoDto;
import project.ktc.springboot_app.section.entity.Section;
import project.ktc.springboot_app.section.repositories.SectionRepository;
import project.ktc.springboot_app.utils.SecurityUtil;
import project.ktc.springboot_app.entity.VideoContent;
import project.ktc.springboot_app.video.repositories.VideoContentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstructorLessonServiceImp implements LessonService {

    private final InstructorLessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final VideoContentRepository videoContentRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ResponseEntity<ApiResponse<SectionWithLessonsDto>> getSectionWithLessons(String sectionId) {
        // Verify section exists and belongs to instructor

        String currentUserId = SecurityUtil.getCurrentUserId();

        Optional<Section> sectionOtp = sectionRepository.findById(sectionId);
        if (sectionOtp.isEmpty()) {
            return ApiResponseUtil.notFound("Section not found with id: " + sectionId);
        }

        // Check if the section belongs to the instructor
        if (!sectionOtp.get().getCourse().getInstructor().getId().equals(currentUserId)) {
            return ApiResponseUtil.forbidden("You do not have permission to access this section");
        }
        Section section = sectionOtp.get();

        // Get lessons for the section
        List<Lesson> lessons = lessonRepository.findLessonsBySectionIdOrderByOrder(sectionId);

        // Convert lessons to DTOs
        List<LessonDto> lessonDtos = lessons.stream()
                .map(this::convertToLessonDto)
                .collect(Collectors.toList());

        SectionWithLessonsDto sectionWithLessonsDto = createSectionWithLessonsDto(section, lessonDtos);

        // Create and return SectionWithLessonsDto
        return ApiResponseUtil.success(sectionWithLessonsDto, "Section with lessons retrieved successfully");
    }

    private SectionWithLessonsDto createSectionWithLessonsDto(Section section, List<LessonDto> lessonDtos) {
        return SectionWithLessonsDto.builder()
                .id(section.getId())
                .title(section.getTitle())
                .order(section.getOrderIndex())
                .lessonCount(lessonDtos.size())
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
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                options = objectMapper.readValue(question.getOptions(), new TypeReference<List<String>>() {
                });
            }
        } catch (Exception e) {
            log.warn("Could not parse options for question {}: {}", question.getId(), question.getOptions(), e);
        }

        return QuizQuestionDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .options(options)
                .correctAnswer(question.getCorrectAnswer())
                .build();
    }
}
