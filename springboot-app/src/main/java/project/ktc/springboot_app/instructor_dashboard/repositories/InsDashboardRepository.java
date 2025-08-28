package project.ktc.springboot_app.instructor_dashboard.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import project.ktc.springboot_app.course.entity.Course;

public interface InsDashboardRepository extends JpaRepository<Course, String> {
  // Get total instructor's courses
  @Query("SELECT COUNT(c) FROM Course c " +
      "WHERE c.instructor.id = :instructorId AND c.isDeleted = false")
  Long countTotalCoursesByInstructorId(String instructorId);

  // Get total active courses
  @Query("SELECT COUNT(c) FROM Course c WHERE c.instructor.id = :instructorId AND c.isDeleted = false AND c.isPublished = true AND c.isApproved = true")
  Long countTotalActiveCourses(String instructorId);
}
