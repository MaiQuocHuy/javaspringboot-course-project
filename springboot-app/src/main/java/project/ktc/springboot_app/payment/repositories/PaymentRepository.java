package project.ktc.springboot_app.payment.repositories;

import java.math.BigDecimal;
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
public interface PaymentRepository extends JpaRepository<Payment, String> {

  @Query(
      "SELECT p FROM Payment p WHERE p.user.id = :userId AND p.course.id = :courseId AND p.status = 'COMPLETED' ORDER BY p.paidAt DESC")
  Optional<Payment> findCompletedPaymentByUserAndCourse(
      @Param("userId") String userId, @Param("courseId") String courseId);

  @Query(
      "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.user.id = :userId AND p.course.id = :courseId AND p.status = 'COMPLETED'")
  BigDecimal getTotalPaidAmountByUserAndCourse(
      @Param("userId") String userId, @Param("courseId") String courseId);

  /** Find payment by Stripe session ID */
  Optional<Payment> findBySessionId(String sessionId);

  /**
   * Find all payments for a specific user with pagination support Orders by creation date
   * descending (most recent first)
   */
  @Query(
      "SELECT p FROM Payment p "
          + "LEFT JOIN FETCH p.course c "
          + "WHERE p.user.id = :userId "
          + "ORDER BY p.createdAt DESC")
  Page<Payment> findPaymentsByUserId(@Param("userId") String userId, Pageable pageable);

  /**
   * Find all payments for a specific user without pagination Orders by creation date descending
   * (most recent first)
   */
  @Query(
      "SELECT p FROM Payment p "
          + "LEFT JOIN FETCH p.course c "
          + "WHERE p.user.id = :userId "
          + "ORDER BY p.createdAt DESC")
  List<Payment> findPaymentsByUserId(@Param("userId") String userId);

  /**
   * Find a specific payment by ID and user ID Ensures that the payment belongs to the specified
   * user
   */
  @Query(
      "SELECT p FROM Payment p "
          + "LEFT JOIN FETCH p.course c "
          + "WHERE p.id = :paymentId AND p.user.id = :userId")
  Optional<Payment> findByIdAndUserId(
      @Param("paymentId") String paymentId, @Param("userId") String userId);

  /**
   * Find a specific payment by course ID and user ID Returns the most recent payment for the
   * course-user combination Uses LIMIT 1 to handle cases with multiple payment attempts
   */
  @Query(
      value =
          "SELECT * FROM payments p "
              + "WHERE p.course_id = :courseId AND p.user_id = :userId "
              + "ORDER BY p.created_at DESC "
              + "LIMIT 1",
      nativeQuery = true)
  Optional<Payment> findByCourseIdAndUserId(
      @Param("courseId") String courseId, @Param("userId") String userId);

  /**
   * Find the most recent COMPLETED payment by course ID and user ID This is used specifically to
   * check if a successful payment already exists
   */
  @Query(
      "SELECT p FROM Payment p "
          + "LEFT JOIN FETCH p.course c "
          + "WHERE p.course.id = :courseId AND p.user.id = :userId "
          + "AND p.status = 'COMPLETED' "
          + "ORDER BY p.createdAt DESC")
  Optional<Payment> findCompletedPaymentByCourseIdAndUserId(
      @Param("courseId") String courseId, @Param("userId") String userId);

  /** Count total payments made by a specific user */
  @Query("SELECT COUNT(p) FROM Payment p WHERE p.user.id = :userId")
  Long countTotalPaymentsByUserId(@Param("userId") String userId);

  /** Calculate total amount spent by a specific user (only completed payments) */
  @Query(
      "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.user.id = :userId AND p.status = 'COMPLETED'")
  BigDecimal calculateTotalAmountSpentByUserId(@Param("userId") String userId);

  /** Count completed payments by a specific user */
  @Query("SELECT COUNT(p) FROM Payment p WHERE p.user.id = :userId AND p.status = 'COMPLETED'")
  Long countCompletedPaymentsByUserId(@Param("userId") String userId);

  /** Count failed payments by a specific user */
  @Query("SELECT COUNT(p) FROM Payment p WHERE p.user.id = :userId AND p.status = 'FAILED'")
  Long countFailedPaymentsByUserId(@Param("userId") String userId);
}
