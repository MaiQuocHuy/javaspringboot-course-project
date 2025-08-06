package project.ktc.springboot_app.quiz.repositories;

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
}
