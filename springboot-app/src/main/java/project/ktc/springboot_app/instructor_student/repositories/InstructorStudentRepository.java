package project.ktc.springboot_app.instructor_student.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.enrollment.entity.Enrollment;

@Repository
public interface InstructorStudentRepository extends JpaRepository<Enrollment, String> {
  // Get total instructor's enrolled student
  @Query(
      value =
          "SELECT DISTINCT(e.user.id) FROM Enrollment e "
              + "INNER JOIN e.course c "
              + "WHERE c.instructor.id = :instructorId")
  List<String> countTotalEnrolledStudents(String instructorId);

  // Get instructor's enrolled student Ids with filters
  @Query(
      value =
          "SELECT DISTINCT(e.user.id) FROM Enrollment e "
              + "INNER JOIN e.course c "
              + "WHERE c.instructor.id = :instructorId AND (:search IS NULL OR (e.user.name LIKE %:search% OR e.user.email LIKE %:search%))")
  List<String> countEnrolledStudentIdsWithFilters(String instructorId, String search);

  // Get instructor's enrolled student
  // @Query(value = "SELECT e.user FROM Enrollment e " +
  // "INNER JOIN e.course c " +
  // "WHERE c.instructor.id = :instructorId AND c.id = :courseId")
  // List<String> getEnrolledStudentsByInstructorId(String instructorId, String
  // courseId);

  // Get student information
  @Query(
      value =
          "SELECT u.id, u.name, u.email, u.thumbnail_url FROM users u" + " WHERE u.id = :studentId",
      nativeQuery = true)
  List<Object[]> getStudentById(String studentId);

  // Get enrolled student's courses
  @Query(
      value =
          "SELECT c.id as courseId, c.title FROM Course c "
              + "INNER JOIN c.enrollments e ON c.id = e.course.id "
              + "WHERE c.instructor.id = :instructorId AND e.user.id = :studentId")
  List<Object[]> getStudentCourses(String instructorId, String studentId);

  // Get student's courses details
  @Query(
      "SELECT DISTINCT c, e.enrolledAt FROM Course c "
          + "LEFT JOIN fetch c.categories "
          + "INNER JOIN c.enrollments e "
          + "WHERE c.instructor.id = :instructorId AND e.user.id = :studentId")
  List<Object[]> getStudentCoursesDetails(String instructorId, String studentId);

  @Query(
      "SELECT COUNT(e) FROM Enrollment e WHERE YEAR(e.enrolledAt) = :year AND MONTH(e.enrolledAt) = :month")
  Long countStudentsEnrolledByMonth(int year, int month);

  // Get course's enrolled students
  @Query(
      "SELECT e.user.id, e.user.name, e.user.email, e.user.thumbnailUrl, e.enrolledAt FROM Enrollment e "
          + "INNER JOIN Course c ON e.course.id = c.id "
          + "WHERE c.instructor.id = :instructorId AND c.id = :courseId ")
  List<Object[]> getCourseEnrolledStudents(String instructorId, String courseId);
}
