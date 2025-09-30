package project.ktc.springboot_app.course.repositories;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.course.dto.projection.CourseReviewProjection;
import project.ktc.springboot_app.course.entity.Course;

@Repository
public interface AdminCourseRepository extends JpaRepository<Course, String> {

	@Query(value = """
			SELECT
			    c.id as id,
			    c.title as title,
			    c.description as description,
			    u.id as createdById,
			    u.name as createdByName,
			    c.created_at as createdAt,
			    crs.status as status,
			    (SELECT COUNT(*) FROM sections s WHERE s.course_id = c.id) as countSection,
			    (SELECT COUNT(*) FROM lessons l
			     JOIN sections s ON l.section_id = s.id
			     WHERE s.course_id = c.id) as countLesson,
			    (SELECT COALESCE(SUM(vc.duration), 0) FROM video_contents vc
			     JOIN lessons l ON l.content_id = vc.id
			     JOIN sections s ON l.section_id = s.id
			     WHERE s.course_id = c.id) as totalDuration,
			    crs.updated_at as statusUpdatedAt
			FROM courses c
			LEFT JOIN users u ON c.instructor_id = u.id
			LEFT JOIN course_review_status crs ON crs.course_id = c.id
			WHERE
			    crs.status IN :statuses
			    AND (:createdBy IS NULL OR c.instructor_id = :createdBy)
			    AND (:dateFrom IS NULL OR c.created_at >= :dateFrom)
			    AND (:dateTo IS NULL OR c.created_at <= :dateTo)
			    AND c.is_deleted = false
			ORDER BY c.created_at DESC
			""", countQuery = """
			SELECT COUNT(*)
			FROM courses c
			LEFT JOIN course_review_status crs ON crs.course_id = c.id
			WHERE
			    crs.status IN :statuses
			    AND (:createdBy IS NULL OR c.instructor_id = :createdBy)
			    AND (:dateFrom IS NULL OR c.created_at >= :dateFrom)
			    AND (:dateTo IS NULL OR c.created_at <= :dateTo)
			    AND c.is_deleted = false
			""", nativeQuery = true)
	Page<CourseReviewProjection> findCoursesForReview(
			@Param("statuses") List<String> statuses,
			@Param("createdBy") String createdBy,
			@Param("dateFrom") LocalDateTime dateFrom,
			@Param("dateTo") LocalDateTime dateTo,
			Pageable pageable);
}
