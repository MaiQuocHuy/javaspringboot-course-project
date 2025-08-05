package project.ktc.springboot_app.quiz.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.exception.ResourceNotFoundException;
import project.ktc.springboot_app.common.exception.ValidationException;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.entity.QuizQuestion;
import project.ktc.springboot_app.lesson.entity.Lesson;
import project.ktc.springboot_app.lesson.repositories.InstructorLessonRepository;
import project.ktc.springboot_app.quiz.dto.CreateQuizDto;
import project.ktc.springboot_app.quiz.dto.CreateQuizQuestionDto;
import project.ktc.springboot_app.quiz.dto.QuizQuestionResponseDto;
import project.ktc.springboot_app.quiz.dto.QuizResponseDto;
import project.ktc.springboot_app.quiz.dto.UpdateQuizDto;
import project.ktc.springboot_app.quiz.dto.UpdateQuizQuestionDto;
import project.ktc.springboot_app.quiz.dto.UpdateQuizResponseDto;
import project.ktc.springboot_app.quiz.interfaces.QuizService;
import project.ktc.springboot_app.quiz.repositories.QuizQuestionRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuizServiceImp implements QuizService {

    private final QuizQuestionRepository quizQuestionRepository;
    private final InstructorLessonRepository lessonRepository;
    private final ObjectMapper objectMapper;

    @Override
    public QuizResponseDto createQuiz(CreateQuizDto createQuizDto, String instructorId) {
        log.info("Creating quiz for lesson: {} by instructor: {}", createQuizDto.getLessonId(), instructorId);

        // 1. Validate lesson exists and instructor owns it
        Lesson lesson = validateAndGetLesson(createQuizDto.getLessonId(), instructorId);

        // 2. Validate that lesson is of type QUIZ
        if (!"type-002".equals(lesson.getLessonType().getId())) { // Assuming type-002 is QUIZ type
            throw new ValidationException("Can only create quiz for lessons of type QUIZ");
        }

        // 3. Check if lesson already has quiz questions
        if (quizQuestionRepository.existsByLessonId(createQuizDto.getLessonId())) {
            throw new ValidationException(
                    "Lesson already has quiz questions. Use update endpoint to modify existing quiz.");
        }

        // 4. Validate and create quiz questions
        List<QuizQuestion> quizQuestions = new ArrayList<>();
        List<QuizQuestionResponseDto> questionResponses = new ArrayList<>();

        for (CreateQuizQuestionDto questionDto : createQuizDto.getQuestions()) {
            // Validate correct answer exists in options
            if (!questionDto.getOptions().containsKey(questionDto.getCorrectAnswer())) {
                throw new ValidationException("Correct answer '" + questionDto.getCorrectAnswer() +
                        "' must be one of the provided options");
            }

            // Create QuizQuestion entity
            QuizQuestion quizQuestion = new QuizQuestion();
            quizQuestion.setLesson(lesson);
            quizQuestion.setQuestionText(questionDto.getQuestionText());
            quizQuestion.setOptions(convertOptionsToJson(questionDto.getOptions()));
            quizQuestion.setCorrectAnswer(questionDto.getCorrectAnswer());
            quizQuestion.setExplanation(questionDto.getExplanation());

            quizQuestions.add(quizQuestion);
        }

        // 5. Save all quiz questions
        List<QuizQuestion> savedQuestions = quizQuestionRepository.saveAll(quizQuestions);

        // 6. Convert to response DTOs
        for (QuizQuestion savedQuestion : savedQuestions) {
            QuizQuestionResponseDto responseDto = QuizQuestionResponseDto.builder()
                    .id(savedQuestion.getId())
                    .questionText(savedQuestion.getQuestionText())
                    .options(convertJsonToOptions(savedQuestion.getOptions()))
                    .correctAnswer(savedQuestion.getCorrectAnswer())
                    .explanation(savedQuestion.getExplanation())
                    .build();
            questionResponses.add(responseDto);
        }

        // 7. Build and return response
        return QuizResponseDto.builder()
                .id(lesson.getId()) // Using lesson ID as quiz ID
                .title(createQuizDto.getTitle())
                .description(createQuizDto.getDescription())
                .courseId(lesson.getSection().getCourse().getId())
                .lessonId(lesson.getId())
                .questions(questionResponses)
                .createdAt(savedQuestions.get(0).getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<UpdateQuizResponseDto>> updateQuiz(String sectionId, String lessonId,
            UpdateQuizDto updateQuizDto,
            String instructorId) {
        log.info("Updating quiz for lesson: {} in section: {} by instructor: {}", lessonId, sectionId, instructorId);

        // 1. Validate lesson exists and instructor owns it
        Lesson lesson = validateAndGetLesson(lessonId, instructorId);

        // 2. Validate that lesson belongs to the specified section
        if (!sectionId.equals(lesson.getSection().getId())) {
            throw new ValidationException("Lesson does not belong to the specified section");
        }

        log.info("Lesson validated: {}", lesson.getLessonType().getId());

        // 3. Validate that lesson is of type QUIZ
        if (!"type-002".equals(lesson.getLessonType().getId())) { // Assuming type-002 is QUIZ type
            throw new ValidationException("Can only update quiz for lessons of type QUIZ");
        }

        // 4. Get existing quiz questions
        List<QuizQuestion> existingQuestions = quizQuestionRepository.findByLessonId(lessonId);

        // 5. Statistics tracking
        int questionsUpdated = 0;
        int questionsAdded = 0;
        int questionsRemoved = existingQuestions.size();

        // 6. Delete all existing questions (full replacement)
        if (!existingQuestions.isEmpty()) {
            quizQuestionRepository.deleteAll(existingQuestions);
            log.info("Deleted {} existing questions for lesson: {}", existingQuestions.size(), lessonId);
        }

        // 7. Create new questions from DTO
        List<QuizQuestion> newQuestions = new ArrayList<>();
        List<QuizQuestionResponseDto> questionResponses = new ArrayList<>();

        for (UpdateQuizQuestionDto questionDto : updateQuizDto.getQuestions()) {
            // Validate correct answer exists in options
            if (!questionDto.getOptions().containsKey(questionDto.getCorrectAnswer())) {
                throw new ValidationException("Correct answer '" + questionDto.getCorrectAnswer() +
                        "' must be one of the provided options: " + questionDto.getOptions().keySet());
            }

            // Create new QuizQuestion entity
            QuizQuestion quizQuestion = new QuizQuestion();
            quizQuestion.setLesson(lesson);
            quizQuestion.setQuestionText(questionDto.getQuestionText());
            quizQuestion.setOptions(convertOptionsToJson(questionDto.getOptions()));
            quizQuestion.setCorrectAnswer(questionDto.getCorrectAnswer());
            quizQuestion.setExplanation(questionDto.getExplanation());

            newQuestions.add(quizQuestion);
        }

        // 8. Save all new quiz questions
        List<QuizQuestion> savedQuestions = quizQuestionRepository.saveAll(newQuestions);
        questionsAdded = savedQuestions.size();

        log.info("Quiz update completed for lesson: {}. Added: {}, Removed: {}",
                lessonId, questionsAdded, questionsRemoved);

        // 9. Convert to response DTOs
        for (QuizQuestion savedQuestion : savedQuestions) {
            QuizQuestionResponseDto responseDto = QuizQuestionResponseDto.builder()
                    .id(savedQuestion.getId())
                    .questionText(savedQuestion.getQuestionText())
                    .options(convertJsonToOptions(savedQuestion.getOptions()))
                    .correctAnswer(savedQuestion.getCorrectAnswer())
                    .explanation(savedQuestion.getExplanation())
                    .build();
            questionResponses.add(responseDto);
        }

        UpdateQuizResponseDto responseDto = UpdateQuizResponseDto.builder()
                .questions(questionResponses)
                .questionsUpdated(questionsUpdated) // Always 0 in full replacement
                .questionsAdded(questionsAdded)
                .questionsRemoved(questionsRemoved)
                .updatedAt(
                        savedQuestions.isEmpty() ? java.time.LocalDateTime.now() : savedQuestions.get(0).getCreatedAt())
                .build();

        // 10. Build and return response with statistics
        return ApiResponseUtil.success(responseDto, "Quiz updated successfully");
    }

    @Override
    public boolean validateLessonOwnership(String lessonId, String instructorId) {
        return lessonRepository.existsByIdAndSectionCourseInstructorId(lessonId, instructorId);
    }

    private Lesson validateAndGetLesson(String lessonId, String instructorId) {
        // Check if lesson exists
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + lessonId));

        // Check if instructor owns the lesson's course
        if (!validateLessonOwnership(lessonId, instructorId)) {
            throw new ValidationException("You are not authorized to create quiz for this lesson");
        }

        return lesson;
    }

    private String convertOptionsToJson(Map<String, String> options) {
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            log.error("Error converting options to JSON", e);
            throw new ValidationException("Invalid options format");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> convertJsonToOptions(String optionsJson) {
        try {
            return objectMapper.readValue(optionsJson, HashMap.class);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to options", e);
            return new HashMap<>();
        }
    }
}
