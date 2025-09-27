package project.ktc.springboot_app.payment.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.payment.entity.Payment;

@Repository
public interface AdminPaymentRepository extends JpaRepository<Payment, String> {

        /**
         * Get all payments with pagination support for admin
         * Orders by creation date descending (most recent first)
         * Includes user and course information
         */
        @Query("SELECT p FROM Payment p " +
                        "LEFT JOIN FETCH p.user u " +
                        "LEFT JOIN FETCH p.course c " +
                        "ORDER BY p.createdAt DESC")
        Page<Payment> findAllPayments(Pageable pageable);

        /**
         * Get all payments without pagination for admin
         * Orders by creation date descending (most recent first)
         * Includes user and course information
         */
        @Query("SELECT p FROM Payment p " +
                        "LEFT JOIN FETCH p.user u " +
                        "LEFT JOIN FETCH p.course c " +
                        "ORDER BY p.createdAt DESC")
        List<Payment> findAllPayments();

        /**
         * Get payment by ID with full details including user and course for admin
         * Returns payment with all related information
         */
        @Query("SELECT p FROM Payment p " +
                        "LEFT JOIN FETCH p.user u " +
                        "LEFT JOIN FETCH p.course c " +
                        "LEFT JOIN FETCH c.instructor " +
                        "WHERE p.id = :paymentId")
        Optional<Payment> findPaymentByIdWithDetails(@Param("paymentId") String paymentId);

        /**
         * Get payments filtered by status with pagination
         * Useful for admin to filter payments by their status
         */
        @Query("SELECT p FROM Payment p " +
                        "LEFT JOIN FETCH p.user u " +
                        "LEFT JOIN FETCH p.course c " +
                        "WHERE p.status = :status " +
                        "ORDER BY p.createdAt DESC")
        Page<Payment> findPaymentsByStatus(@Param("status") Payment.PaymentStatus status, Pageable pageable);

        /**
         * Get payments for a specific user (admin view)
         * Includes all payment details for administrative purposes
         */
        @Query("SELECT p FROM Payment p " +
                        "LEFT JOIN FETCH p.user u " +
                        "LEFT JOIN FETCH p.course c " +
                        "WHERE p.user.id = :userId " +
                        "ORDER BY p.createdAt DESC")
        Page<Payment> findPaymentsByUserIdForAdmin(@Param("userId") String userId, Pageable pageable);

        /**
         * Get payments for a specific course (admin view)
         * Useful for admin to see all payments for a particular course
         */
        @Query("SELECT p FROM Payment p " +
                        "LEFT JOIN FETCH p.user u " +
                        "LEFT JOIN FETCH p.course c " +
                        "WHERE p.course.id = :courseId " +
                        "ORDER BY p.createdAt DESC")
        Page<Payment> findPaymentsByCourseIdForAdmin(@Param("courseId") String courseId, Pageable pageable);

        /**
         * Search payments by user email or course title (admin functionality)
         * Allows admin to search for payments using partial matches
         */
        @Query("SELECT p FROM Payment p " +
                        "LEFT JOIN FETCH p.user u " +
                        "LEFT JOIN FETCH p.course c " +
                        "WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "OR LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "ORDER BY p.createdAt DESC")
        Page<Payment> searchPayments(@Param("searchTerm") String searchTerm, Pageable pageable);

        /**
         * Count all payments
         */
        @Query("SELECT COUNT(p) FROM Payment p")
        Long countAllPayments();

        /**
         * Count payments by status
         */
        @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
        Long countPaymentsByStatus(@Param("status") Payment.PaymentStatus status);

        /**
         * Count completed payments
         */
        @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED'")
        Long countCompletedPayments();

        /**
         * Count pending payments
         */
        @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'PENDING'")
        Long countPendingPayments();

        /**
         * Count failed payments
         */
        @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'FAILED'")
        Long countFailedPayments();

        /**
         * Get all payments with advanced search and filtering for admin
         * Supports search by payment ID, user name, or course title
         * Supports filtering by status and date range
         */
        @Query("SELECT p FROM Payment p " +
                        "LEFT JOIN FETCH p.user u " +
                        "LEFT JOIN FETCH p.course c " +
                        "WHERE (:search IS NULL OR :search = '' OR " +
                        "       LOWER(p.id) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "       LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "       LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:status IS NULL OR p.status = :status) " +
                        "AND (:paymentMethod IS NULL OR :paymentMethod = '' OR " +
                        "     LOWER(p.paymentMethod) LIKE LOWER(CONCAT('%', :paymentMethod, '%'))) " +
                        "AND (:fromDate IS NULL OR DATE(p.createdAt) >= :fromDate) " +
                        "AND (:toDate IS NULL OR DATE(p.createdAt) <= :toDate) " +
                        "ORDER BY p.createdAt DESC")
        Page<Payment> findAllPaymentsWithFilter(
                        @Param("search") String search,
                        @Param("status") project.ktc.springboot_app.payment.entity.Payment.PaymentStatus status,
                        @Param("paymentMethod") String paymentMethod,
                        @Param("fromDate") java.time.LocalDate fromDate,
                        @Param("toDate") java.time.LocalDate toDate,
                        Pageable pageable);

        /**
         * Get all payments with advanced search and filtering for admin (no pagination)
         * Supports search by payment ID, user name, or course title
         * Supports filtering by status and date range
         */
        @Query("SELECT p FROM Payment p " +
                        "LEFT JOIN FETCH p.user u " +
                        "LEFT JOIN FETCH p.course c " +
                        "WHERE (:search IS NULL OR :search = '' OR " +
                        "       LOWER(p.id) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "       LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "       LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:status IS NULL OR p.status = :status) " +
                        "AND (:paymentMethod IS NULL OR :paymentMethod = '' OR " +
                        "     LOWER(p.paymentMethod) LIKE LOWER(CONCAT('%', :paymentMethod, '%'))) " +
                        "AND (:fromDate IS NULL OR DATE(p.createdAt) >= :fromDate) " +
                        "AND (:toDate IS NULL OR DATE(p.createdAt) <= :toDate) " +
                        "ORDER BY p.createdAt DESC")
        List<Payment> findAllPaymentsWithFilter(
                        @Param("search") String search,
                        @Param("status") project.ktc.springboot_app.payment.entity.Payment.PaymentStatus status,
                        @Param("paymentMethod") String paymentMethod,
                        @Param("fromDate") java.time.LocalDate fromDate,
                        @Param("toDate") java.time.LocalDate toDate);

}
