package project.ktc.springboot_app.payment.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.entity.Payment;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.course.id = :courseId AND p.status = 'COMPLETED' ORDER BY p.paidAt DESC")
    Optional<Payment> findCompletedPaymentByUserAndCourse(@Param("userId") String userId,
            @Param("courseId") String courseId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.user.id = :userId AND p.course.id = :courseId AND p.status = 'COMPLETED'")
    BigDecimal getTotalPaidAmountByUserAndCourse(@Param("userId") String userId, @Param("courseId") String courseId);

    /**
     * Find payment by Stripe session ID
     */
    Optional<Payment> findBySessionId(String sessionId);
}
