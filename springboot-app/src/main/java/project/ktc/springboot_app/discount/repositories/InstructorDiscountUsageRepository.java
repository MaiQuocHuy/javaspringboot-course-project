package project.ktc.springboot_app.discount.repositories;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.discount.entity.DiscountUsage;
import project.ktc.springboot_app.discount.enums.DiscountType;

/**
 * Repository for instructor discount usage operations Handles queries for
 * discount usage from
 * instructor perspective
 */
@Repository
public interface InstructorDiscountUsageRepository extends JpaRepository<DiscountUsage, String> {

	/** Find all discount usages for courses owned by the instructor */
	@Query("""
			SELECT du FROM DiscountUsage du
			JOIN FETCH du.discount d
			JOIN FETCH du.user u
			JOIN FETCH du.course c
			LEFT JOIN FETCH du.referredByUser rbu
			WHERE c.instructor.id = :instructorId
			ORDER BY du.usedAt DESC
			""")
	Page<DiscountUsage> findByCourseInstructorId(
			@Param("instructorId") String instructorId, Pageable pageable);

	/** Find a specific discount usage by ID that belongs to instructor's courses */
	@Query("""
			SELECT du FROM DiscountUsage du
			JOIN FETCH du.discount d
			JOIN FETCH du.user u
			JOIN FETCH du.course c
			LEFT JOIN FETCH du.referredByUser rbu
			WHERE du.id = :discountUsageId AND c.instructor.id = :instructorId
			""")
	DiscountUsage findByIdAndCourseInstructorId(
			@Param("discountUsageId") String discountUsageId, @Param("instructorId") String instructorId);

	/** Count total discount usages for instructor's courses */
	@Query("SELECT COUNT(du) FROM DiscountUsage du WHERE du.course.instructor.id = :instructorId")
	Long countByCourseInstructorId(@Param("instructorId") String instructorId);

	/**
	 * Find all discount usages for courses owned by the instructor with search and
	 * filtering Search
	 * by discount usage ID, discount code, user name, or course title Filter by
	 * discount type and
	 * date range
	 */
	@Query("""
			SELECT du FROM DiscountUsage du
			JOIN FETCH du.discount d
			JOIN FETCH du.user u
			JOIN FETCH du.course c
			LEFT JOIN FETCH du.referredByUser rbu
			WHERE c.instructor.id = :instructorId
			AND (:search IS NULL OR :search = '' OR
			    LOWER(du.id) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')))
			AND (:type IS NULL OR d.type = :type)
			AND (:fromDate IS NULL OR DATE(du.usedAt) >= :fromDate)
			AND (:toDate IS NULL OR DATE(du.usedAt) <= :toDate)
			ORDER BY du.usedAt DESC
			""")
	Page<DiscountUsage> findByCourseInstructorIdWithFilter(
			@Param("instructorId") String instructorId,
			@Param("search") String search,
			@Param("type") DiscountType type,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate,
			Pageable pageable);
}
