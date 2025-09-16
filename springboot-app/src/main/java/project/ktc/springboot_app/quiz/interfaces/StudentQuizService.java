package project.ktc.springboot_app.quiz.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.quiz.dto.QuizScoreResponseDto;
import project.ktc.springboot_app.quiz.dto.StudentQuizStatsDto;
import project.ktc.springboot_app.quiz.dto.QuizScoreDetailResponseDto;

public interface StudentQuizService {

    /**
     * Get quiz scores for the current authenticated student
     * 
     * @param pageable Pagination and sorting parameters
     * @return Paginated response with quiz scores
     */
    ResponseEntity<ApiResponse<PaginatedResponse<QuizScoreResponseDto>>> getQuizScores(Pageable pageable);

    /**
     * Get detailed information about a specific quiz score for the current
     * authenticated student
     * 
     * @param quizResultId ID of the quiz result to retrieve
     * @return Detailed quiz score with questions and answers
     */
    ResponseEntity<ApiResponse<QuizScoreDetailResponseDto>> getQuizScoreDetail(String quizResultId);

    ResponseEntity<ApiResponse<StudentQuizStatsDto>> getQuizStats();
}
