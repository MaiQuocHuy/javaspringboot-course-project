package project.ktc.springboot_app.quiz.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.quiz.dto.QuizScoreDetailResponseDto;
import project.ktc.springboot_app.quiz.dto.QuizScoreResponseDto;

public interface InstructorQuizService {

  ResponseEntity<ApiResponse<PaginatedResponse<QuizScoreResponseDto>>> getStudentQuizScores(
      String studentId, Pageable pageable);

  ResponseEntity<ApiResponse<QuizScoreDetailResponseDto>> getStudentQuizScoreDetail(
      String studentId, String quizResultId);
}
