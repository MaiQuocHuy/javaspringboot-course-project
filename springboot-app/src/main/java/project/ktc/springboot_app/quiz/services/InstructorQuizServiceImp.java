package project.ktc.springboot_app.quiz.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.exception.ResourceNotFoundException;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.entity.QuizQuestion;
import project.ktc.springboot_app.entity.QuizResult;
import project.ktc.springboot_app.quiz.dto.QuizScoreDetailResponseDto;
import project.ktc.springboot_app.quiz.dto.QuizScoreResponseDto;
import project.ktc.springboot_app.quiz.interfaces.InstructorQuizService;
import project.ktc.springboot_app.quiz.repositories.QuizQuestionRepository;
import project.ktc.springboot_app.quiz.repositories.QuizResultRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorQuizServiceImp implements InstructorQuizService {

  private final QuizResultRepository quizResultRepository;
  private final QuizQuestionRepository quizQuestionRepository;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  @Override
  public ResponseEntity<ApiResponse<PaginatedResponse<QuizScoreResponseDto>>> getStudentQuizScores(
      String studentId, Pageable pageable) {
    try {
      // Get current user
      Optional<User> currentUser = userRepository.findById(studentId);
      if (currentUser.isEmpty()) {
        return ApiResponseUtil.notFound("Student not found");
      }

      // Get quiz results for the user
      Page<QuizResult> quizResultsPage =
          quizResultRepository.findQuizScoresByUserId(studentId, pageable);

      // Map to DTOs
      List<QuizScoreResponseDto> quizScoreDtos =
          quizResultsPage.getContent().stream()
              .map(this::mapToQuizScoreDto)
              .collect(Collectors.toList());

      // Create paginated response
      PaginatedResponse<QuizScoreResponseDto> paginatedResponse =
          PaginatedResponse.<QuizScoreResponseDto>builder()
              .content(quizScoreDtos)
              .page(
                  PaginatedResponse.PageInfo.builder()
                      .number(quizResultsPage.getNumber())
                      .size(quizResultsPage.getSize())
                      .totalPages(quizResultsPage.getTotalPages())
                      .totalElements(quizResultsPage.getTotalElements())
                      .first(quizResultsPage.isFirst())
                      .last(quizResultsPage.isLast())
                      .build())
              .build();

      return ApiResponseUtil.success(paginatedResponse, "Quiz scores retrieved successfully");
    } catch (Exception e) {
      return ApiResponseUtil.internalServerError("An error occurred while retrieving quiz scores");
    }
  }

  @Override
  public ResponseEntity<ApiResponse<QuizScoreDetailResponseDto>> getStudentQuizScoreDetail(
      String studentId, String quizResultId) {
    try {
      // Check if student exists
      Optional<User> enrolledStudent = userRepository.findById(studentId);
      if (enrolledStudent.isEmpty()) {
        return ApiResponseUtil.notFound("Student not found");
      }
      // Get quiz result with ownership validation
      QuizResult quizResult =
          quizResultRepository
              .findQuizResultByIdAndUserId(quizResultId, studentId)
              .orElseThrow(
                  () -> new ResourceNotFoundException("Quiz result not found or access denied"));

      log.info("Quiz result found: {}", quizResult);
      // Get quiz questions for this lesson
      List<QuizQuestion> quizQuestions =
          quizQuestionRepository.findByLessonId(quizResult.getLesson().getId());

      // Parse student answers
      Map<String, String> studentAnswers = parseStudentAnswers(quizResult.getAnswers());

      // Build response
      QuizScoreDetailResponseDto responseDto =
          mapToQuizScoreDetailDto(quizResult, quizQuestions, studentAnswers);

      return ApiResponseUtil.success(responseDto, "Quiz score details retrieved successfully");
    } catch (Exception e) {
      return ApiResponseUtil.internalServerError(
          "An error occurred while retrieving quiz score details");
    }
  }

  private QuizScoreDetailResponseDto mapToQuizScoreDetailDto(
      QuizResult quizResult, List<QuizQuestion> quizQuestions, Map<String, String> studentAnswers) {
    // Get total questions for this lesson
    Long totalQuestions =
        quizResultRepository.countQuestionsByLessonId(quizResult.getLesson().getId());

    // Calculate correct answers based on score percentage
    Integer correctAnswers =
        calculateCorrectAnswers(quizResult.getScore(), totalQuestions.intValue());

    // Check if user can review this quiz (for now, always true)
    Boolean canReview = true;

    // Map questions with student answers
    List<QuizScoreDetailResponseDto.QuestionDetail> questionDetails =
        quizQuestions.stream()
            .map(question -> mapToQuestionDetail(question, studentAnswers))
            .collect(Collectors.toList());

    return QuizScoreDetailResponseDto.builder()
        .id(quizResult.getId())
        .lesson(
            QuizScoreDetailResponseDto.LessonSummary.builder()
                .id(quizResult.getLesson().getId())
                .title(quizResult.getLesson().getTitle())
                .build())
        .section(
            QuizScoreDetailResponseDto.SectionSummary.builder()
                .id(quizResult.getLesson().getSection().getId())
                .title(quizResult.getLesson().getSection().getTitle())
                .build())
        .course(
            QuizScoreDetailResponseDto.CourseSummary.builder()
                .id(quizResult.getLesson().getSection().getCourse().getId())
                .title(quizResult.getLesson().getSection().getCourse().getTitle())
                .build())
        .score(quizResult.getScore())
        .totalQuestions(totalQuestions.intValue())
        .correctAnswers(correctAnswers)
        .completedAt(quizResult.getCompletedAt())
        .canReview(canReview)
        .questions(questionDetails)
        .build();
  }

  private QuizScoreDetailResponseDto.QuestionDetail mapToQuestionDetail(
      QuizQuestion question, Map<String, String> studentAnswers) {
    // Parse options from JSON
    Map<String, String> options = parseQuestionOptionsAsMap(question.getOptions());

    // Get student answer for this question
    String studentAnswer = studentAnswers.get(question.getId());

    // Debug logging if student answer is null
    if (studentAnswer == null) {
      log.debug(
          "No student answer found for question ID: {}. Available answer keys: {}",
          question.getId(),
          studentAnswers.keySet());
    }

    // Check if answer is correct
    Boolean isCorrect = question.getCorrectAnswer().equals(studentAnswer);

    return QuizScoreDetailResponseDto.QuestionDetail.builder()
        .id(question.getId())
        .questionText(question.getQuestionText())
        .options(options)
        .studentAnswer(studentAnswer)
        .correctAnswer(question.getCorrectAnswer())
        .isCorrect(isCorrect)
        .explanation(question.getExplanation())
        .build();
  }

  private Map<String, String> parseQuestionOptionsAsMap(String optionsJson) {
    try {
      // Try to parse as Map<String, String> (current format)
      return objectMapper.readValue(optionsJson, new TypeReference<Map<String, String>>() {});
    } catch (JsonProcessingException e) {
      log.warn("Failed to parse question options as Map: {}", optionsJson, e);
      return new HashMap<>();
    }
  }

  private Map<String, String> parseStudentAnswers(String answersJson) {
    try {
      if (answersJson == null || answersJson.trim().isEmpty()) {
        return Map.of();
      }
      Map<String, String> answers =
          objectMapper.readValue(answersJson, new TypeReference<Map<String, String>>() {});
      log.debug("Parsed student answers: {}", answers);
      return answers;
    } catch (JsonProcessingException e) {
      log.warn("Failed to parse student answers: {}", answersJson, e);
      return Map.of();
    }
  }

  private QuizScoreResponseDto mapToQuizScoreDto(QuizResult quizResult) {
    // Get total questions for this lesson
    Long totalQuestions =
        quizResultRepository.countQuestionsByLessonId(quizResult.getLesson().getId());

    // Calculate correct answers based on score percentage
    Integer correctAnswers =
        calculateCorrectAnswers(quizResult.getScore(), totalQuestions.intValue());

    // Check if user can review this quiz (for now, always true)
    Boolean canReview = true;

    return QuizScoreResponseDto.builder()
        .id(quizResult.getId())
        .lesson(
            QuizScoreResponseDto.LessonSummary.builder()
                .id(quizResult.getLesson().getId())
                .title(quizResult.getLesson().getTitle())
                .build())
        .section(
            QuizScoreResponseDto.SectionSummary.builder()
                .id(quizResult.getLesson().getSection().getId())
                .title(quizResult.getLesson().getSection().getTitle())
                .build())
        .course(
            QuizScoreResponseDto.CourseSummary.builder()
                .id(quizResult.getLesson().getSection().getCourse().getId())
                .title(quizResult.getLesson().getSection().getCourse().getTitle())
                .build())
        .score(quizResult.getScore())
        .totalQuestions(totalQuestions.intValue())
        .correctAnswers(correctAnswers)
        .completedAt(quizResult.getCompletedAt())
        .canReview(canReview)
        .build();
  }

  private Integer calculateCorrectAnswers(BigDecimal score, Integer totalQuestions) {
    if (score == null || totalQuestions == null || totalQuestions == 0) {
      return 0;
    }

    // Assuming score is a percentage (0-100)
    BigDecimal percentage = score.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    BigDecimal correctAnswersBd = percentage.multiply(BigDecimal.valueOf(totalQuestions));

    return correctAnswersBd.setScale(0, RoundingMode.HALF_UP).intValue();
  }
}
