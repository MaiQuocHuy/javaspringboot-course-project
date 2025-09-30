package project.ktc.springboot_app.course.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.course.entity.CourseReviewStatusHistory;

@Repository
public interface CourseReviewStatusHistoryRepository
    extends JpaRepository<CourseReviewStatusHistory, String> {

  /**
   * Find the latest course review status history record for a specific course by ordering by
   * createdAt DESC and limiting to 1 result.
   *
   * @param courseId the course ID to search for
   * @return Optional containing the latest status history record, or empty if none found
   */
  @Query(
      "SELECT h FROM CourseReviewStatusHistory h "
          + "JOIN h.courseReview cr "
          + "WHERE cr.course.id = :courseId "
          + "ORDER BY h.createdAt DESC")
  List<CourseReviewStatusHistory> findByCourseIdOrderByCreatedAtDesc(
      @Param("courseId") String courseId);

  /**
   * Find the latest course review status history record for a specific course
   *
   * @param courseId the course ID to search for
   * @return Optional containing the latest status history record, or empty if none found
   */
  default Optional<CourseReviewStatusHistory> findLatestByCourseId(String courseId) {
    List<CourseReviewStatusHistory> histories = findByCourseIdOrderByCreatedAtDesc(courseId);
    return histories.isEmpty() ? Optional.empty() : Optional.of(histories.get(0));
  }
}
