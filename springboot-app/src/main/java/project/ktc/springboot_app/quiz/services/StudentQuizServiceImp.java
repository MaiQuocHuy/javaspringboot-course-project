package project.ktc.springboot_app.quiz.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.utils.SecurityUtil;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.exception.ResourceNotFoundException;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.entity.QuizQuestion;
import project.ktc.springboot_app.entity.QuizResult;
import project.ktc.springboot_app.quiz.dto.QuizScoreResponseDto;
import project.ktc.springboot_app.quiz.dto.StudentQuizStatsDto;
import project.ktc.springboot_app.quiz.dto.QuizScoreDetailResponseDto;
import project.ktc.springboot_app.quiz.interfaces.StudentQuizService;
import project.ktc.springboot_app.quiz.repositories.QuizResultRepository;
import project.ktc.springboot_app.quiz.repositories.QuizQuestionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentQuizServiceImp implements StudentQuizService {

        private final QuizResultRepository quizResultRepository;
        private final QuizQuestionRepository quizQuestionRepository;
        private final UserRepository userRepository;
        private final ObjectMapper objectMapper;

        @Override
        public ResponseEntity<ApiResponse<PaginatedResponse<QuizScoreResponseDto>>> getQuizScores(Pageable pageable) {
                log.info("Getting quiz scores for current user with pagination: {}", pageable);

                // Get current user
                String currentUserEmail = getCurrentUserEmail();
                User currentUser = userRepository.findByEmail(currentUserEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                // Get quiz results for the user
                Page<QuizResult> quizResultsPage = quizResultRepository.findQuizScoresByUserId(currentUser.getId(),
                                pageable);

                // Map to DTOs
                List<QuizScoreResponseDto> quizScoreDtos = quizResultsPage.getContent().stream()
                                .map(this::mapToQuizScoreDto)
                                .collect(Collectors.toList());

                // Create paginated response
                PaginatedResponse<QuizScoreResponseDto> paginatedResponse = PaginatedResponse
                                .<QuizScoreResponseDto>builder()
                                .content(quizScoreDtos)
                                .page(PaginatedResponse.PageInfo.builder()
                                                .number(quizResultsPage.getNumber())
                                                .size(quizResultsPage.getSize())
                                                .totalPages(quizResultsPage.getTotalPages())
                                                .totalElements(quizResultsPage.getTotalElements())
                                                .first(quizResultsPage.isFirst())
                                                .last(quizResultsPage.isLast())
                                                .build())
                                .build();

                return ApiResponseUtil.success(paginatedResponse, "Quiz scores retrieved successfully");
        }

        @Override
        public ResponseEntity<ApiResponse<QuizScoreDetailResponseDto>> getQuizScoreDetail(String quizResultId) {
                log.info("Getting quiz score detail for quiz result ID: {}", quizResultId);

                // Get current user
                String currentUserEmail = getCurrentUserEmail();
                User currentUser = userRepository.findByEmail(currentUserEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                // Get quiz result with ownership validation
                QuizResult quizResult = quizResultRepository
                                .findQuizResultByIdAndUserId(quizResultId, currentUser.getId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Quiz result not found or access denied"));

                log.info("Quiz result found: {}", quizResult);
                // Get quiz questions for this lesson
                List<QuizQuestion> quizQuestions = quizQuestionRepository
                                .findByLessonId(quizResult.getLesson().getId());

                // Parse student answers
                Map<String, String> studentAnswers = parseStudentAnswers(quizResult.getAnswers());

                // Build response
                QuizScoreDetailResponseDto responseDto = mapToQuizScoreDetailDto(quizResult, quizQuestions,
                                studentAnswers);

                return ApiResponseUtil.success(responseDto, "Quiz score details retrieved successfully");
        }

        private QuizScoreDetailResponseDto mapToQuizScoreDetailDto(QuizResult quizResult,
                        List<QuizQuestion> quizQuestions,
                        Map<String, String> studentAnswers) {
                // Get total questions for this lesson
                Long totalQuestions = quizResultRepository.countQuestionsByLessonId(quizResult.getLesson().getId());

                // Calculate correct answers based on score percentage
                Integer correctAnswers = calculateCorrectAnswers(quizResult.getScore(), totalQuestions.intValue());

                // Check if user can review this quiz (for now, always true)
                Boolean canReview = true;

                // Map questions with student answers
                List<QuizScoreDetailResponseDto.QuestionDetail> questionDetails = quizQuestions.stream()
                                .map(question -> mapToQuestionDetail(question, studentAnswers))
                                .collect(Collectors.toList());

                return QuizScoreDetailResponseDto.builder()
                                .id(quizResult.getId())
                                .lesson(QuizScoreDetailResponseDto.LessonSummary.builder()
                                                .id(quizResult.getLesson().getId())
                                                .title(quizResult.getLesson().getTitle())
                                                .build())
                                .section(QuizScoreDetailResponseDto.SectionSummary.builder()
                                                .id(quizResult.getLesson().getSection().getId())
                                                .title(quizResult.getLesson().getSection().getTitle())
                                                .build())
                                .course(QuizScoreDetailResponseDto.CourseSummary.builder()
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

        private QuizScoreDetailResponseDto.QuestionDetail mapToQuestionDetail(QuizQuestion question,
                        Map<String, String> studentAnswers) {
                // Parse options from JSON
                Map<String, String> options = parseQuestionOptionsAsMap(question.getOptions());

                // Get student answer for this question
                String studentAnswer = studentAnswers.get(question.getId());

                // Debug logging if student answer is null
                if (studentAnswer == null) {
                        log.debug("No student answer found for question ID: {}. Available answer keys: {}",
                                        question.getId(), studentAnswers.keySet());
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
                        return objectMapper.readValue(optionsJson, new TypeReference<Map<String, String>>() {
                        });
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
                        Map<String, String> answers = objectMapper.readValue(answersJson,
                                        new TypeReference<Map<String, String>>() {
                                        });
                        log.debug("Parsed student answers: {}", answers);
                        return answers;
                } catch (JsonProcessingException e) {
                        log.warn("Failed to parse student answers: {}", answersJson, e);
                        return Map.of();
                }
        }

        private QuizScoreResponseDto mapToQuizScoreDto(QuizResult quizResult) {
                // Get total questions for this lesson
                Long totalQuestions = quizResultRepository.countQuestionsByLessonId(quizResult.getLesson().getId());

                // Calculate correct answers based on score percentage
                Integer correctAnswers = calculateCorrectAnswers(quizResult.getScore(), totalQuestions.intValue());

                // Check if user can review this quiz (for now, always true)
                Boolean canReview = true;

                return QuizScoreResponseDto.builder()
                                .id(quizResult.getId())
                                .lesson(QuizScoreResponseDto.LessonSummary.builder()
                                                .id(quizResult.getLesson().getId())
                                                .title(quizResult.getLesson().getTitle())
                                                .build())
                                .section(QuizScoreResponseDto.SectionSummary.builder()
                                                .id(quizResult.getLesson().getSection().getId())
                                                .title(quizResult.getLesson().getSection().getTitle())
                                                .build())
                                .course(QuizScoreResponseDto.CourseSummary.builder()
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

        private String getCurrentUserEmail() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || authentication.getName() == null) {
                        throw new ResourceNotFoundException("No authenticated user found");
                }
                return authentication.getName();
        }

        @Override
        public ResponseEntity<ApiResponse<StudentQuizStatsDto>> getQuizStats() {
                try {
                        String currentUserId = SecurityUtil.getCurrentUserId();
                        log.info("Fetching quiz statistics for user: {}", currentUserId);

                        // Get quiz statistics using repository methods
                        Long totalQuizzes = quizResultRepository.countTotalQuizzesByUserId(currentUserId);
                        Long passedQuizzes = quizResultRepository.countPassedQuizzesByUserId(currentUserId);
                        Long failedQuizzes = quizResultRepository.countFailedQuizzesByUserId(currentUserId);
                        Double averageScore = quizResultRepository.calculateAverageScoreByUserId(currentUserId);

                        // Handle null values
                        long totalQuizzesValue = totalQuizzes != null ? totalQuizzes : 0L;
                        long passedQuizzesValue = passedQuizzes != null ? passedQuizzes : 0L;
                        long failedQuizzesValue = failedQuizzes != null ? failedQuizzes : 0L;
                        double averageScoreValue = averageScore != null ? averageScore : 0.0;

                        // Round average score to 2 decimal places
                        BigDecimal roundedAverage = BigDecimal.valueOf(averageScoreValue)
                                        .setScale(2, RoundingMode.HALF_UP);

                        StudentQuizStatsDto stats = StudentQuizStatsDto.builder()
                                        .totalQuizzes(totalQuizzesValue)
                                        .passedQuizzes(passedQuizzesValue)
                                        .failedQuizzes(failedQuizzesValue)
                                        .averageScore(roundedAverage.doubleValue())
                                        .build();

                        log.info("Quiz statistics retrieved successfully for user {}: Total={}, Passed={}, Failed={}, Average={}",
                                        currentUserId, totalQuizzesValue, passedQuizzesValue, failedQuizzesValue,
                                        roundedAverage);

                        return ApiResponseUtil.success(stats, "Quiz statistics retrieved successfully");

                } catch (Exception e) {
                        log.error("Error retrieving quiz statistics: {}", e.getMessage(), e);
                        return ApiResponseUtil.internalServerError("Failed to retrieve quiz statistics");
                }
        }
}
