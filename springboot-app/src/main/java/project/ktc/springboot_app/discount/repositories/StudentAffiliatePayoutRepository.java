package project.ktc.springboot_app.discount.repositories;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.discount.entity.AffiliatePayout;
import project.ktc.springboot_app.discount.enums.PayoutStatus;

/**
 * Repository for student affiliate payout operations Handles queries for
 * affiliate payouts from
 * student perspective
 */
@Repository
public interface StudentAffiliatePayoutRepository extends JpaRepository<AffiliatePayout, String> {

	/** Find all affiliate payouts for a student (as referrer) */
	@Query("""
			SELECT ap FROM AffiliatePayout ap
			JOIN FETCH ap.referredByUser u
			JOIN FETCH ap.course c
			LEFT JOIN FETCH ap.discountUsage du
			WHERE ap.referredByUser.id = :studentId
			ORDER BY ap.createdAt DESC
			""")
	Page<AffiliatePayout> findByReferredByUserId(
			@Param("studentId") String studentId, Pageable pageable);

	/** Count total affiliate payouts by student and status */
	@Query("SELECT COUNT(ap) FROM AffiliatePayout ap WHERE ap.referredByUser.id = :studentId")
	Long countByReferredByUserId(@Param("studentId") String studentId);

	/** Count affiliate payouts by student and status */
	@Query("SELECT COUNT(ap) FROM AffiliatePayout ap WHERE ap.referredByUser.id = :studentId AND ap.payoutStatus = :status")
	Long countByReferredByUserIdAndPayoutStatus(
			@Param("studentId") String studentId, @Param("status") PayoutStatus status);

	/** Calculate total commission amount by student and status */
	@Query("SELECT COALESCE(SUM(ap.commissionAmount), 0) FROM AffiliatePayout ap WHERE ap.referredByUser.id = :studentId")
	BigDecimal sumCommissionAmountByReferredByUserId(@Param("studentId") String studentId);

	/** Calculate commission amount by student and status */
	@Query("SELECT COALESCE(SUM(ap.commissionAmount), 0) FROM AffiliatePayout ap WHERE ap.referredByUser.id = :studentId AND ap.payoutStatus = :status")
	BigDecimal sumCommissionAmountByReferredByUserIdAndPayoutStatus(
			@Param("studentId") String studentId, @Param("status") PayoutStatus status);
}
