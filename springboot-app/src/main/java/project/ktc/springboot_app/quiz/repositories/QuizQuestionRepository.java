package project.ktc.springboot_app.quiz.repositories;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.entity.QuizQuestion;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, String> {

  @Query(
      "SELECT q FROM QuizQuestion q "
          + "WHERE q.lesson.id = :lessonId "
          + "ORDER BY q.createdAt ASC")
  List<QuizQuestion> findQuestionsByLessonId(@Param("lessonId") String lessonId);

  /** Find all quiz questions by lesson ID */
  List<QuizQuestion> findByLessonIdOrderByCreatedAtAsc(String lessonId);

  /** Count questions by lesson ID */
  long countByLessonId(String lessonId);

  /** Check if lesson has any quiz questions */
  boolean existsByLessonId(String lessonId);

  /** Find questions by lesson ID and instructor (for ownership verification) */
  @Query(
      "SELECT qq FROM QuizQuestion qq "
          + "JOIN qq.lesson l "
          + "JOIN l.section s "
          + "JOIN s.course c "
          + "WHERE qq.lesson.id = :lessonId AND c.instructor.id = :instructorId")
  List<QuizQuestion> findByLessonIdAndInstructorId(
      @Param("lessonId") String lessonId, @Param("instructorId") String instructorId);

  /** Delete all questions by lesson ID */
  void deleteByLessonId(String lessonId);

  /** Find all quiz questions for a lesson ordered by creation date */
  List<QuizQuestion> findByLessonIdOrderByCreatedAt(String lessonId);

  /** Find all quiz questions for a lesson */
  List<QuizQuestion> findByLessonId(String lessonId);

  /**
   * Find quiz questions by lesson with instructor ownership validation and pagination This query
   * joins through lesson -> section -> course -> instructor to ensure the instructor owns the
   * course containing the quiz questions
   */
  @Query(
      "SELECT qq FROM QuizQuestion qq "
          + "JOIN qq.lesson l "
          + "JOIN l.section s "
          + "JOIN s.course c "
          + "WHERE l.id = :lessonId "
          + "AND s.id = :sectionId "
          + "AND c.instructor.id = :instructorId "
          + "ORDER BY qq.createdAt ASC")
  Page<QuizQuestion> findQuizQuestionsByLessonAndOwnership(
      @Param("lessonId") String lessonId,
      @Param("sectionId") String sectionId,
      @Param("instructorId") String instructorId,
      Pageable pageable);
}
