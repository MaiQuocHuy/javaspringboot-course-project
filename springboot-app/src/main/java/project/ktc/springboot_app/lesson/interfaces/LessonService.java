package project.ktc.springboot_app.lesson.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.lesson.dto.CreateLessonDto;
import project.ktc.springboot_app.lesson.dto.CreateLessonResponseDto;
import project.ktc.springboot_app.lesson.dto.CreateLessonWithQuizDto;
import project.ktc.springboot_app.lesson.dto.LessonSubmissionsResponseDto;
import project.ktc.springboot_app.lesson.dto.LessonWithQuizResponseDto;
import project.ktc.springboot_app.lesson.dto.ReorderLessonsDto;
import project.ktc.springboot_app.lesson.dto.SubmissionDetailResponseDto;
import project.ktc.springboot_app.lesson.dto.UpdateLessonDto;
import project.ktc.springboot_app.lesson.dto.UpdateLessonResponseDto;
import project.ktc.springboot_app.quiz.dto.QuizQuestionResponseDto;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;

public interface LessonService {

	/**
	 * Retrieves a section with all its lessons, including completion status for the
	 * instructor
	 *
	 * @param sectionId
	 *            The ID of the section
	 * @return SectionWithLessonsDto containing section details and lessons with
	 *         completion status
	 */
	ResponseEntity<ApiResponse<SectionWithLessonsDto>> getSectionWithLessons(String sectionId);

	/**
	 * Creates a new lesson in a section owned by the instructor
	 *
	 * @param sectionId
	 *            The ID of the section
	 * @param lessonDto
	 *            The lesson creation data
	 * @param videoFile
	 *            The video file for video lessons (required for VIDEO type)
	 * @return LessonCreateResponseDto containing the created lesson details
	 */
	ResponseEntity<ApiResponse<CreateLessonResponseDto>> createLesson(
			String sectionId, CreateLessonDto lessonDto, MultipartFile videoFile);

	/**
	 * Updates an existing lesson in a section owned by the instructor
	 *
	 * @param sectionId
	 *            The ID of the section
	 * @param lessonId
	 *            The ID of the lesson to update
	 * @param lessonDto
	 *            The lesson update data
	 * @param videoFile
	 *            The video file for video lessons (optional)
	 * @return UpdateLessonResponseDto containing the updated lesson details
	 */
	ResponseEntity<ApiResponse<UpdateLessonResponseDto>> updateLesson(
			String sectionId, String lessonId, UpdateLessonDto lessonDto, MultipartFile videoFile);

	/**
	 * Deletes a lesson from a section owned by the instructor If the lesson is of
	 * type VIDEO, the
	 * associated video file will also be deleted from cloud storage
	 *
	 * @param sectionId
	 *            The ID of the section
	 * @param lessonId
	 *            The ID of the lesson to delete
	 * @return ApiResponse with success message
	 */
	ResponseEntity<ApiResponse<String>> deleteLesson(String sectionId, String lessonId);

	/**
	 * Reorders lessons within a section owned by the instructor
	 *
	 * @param sectionId
	 *            The ID of the section
	 * @param reorderLessonsDto
	 *            The lesson reorder data containing lesson IDs in intended order
	 * @return ApiResponse with success message
	 */
	ResponseEntity<ApiResponse<String>> reorderLessons(
			String sectionId, ReorderLessonsDto reorderLessonsDto);

	/**
	 * Marks a lesson as completed by the instructor Allows instructors to track
	 * which lessons have
	 * been finalized or verified during course creation or update
	 *
	 * @param sectionId
	 *            The ID of the section
	 * @param lessonId
	 *            The ID of the lesson to mark as completed
	 * @return ApiResponse with success message
	 */
	ResponseEntity<ApiResponse<String>> completeLesson(String sectionId, String lessonId);

	/**
	 * Creates a new lesson with quiz in a single transaction Allows instructors to
	 * create a lesson
	 * and attach a quiz in one operation
	 *
	 * @param sectionId
	 *            The ID of the section
	 * @param lessonWithQuizDto
	 *            The lesson and quiz creation data
	 * @return LessonWithQuizResponseDto containing the created lesson and quiz
	 *         details
	 */
	ResponseEntity<ApiResponse<LessonWithQuizResponseDto>> createLessonWithQuiz(
			String sectionId, CreateLessonWithQuizDto lessonWithQuizDto);

	/**
	 * Retrieves paginated quiz questions for a specific lesson owned by the
	 * instructor
	 *
	 * @param sectionId
	 *            The ID of the section containing the lesson
	 * @param lessonId
	 *            The ID of the lesson containing the quiz questions
	 * @param pageable
	 *            Pagination parameters
	 * @return PaginatedResponse containing quiz questions
	 */
	ResponseEntity<ApiResponse<PaginatedResponse<QuizQuestionResponseDto>>> getQuizQuestions(
			String sectionId, String lessonId, Pageable pageable);

	/**
	 * Retrieves paginated lesson submissions for a specific lesson owned by the
	 * instructor Includes
	 * both students who have submitted and those who haven't, along with summary
	 * statistics
	 *
	 * @param lessonId
	 *            The ID of the lesson to get submissions for
	 * @param pageable
	 *            Pagination parameters
	 * @return LessonSubmissionsResponseDto containing submissions data and summary
	 */
	ResponseEntity<ApiResponse<LessonSubmissionsResponseDto>> getSubmissions(
			String lessonId, Pageable pageable);

	/**
	 * Retrieves detailed information about a specific student submission for a
	 * lesson Only the
	 * instructor who owns the course can access this endpoint
	 *
	 * @param lessonId
	 *            The ID of the lesson
	 * @param submissionId
	 *            The ID of the submission
	 * @return SubmissionDetailResponseDto containing detailed submission
	 *         information
	 */
	ResponseEntity<ApiResponse<SubmissionDetailResponseDto>> getSubmissionDetails(
			String lessonId, String submissionId);
}
