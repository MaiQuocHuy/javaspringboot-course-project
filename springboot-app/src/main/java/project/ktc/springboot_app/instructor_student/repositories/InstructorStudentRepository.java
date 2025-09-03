package project.ktc.springboot_app.instructor_student.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.enrollment.entity.Enrollment;

@Repository
public interface InstructorStudentRepository extends JpaRepository<Enrollment, String> {
        // Get instructor's enrolled students
        @Query(value = "SELECT DISTINCT(e.user.id) FROM Enrollment e " +
                        "INNER JOIN e.course c " +
                        "WHERE c.instructor.id = :instructorId")
        List<String> getEnrolledStudentsByInstructorId(String instructorId);

        // Get student information
        @Query(value = "SELECT u.id, u.name, u.email, u.thumbnail_url FROM Users u" +
                        " WHERE u.id = :studentId", nativeQuery = true)
        List<Object[]> getStudentById(String studentId);

        // Get student's courses
        @Query(value = "SELECT c.id as courseId, c.title FROM Course c " +
                        "INNER JOIN c.enrollments e ON c.id = e.course.id " +
                        "WHERE e.user.id = :studentId")
        List<Object[]> getStudentCourses(String studentId);

        // Get student's courses details
        @Query("SELECT DISTINCT c, e.enrolledAt FROM Course c "
                        +
                        "LEFT JOIN fetch c.categories " +
                        "INNER JOIN c.enrollments e " +
                        "WHERE e.user.id = :studentId")
        List<Object[]> getStudentCoursesDetails(String studentId);

        @Query("SELECT COUNT(e) FROM Enrollment e WHERE YEAR(e.enrolledAt) = :year AND MONTH(e.enrolledAt) = :month")
        Long countStudentsEnrolledByMonth(int year, int month);
}
