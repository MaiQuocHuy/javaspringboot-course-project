package project.ktc.springboot_app.discount.repositories;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.discount.entity.AffiliatePayout;
import project.ktc.springboot_app.discount.enums.PayoutStatus;

/**
 * Repository for instructor affiliate payout operations Handles queries for
 * affiliate payouts from
 * instructor perspective
 */
@Repository
public interface InstructorAffiliatePayoutRepository
		extends JpaRepository<AffiliatePayout, String> {

	/** Find all affiliate payouts for courses owned by the instructor */
	@Query("""
			SELECT ap FROM AffiliatePayout ap
			JOIN FETCH ap.referredByUser u
			JOIN FETCH ap.course c
			LEFT JOIN FETCH ap.discountUsage du
			WHERE c.instructor.id = :instructorId
			ORDER BY ap.createdAt DESC
			""")
	Page<AffiliatePayout> findByCourseInstructorId(
			@Param("instructorId") String instructorId, Pageable pageable);

	/**
	 * Find a specific affiliate payout by ID that belongs to instructor's courses
	 */
	@Query("""
			SELECT ap FROM AffiliatePayout ap
			JOIN FETCH ap.referredByUser u
			JOIN FETCH ap.course c
			LEFT JOIN FETCH ap.discountUsage du
			LEFT JOIN FETCH du.discount d
			LEFT JOIN FETCH du.user du_user
			WHERE ap.id = :affiliatePayoutId AND c.instructor.id = :instructorId
			""")
	AffiliatePayout findByIdAndCourseInstructorId(
			@Param("affiliatePayoutId") String affiliatePayoutId,
			@Param("instructorId") String instructorId);

	/** Count total affiliate payouts for instructor's courses */
	@Query("SELECT COUNT(ap) FROM AffiliatePayout ap WHERE ap.course.instructor.id = :instructorId")
	Long countByCourseInstructorId(@Param("instructorId") String instructorId);

	/**
	 * Find affiliate payouts for instructor's courses with search and filter
	 * options
	 */
	@Query("""
			SELECT ap FROM AffiliatePayout ap
			JOIN FETCH ap.referredByUser u
			JOIN FETCH ap.course c
			LEFT JOIN FETCH ap.discountUsage du
			LEFT JOIN FETCH du.discount d
			WHERE c.instructor.id = :instructorId
			AND (:search IS NULL OR :search = '' OR
			     LOWER(ap.id) LIKE LOWER(CONCAT('%', :search, '%')) OR
			     LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
			     LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
			     (d IS NOT NULL AND LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%'))))
			AND (:status IS NULL OR ap.payoutStatus = :status)
			AND (:fromDate IS NULL OR DATE(ap.createdAt) >= :fromDate)
			AND (:toDate IS NULL OR DATE(ap.createdAt) <= :toDate)
			ORDER BY ap.createdAt DESC
			""")
	Page<AffiliatePayout> findByCourseInstructorIdWithFilter(
			@Param("instructorId") String instructorId,
			@Param("search") String search,
			@Param("status") PayoutStatus status,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate,
			Pageable pageable);
}
