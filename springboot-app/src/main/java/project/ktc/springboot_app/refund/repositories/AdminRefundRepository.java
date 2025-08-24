package project.ktc.springboot_app.refund.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import project.ktc.springboot_app.refund.entity.Refund;

public interface AdminRefundRepository extends RefundRepository {

        /**
         * Get all refunds with pagination support for admin
         * Orders by creation date descending (most recent first)
         * Includes payment information
         */
        @Query("SELECT r FROM Refund r " +
                        "LEFT JOIN FETCH r.payment p " +
                        "LEFT JOIN FETCH p.user u " +
                        "ORDER BY r.requestedAt DESC")
        Page<Refund> findAllRefunds(Pageable pageable);

        /**
         * Get all payments without pagination for admin
         * Orders by creation date descending (most recent first)
         * Includes user and course information
         */
        @Query("SELECT r FROM Refund r " +
                        "LEFT JOIN FETCH r.payment p " +
                        "LEFT JOIN FETCH p.user u " +
                        "ORDER BY r.requestedAt DESC")
        List<Refund> findAllRefunds();

        /**
         * Get refund by ID with full details including user and course for admin
         * Returns refund with all related information
         */
        @Query("SELECT r FROM Refund r " +
                        "LEFT JOIN FETCH r.payment p " +
                        "LEFT JOIN FETCH p.user u " +
                        "LEFT JOIN FETCH p.course c " +
                        "LEFT JOIN FETCH c.instructor " +
                        "WHERE r.id = :refundId")
        Optional<Refund> findRefundByIdWithDetails(@Param("refundId") String refundId);

        /**
         * Count all refunds
         */
        @Query("SELECT COUNT(r) FROM Refund r")
        Long countAllRefunds();

        /**
         * Count refunds by status
         */
        @Query("SELECT COUNT(r) FROM Refund r WHERE r.status = :status")
        Long countRefundsByStatus(@Param("status") Refund.RefundStatus status);

        /**
         * Count pending refunds
         */
        @Query("SELECT COUNT(r) FROM Refund r WHERE r.status = 'PENDING'")
        Long countPendingRefunds();

        /**
         * Count completed refunds
         */
        @Query("SELECT COUNT(r) FROM Refund r WHERE r.status = 'COMPLETED'")
        Long countCompletedRefunds();

        /**
         * Count failed refunds
         */
        @Query("SELECT COUNT(r) FROM Refund r WHERE r.status = 'FAILED'")
        Long countFailedRefunds();

}
