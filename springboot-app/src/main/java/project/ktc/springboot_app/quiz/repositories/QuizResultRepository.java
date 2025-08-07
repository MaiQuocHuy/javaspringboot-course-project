package project.ktc.springboot_app.quiz.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.entity.QuizResult;

import java.util.Optional;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, String> {

    /**
     * Find quiz result by user ID and lesson ID
     */
    @Query("SELECT qr FROM QuizResult qr WHERE qr.user.id = :userId AND qr.lesson.id = :lessonId")
    Optional<QuizResult> findByUserIdAndLessonId(@Param("userId") String userId, @Param("lessonId") String lessonId);

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
}
