package project.ktc.springboot_app.lesson.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.entity.LessonCompletion;

import java.util.List;
import java.util.Set;

@Repository
public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, String> {

    /**
     * Find lesson completions for a specific user and list of lesson IDs
     */
    @Query("SELECT lc FROM LessonCompletion lc WHERE lc.user.id = :userId AND lc.lesson.id IN :lessonIds")
    List<LessonCompletion> findByUserIdAndLessonIdIn(@Param("userId") String userId,
            @Param("lessonIds") Set<String> lessonIds);

    /**
     * Check if a lesson is completed by a specific user
     */
    @Query("SELECT COUNT(lc) > 0 FROM LessonCompletion lc WHERE lc.user.id = :userId AND lc.lesson.id = :lessonId")
    boolean existsByUserIdAndLessonId(@Param("userId") String userId, @Param("lessonId") String lessonId);

    /**
     * Get all completed lesson IDs for a user in a specific section
     */
    @Query("SELECT lc.lesson.id FROM LessonCompletion lc WHERE lc.user.id = :userId AND lc.lesson.section.id = :sectionId")
    List<String> findCompletedLessonIdsByUserAndSection(@Param("userId") String userId,
            @Param("sectionId") String sectionId);
}
