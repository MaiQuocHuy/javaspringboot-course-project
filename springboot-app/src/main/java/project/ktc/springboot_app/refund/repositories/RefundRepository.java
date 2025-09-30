package project.ktc.springboot_app.refund.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.refund.entity.Refund;

@Repository
public interface RefundRepository extends JpaRepository<Refund, String> {

	/** Check if a refund already exists for a specific payment */
	@Query("SELECT r FROM Refund r WHERE r.payment.id = :paymentId")
	Optional<Refund> findByPaymentId(@Param("paymentId") String paymentId);

	/** Check if a refund exists for a specific user and course combination */
	@Query("SELECT r FROM Refund r WHERE r.payment.user.id = :userId AND r.payment.course.id = :courseId")
	Optional<Refund> findByUserIdAndCourseId(
			@Param("userId") String userId, @Param("courseId") String courseId);
}
