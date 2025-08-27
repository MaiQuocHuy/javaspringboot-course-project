package project.ktc.springboot_app.refund.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.refund.entity.Refund;

@Repository
public interface InstructorRefundRepository extends JpaRepository<Refund, String> {

    /**
     * Get all refunds with pagination support for instructor
     * Orders by creation date descending (most recent first)
     * Includes payment information
     */
    @Query("SELECT r FROM Refund r " +
            "LEFT JOIN FETCH r.payment p " +
            "LEFT JOIN FETCH p.user u " +
            "LEFT JOIN FETCH p.course c " +
            "WHERE c.instructor.id = :instructorId " +
            "ORDER BY r.requestedAt DESC")
    Page<Refund> findAllRefundsByInstructorId(@Param("instructorId") String instructorId, Pageable pageable);

    /**
     * Get all payments without pagination for instructor
     * Orders by creation date descending (most recent first)
     * Includes user and course information
     */
    @Query("SELECT r FROM Refund r " +
            "LEFT JOIN FETCH r.payment p " +
            "LEFT JOIN FETCH p.user u " +
            "LEFT JOIN FETCH p.course c " +
            "WHERE c.instructor.id = :instructorId " +
            "ORDER BY r.requestedAt DESC")
    List<Refund> findAllRefundsByInstructorId(@Param("instructorId") String instructorId);

    /**
     * Get refund by ID with full details including user and course for instructor
     * Returns refund with all related information
     */
    @Query("SELECT r FROM Refund r " +
            "LEFT JOIN FETCH r.payment p " +
            "LEFT JOIN FETCH p.user u " +
            "LEFT JOIN FETCH p.course c " +
            "WHERE r.id = :refundId AND c.instructor.id = :instructorId")
    Optional<Refund> findRefundByIdAndInstructorIdWithDetails(@Param("refundId") String refundId,
            @Param("instructorId") String instructorId);
}
