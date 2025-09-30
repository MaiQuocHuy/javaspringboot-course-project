package project.ktc.springboot_app.quiz.interfaces;

import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.quiz.dto.CreateQuizDto;
import project.ktc.springboot_app.quiz.dto.QuizResponseDto;
import project.ktc.springboot_app.quiz.dto.UpdateQuizDto;
import project.ktc.springboot_app.quiz.dto.UpdateQuizResponseDto;

public interface QuizService {

	/**
	 * Creates a new quiz for a lesson
	 *
	 * @param createQuizDto
	 *            Quiz creation data
	 * @param instructorId
	 *            ID of the instructor creating the quiz
	 * @return Created quiz response
	 */
	QuizResponseDto createQuiz(CreateQuizDto createQuizDto, String instructorId);

	/**
	 * Updates (replaces) quiz questions for a lesson This is a full replacement -
	 * questions not
	 * included will be deleted
	 *
	 * @param sectionId
	 *            Section ID containing the lesson
	 * @param lessonId
	 *            Lesson ID to update quiz for
	 * @param updateQuizDto
	 *            Update quiz data
	 * @param instructorId
	 *            ID of the instructor updating the quiz
	 * @return Updated quiz response with statistics
	 */
	ResponseEntity<ApiResponse<UpdateQuizResponseDto>> updateQuiz(
			String sectionId, String lessonId, UpdateQuizDto updateQuizDto, String instructorId);

	/**
	 * Validates if instructor owns the lesson
	 *
	 * @param lessonId
	 *            Lesson ID
	 * @param instructorId
	 *            Instructor ID
	 * @return true if instructor owns the lesson
	 */
	boolean validateLessonOwnership(String lessonId, String instructorId);
}
