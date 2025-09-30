package project.ktc.springboot_app.lesson.interfaces;

import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.quiz.dto.QuizSubmissionResponseDto;
import project.ktc.springboot_app.quiz.dto.SubmitQuizDto;

public interface StudentService {

  /**
   * Marks a lesson as completed by the instructor Allows instructors to track which lessons have
   * been finalized or verified during course creation or update
   *
   * @param sectionId The ID of the section
   * @param lessonId The ID of the lesson to mark as completed
   * @return ApiResponse with success message
   */
  ResponseEntity<ApiResponse<String>> completeLesson(String sectionId, String lessonId);

  /**
   * Submits quiz answers for a specific lesson If a result already exists for the current user and
   * lesson, it will be overwritten
   *
   * @param sectionId The ID of the section containing the lesson
   * @param lessonId The ID of the lesson being submitted
   * @param submitQuizDto The quiz answers
   * @return ApiResponse with quiz submission results
   */
  ResponseEntity<ApiResponse<QuizSubmissionResponseDto>> submitQuiz(
      String sectionId, String lessonId, SubmitQuizDto submitQuizDto);
}
