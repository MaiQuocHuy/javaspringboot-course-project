package project.ktc.springboot_app.lesson.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.lesson.entity.Lesson;

@Repository
public interface InstructorLessonRepository extends JpaRepository<Lesson, String> {

	@Query("SELECT l FROM Lesson l " + "WHERE l.section.id = :sectionId " + "ORDER BY l.orderIndex ASC")
	List<Lesson> findLessonsBySectionIdOrderByOrder(@Param("sectionId") String sectionId);

	@Query("SELECT l FROM Lesson l "
			+ "WHERE l.section.id = :sectionId "
			+ "AND l.orderIndex > :orderIndex "
			+ "ORDER BY l.orderIndex ASC")
	List<Lesson> findLessonsBySectionIdAndOrderGreaterThan(
			@Param("sectionId") String sectionId, @Param("orderIndex") int orderIndex);

	@Query("SELECT COUNT(l) FROM Lesson l WHERE l.section.id = :sectionId")
	Long countLessonsBySectionId(@Param("sectionId") String sectionId);

	/** Check if lesson exists and instructor owns it through course */
	@Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END "
			+ "FROM Lesson l "
			+ "JOIN l.section s "
			+ "JOIN s.course c "
			+ "WHERE l.id = :lessonId AND c.instructor.id = :instructorId")
	boolean existsByIdAndSectionCourseInstructorId(
			@Param("lessonId") String lessonId, @Param("instructorId") String instructorId);

	/** Check if lesson belongs to the specified section */
	@Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END "
			+ "FROM Lesson l "
			+ "WHERE l.id = :lessonId AND l.section.id = :sectionId")
	boolean existsByIdAndSectionId(
			@Param("lessonId") String lessonId, @Param("sectionId") String sectionId);

	/** Check if section exists and instructor owns it through course */
	@Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END "
			+ "FROM Section s "
			+ "JOIN s.course c "
			+ "WHERE s.id = :sectionId AND c.instructor.id = :instructorId")
	boolean existsBySectionIdAndInstructorId(
			@Param("sectionId") String sectionId, @Param("instructorId") String instructorId);

	/** Check if lesson is of QUIZ type */
	@Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END "
			+ "FROM Lesson l "
			+ "JOIN l.lessonType lt "
			+ "WHERE l.id = :lessonId AND lt.name = 'QUIZ'")
	boolean isLessonOfQuizType(@Param("lessonId") String lessonId);
}
