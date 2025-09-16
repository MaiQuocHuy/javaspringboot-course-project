package project.ktc.springboot_app.quiz.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.entity.QuizResult;
import project.ktc.springboot_app.lesson.dto.StudentSubmissionDto;
import project.ktc.springboot_app.lesson.dto.SubmissionSummaryDto;

import java.util.Optional;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, String> {

        /**
         * Find quiz result by user ID and lesson ID
         */
        @Query("SELECT qr FROM QuizResult qr WHERE qr.user.id = :userId AND qr.lesson.id = :lessonId")
        Optional<QuizResult> findByUserIdAndLessonId(@Param("userId") String userId,
                        @Param("lessonId") String lessonId);

        /**
         * Check if user has submitted quiz for a specific lesson
         */
        @Query("SELECT COUNT(qr) > 0 FROM QuizResult qr WHERE qr.user.id = :userId AND qr.lesson.id = :lessonId")
        boolean existsByUserIdAndLessonId(@Param("userId") String userId, @Param("lessonId") String lessonId);

        /**
         * Delete quiz result by user ID and lesson ID
         */
        void deleteByUserIdAndLessonId(String userId, String lessonId);

        /**
         * Find all quiz results for a specific user in courses they are enrolled in
         * with lesson, section, and course information
         */
        @Query("SELECT qr FROM QuizResult qr " +
                        "JOIN FETCH qr.lesson l " +
                        "JOIN FETCH l.section s " +
                        "JOIN FETCH s.course c " +
                        "JOIN FETCH qr.user u " +
                        "WHERE u.id = :userId " +
                        "AND EXISTS (SELECT 1 FROM Enrollment e WHERE e.user.id = :userId AND e.course.id = c.id)")
        Page<QuizResult> findQuizScoresByUserId(@Param("userId") String userId, Pageable pageable);

        /**
         * Count total questions for a specific lesson by counting quiz questions
         */
        @Query("SELECT COUNT(qq) FROM QuizQuestion qq WHERE qq.lesson.id = :lessonId")
        Long countQuestionsByLessonId(@Param("lessonId") String lessonId);

        /**
         * Check if user can review a specific quiz result
         * For now, all completed quizzes can be reviewed
         */
        @Query("SELECT CASE WHEN COUNT(qr) > 0 THEN true ELSE false END " +
                        "FROM QuizResult qr " +
                        "WHERE qr.id = :quizResultId AND qr.user.id = :userId")
        Boolean canUserReviewQuiz(@Param("quizResultId") String quizResultId, @Param("userId") String userId);

        /**
         * Find a specific quiz result for a user with lesson, section, and course
         * information
         */
        @Query("SELECT qr FROM QuizResult qr " +
                        "JOIN FETCH qr.lesson l " +
                        "JOIN FETCH l.section s " +
                        "JOIN FETCH s.course c " +
                        "JOIN FETCH qr.user u " +
                        "WHERE qr.id = :quizResultId AND u.id = :userId " +
                        "AND EXISTS (SELECT 1 FROM Enrollment e WHERE e.user.id = :userId AND e.course.id = c.id)")
        Optional<QuizResult> findQuizResultByIdAndUserId(@Param("quizResultId") String quizResultId,
                        @Param("userId") String userId);

        /**
         * Find all enrolled students with their submission status for a lesson
         * Returns both students who have submitted and those who haven't
         */
        @Query("SELECT new project.ktc.springboot_app.lesson.dto.StudentSubmissionDto(" +
                        "u.id, u.name, u.email, qr.id, qr.score, qr.completedAt) " +
                        "FROM Enrollment e " +
                        "JOIN e.user u " +
                        "JOIN e.course c " +
                        "JOIN c.sections s " +
                        "JOIN s.lessons l " +
                        "LEFT JOIN QuizResult qr ON (qr.user.id = u.id AND qr.lesson.id = l.id) " +
                        "WHERE l.id = :lessonId " +
                        "AND c.instructor.id = :instructorId " +
                        "ORDER BY qr.completedAt DESC NULLS LAST, u.name ASC")
        Page<StudentSubmissionDto> findAllStudentSubmissionsByLesson(
                        @Param("lessonId") String lessonId,
                        @Param("instructorId") String instructorId,
                        Pageable pageable);

        /**
         * Calculate summary statistics for lesson submissions
         */
        @Query("SELECT new project.ktc.springboot_app.lesson.dto.SubmissionSummaryDto(" +
                        "COUNT(DISTINCT e.user.id), " +
                        "COUNT(DISTINCT qr.id), " +
                        "AVG(qr.score)) " +
                        "FROM Enrollment e " +
                        "JOIN e.course c " +
                        "JOIN c.sections s " +
                        "JOIN s.lessons l " +
                        "LEFT JOIN QuizResult qr ON (qr.user.id = e.user.id AND qr.lesson.id = l.id) " +
                        "WHERE l.id = :lessonId " +
                        "AND c.instructor.id = :instructorId")
        SubmissionSummaryDto getSubmissionSummary(
                        @Param("lessonId") String lessonId,
                        @Param("instructorId") String instructorId);

        /**
         * Count total enrolled students for pagination
         */
        @Query("SELECT COUNT(DISTINCT e.user.id) FROM Enrollment e " +
                        "JOIN e.course c " +
                        "JOIN c.sections s " +
                        "JOIN s.lessons l " +
                        "WHERE l.id = :lessonId " +
                        "AND c.instructor.id = :instructorId")
        Long countEnrolledStudentsByLesson(
                        @Param("lessonId") String lessonId,
                        @Param("instructorId") String instructorId);

        /**
         * Find submission details by submission ID, lesson ID, and instructor ownership
         * Validates that the instructor owns the course containing the lesson
         */
        @Query("SELECT qr FROM QuizResult qr " +
                        "JOIN FETCH qr.user u " +
                        "JOIN FETCH qr.lesson l " +
                        "JOIN FETCH l.section s " +
                        "JOIN FETCH s.course c " +
                        "WHERE qr.id = :submissionId " +
                        "AND l.id = :lessonId " +
                        "AND c.instructor.id = :instructorId")
        Optional<QuizResult> findSubmissionDetailsByInstructor(
                        @Param("submissionId") String submissionId,
                        @Param("lessonId") String lessonId,
                        @Param("instructorId") String instructorId);

        /**
         * Find recent quiz submissions for activities
         */
        @Query("SELECT qr FROM QuizResult qr " +
                        "JOIN FETCH qr.lesson l " +
                        "JOIN FETCH l.section s " +
                        "JOIN FETCH s.course c " +
                        "WHERE qr.user.id = :userId " +
                        "ORDER BY qr.completedAt DESC")
        java.util.List<QuizResult> findRecentSubmissionsByUserId(@Param("userId") String userId,
                        org.springframework.data.domain.Pageable pageable);

        /**
         * Quiz Statistics Methods
         */

        /**
         * Count total quiz submissions by user ID
         */
        @Query("SELECT COUNT(qr) FROM QuizResult qr WHERE qr.user.id = :userId")
        Long countTotalQuizzesByUserId(@Param("userId") String userId);

        /**
         * Count passed quiz submissions (score >= 80) by user ID
         */
        @Query("SELECT COUNT(qr) FROM QuizResult qr WHERE qr.user.id = :userId AND qr.score >= 80")
        Long countPassedQuizzesByUserId(@Param("userId") String userId);

        /**
         * Count failed quiz submissions (score < 80) by user ID
         */
        @Query("SELECT COUNT(qr) FROM QuizResult qr WHERE qr.user.id = :userId AND qr.score < 80")
        Long countFailedQuizzesByUserId(@Param("userId") String userId);

        /**
         * Calculate average score for all quiz submissions by user ID
         */
        @Query("SELECT AVG(qr.score) FROM QuizResult qr WHERE qr.user.id = :userId")
        Double calculateAverageScoreByUserId(@Param("userId") String userId);
}
