package project.ktc.springboot_app.discount.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.discount.entity.AffiliatePayout;

/**
 * Repository for instructor affiliate payout operations
 * Handles queries for affiliate payouts from instructor perspective
 */
@Repository
public interface InstructorAffiliatePayoutRepository extends JpaRepository<AffiliatePayout, String> {

    /**
     * Find all affiliate payouts for courses owned by the instructor
     */
    @Query("""
            SELECT ap FROM AffiliatePayout ap
            JOIN FETCH ap.referredByUser u
            JOIN FETCH ap.course c
            LEFT JOIN FETCH ap.discountUsage du
            WHERE c.instructor.id = :instructorId
            ORDER BY ap.createdAt DESC
            """)
    Page<AffiliatePayout> findByCourseInstructorId(@Param("instructorId") String instructorId, Pageable pageable);

    /**
     * Count total affiliate payouts for instructor's courses
     */
    @Query("SELECT COUNT(ap) FROM AffiliatePayout ap WHERE ap.course.instructor.id = :instructorId")
    Long countByCourseInstructorId(@Param("instructorId") String instructorId);
}
