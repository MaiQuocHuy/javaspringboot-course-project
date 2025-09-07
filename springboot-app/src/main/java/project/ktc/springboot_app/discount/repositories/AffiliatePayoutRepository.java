package project.ktc.springboot_app.discount.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.discount.entity.AffiliatePayout;
import project.ktc.springboot_app.discount.enums.PayoutStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AffiliatePayout entity operations
 */
@Repository
public interface AffiliatePayoutRepository extends JpaRepository<AffiliatePayout, String> {

    /**
     * Find payouts by user ID with pagination
     */
    Page<AffiliatePayout> findByReferredByUserId(String referredByUserId, Pageable pageable);

    /**
     * Find payouts by status
     */
    List<AffiliatePayout> findByPayoutStatus(PayoutStatus status);

    /**
     * Find payouts by status with pagination
     */
    Page<AffiliatePayout> findByPayoutStatus(PayoutStatus status, Pageable pageable);

    /**
     * Find payouts by user and status
     */
    List<AffiliatePayout> findByReferredByUserIdAndPayoutStatus(String referredByUserId, PayoutStatus status);

    /**
     * Find payouts by course ID
     */
    List<AffiliatePayout> findByCourseId(String courseId);

    /**
     * Calculate total pending payout amount for a user
     */
    @Query("SELECT COALESCE(SUM(ap.commissionAmount), 0) FROM AffiliatePayout ap " +
            "WHERE ap.referredByUser.id = :userId AND ap.payoutStatus = 'PENDING'")
    Double getTotalPendingPayoutByUserId(@Param("userId") String userId);

    /**
     * Calculate total paid amount for a user
     */
    @Query("SELECT COALESCE(SUM(ap.commissionAmount), 0) FROM AffiliatePayout ap " +
            "WHERE ap.referredByUser.id = :userId AND ap.payoutStatus = 'PAID'")
    Double getTotalPaidAmountByUserId(@Param("userId") String userId);

    /**
     * Find payouts within date range
     */
    @Query("SELECT ap FROM AffiliatePayout ap WHERE ap.createdAt BETWEEN :startDate AND :endDate")
    List<AffiliatePayout> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find paid payouts within date range
     */
    @Query("SELECT ap FROM AffiliatePayout ap WHERE ap.paidAt BETWEEN :startDate AND :endDate")
    List<AffiliatePayout> findByPaidAtBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count payouts by status
     */
    long countByPayoutStatus(PayoutStatus status);

    /**
     * Count payouts for a specific user by status
     */
    long countByReferredByUserIdAndPayoutStatus(String referredByUserId, PayoutStatus status);

    /**
     * Find pending payouts older than specified date (for processing)
     */
    @Query("SELECT ap FROM AffiliatePayout ap WHERE ap.payoutStatus = 'PENDING' " +
            "AND ap.createdAt < :beforeDate")
    List<AffiliatePayout> findPendingPayoutsOlderThan(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * Find payouts by discount usage ID
     */
    List<AffiliatePayout> findByDiscountUsageId(String discountUsageId);

    /**
     * Get payout statistics for analytics
     */
    @Query("SELECT ap.payoutStatus, COUNT(ap), SUM(ap.commissionAmount) " +
            "FROM AffiliatePayout ap " +
            "WHERE ap.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY ap.payoutStatus")
    List<Object[]> getPayoutStats(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
