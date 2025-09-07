package project.ktc.springboot_app.discount.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.discount.entity.DiscountUsage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for DiscountUsage entity operations
 */
@Repository
public interface DiscountUsageRepository extends JpaRepository<DiscountUsage, String> {

        /**
         * Count usage by discount ID
         */
        long countByDiscountId(String discountId);

        /**
         * Count usage by user ID and discount ID
         */
        long countByUserIdAndDiscountId(String userId, String discountId);

        /**
         * Find usage by discount ID with pagination
         */
        Page<DiscountUsage> findByDiscountId(String discountId, Pageable pageable);

        /**
         * Find usage by user ID with pagination
         */
        Page<DiscountUsage> findByUserId(String userId, Pageable pageable);

        /**
         * Find usage by course ID
         */
        List<DiscountUsage> findByCourseId(String courseId);

        /**
         * Find usage by user ID and course ID
         */
        List<DiscountUsage> findByUserIdAndCourseId(String userId, String courseId);

        /**
         * Find usage by referrer user ID
         */
        List<DiscountUsage> findByReferredByUserId(String referredByUserId);

        /**
         * Find usage within date range
         */
        @Query("SELECT du FROM DiscountUsage du WHERE du.usedAt BETWEEN :startDate AND :endDate")
        List<DiscountUsage> findByUsedAtBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Calculate total discount amount by discount ID
         */
        @Query("SELECT COALESCE(SUM(du.discountAmount), 0) FROM DiscountUsage du WHERE du.discount.id = :discountId")
        Double getTotalDiscountAmountByDiscountId(@Param("discountId") String discountId);

        /**
         * Calculate total discount amount by user ID
         */
        @Query("SELECT COALESCE(SUM(du.discountAmount), 0) FROM DiscountUsage du WHERE du.user.id = :userId")
        Double getTotalDiscountAmountByUserId(@Param("userId") String userId);

        /**
         * Find recent usage for a user (for recommendations/analytics)
         */
        @Query("SELECT du FROM DiscountUsage du WHERE du.user.id = :userId " +
                        "ORDER BY du.usedAt DESC")
        Page<DiscountUsage> findRecentUsageByUserId(@Param("userId") String userId, Pageable pageable);

        /**
         * Check if user has used a specific discount
         */
        boolean existsByUserIdAndDiscountId(String userId, String discountId);

        /**
         * Find usage for analytics - discount performance
         */
        @Query("SELECT du.discount.id, COUNT(du), SUM(du.discountAmount) " +
                        "FROM DiscountUsage du " +
                        "WHERE du.usedAt BETWEEN :startDate AND :endDate " +
                        "GROUP BY du.discount.id")
        List<Object[]> getDiscountUsageStats(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);
}
