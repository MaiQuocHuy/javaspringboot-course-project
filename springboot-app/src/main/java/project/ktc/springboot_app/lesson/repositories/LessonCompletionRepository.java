package project.ktc.springboot_app.lesson.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.entity.LessonCompletion;

@Repository
public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, String> {

  /** Find lesson completions for a specific user and list of lesson IDs */
  @Query(
      "SELECT lc FROM LessonCompletion lc WHERE lc.user.id = :userId AND lc.lesson.id IN :lessonIds")
  List<LessonCompletion> findByUserIdAndLessonIdIn(
      @Param("userId") String userId, @Param("lessonIds") Set<String> lessonIds);

  /** Check if a lesson is completed by a specific user */
  @Query(
      "SELECT COUNT(lc) > 0 FROM LessonCompletion lc WHERE lc.user.id = :userId AND lc.lesson.id = :lessonId")
  boolean existsByUserIdAndLessonId(
      @Param("userId") String userId, @Param("lessonId") String lessonId);

  /** Get all completed lesson IDs for a user in a specific section */
  @Query(
      "SELECT lc.lesson.id FROM LessonCompletion lc WHERE lc.user.id = :userId AND lc.lesson.section.id = :sectionId")
  List<String> findCompletedLessonIdsByUserAndSection(
      @Param("userId") String userId, @Param("sectionId") String sectionId);

  /** Find lesson completion by user ID and lesson ID */
  @Query(
      "SELECT lc FROM LessonCompletion lc WHERE lc.user.id = :userId AND lc.lesson.id = :lessonId")
  Optional<LessonCompletion> findByUserIdAndLessonId(
      @Param("userId") String userId, @Param("lessonId") String lessonId);

  /** Find recent lesson completions for activities */
  @Query(
      "SELECT lc FROM LessonCompletion lc "
          + "JOIN FETCH lc.lesson l "
          + "JOIN FETCH l.section s "
          + "JOIN FETCH s.course c "
          + "WHERE lc.user.id = :userId "
          + "ORDER BY lc.completedAt DESC")
  List<LessonCompletion> findRecentCompletionsByUserId(
      @Param("userId") String userId, org.springframework.data.domain.Pageable pageable);
}
