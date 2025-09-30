package project.ktc.springboot_app.course.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.course.entity.CourseReviewStatus;

@Repository
public interface CourseReviewStatusRepository extends JpaRepository<CourseReviewStatus, String> {

  @Query("SELECT crs FROM CourseReviewStatus crs WHERE crs.course.id = :courseId")
  Optional<CourseReviewStatus> findByCourseId(@Param("courseId") String courseId);
}
