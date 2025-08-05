package project.ktc.springboot_app.quiz.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.entity.QuizQuestion;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, String> {

    @Query("SELECT q FROM QuizQuestion q " +
            "WHERE q.lesson.id = :lessonId " +
            "ORDER BY q.createdAt ASC")
    List<QuizQuestion> findQuestionsByLessonId(@Param("lessonId") String lessonId);

    /**
     * Find all quiz questions by lesson ID
     */
    List<QuizQuestion> findByLessonIdOrderByCreatedAtAsc(String lessonId);

    /**
     * Count questions by lesson ID
     */
    long countByLessonId(String lessonId);

    /**
     * Check if lesson has any quiz questions
     */
    boolean existsByLessonId(String lessonId);

    /**
     * Find questions by lesson ID and instructor (for ownership verification)
     */
    @Query("SELECT qq FROM QuizQuestion qq " +
            "JOIN qq.lesson l " +
            "JOIN l.section s " +
            "JOIN s.course c " +
            "WHERE qq.lesson.id = :lessonId AND c.instructor.id = :instructorId")
    List<QuizQuestion> findByLessonIdAndInstructorId(@Param("lessonId") String lessonId,
            @Param("instructorId") String instructorId);

    /**
     * Delete all questions by lesson ID
     */
    void deleteByLessonId(String lessonId);

    /**
     * Find all quiz questions for a lesson ordered by creation date
     */
    List<QuizQuestion> findByLessonIdOrderByCreatedAt(String lessonId);

    /**
     * Find all quiz questions for a lesson
     */
    List<QuizQuestion> findByLessonId(String lessonId);

}
