package project.ktc.springboot_app.earning.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.earning.entity.InstructorEarning;

@Repository
public interface InstructorEarningRepository extends JpaRepository<InstructorEarning, String> {

	@Query("SELECT ie FROM InstructorEarning ie "
			+ "JOIN FETCH ie.course c "
			+ "JOIN FETCH ie.payment p "
			+ "WHERE ie.instructor.id = :instructorId "
			+ "AND (:courseId IS NULL OR ie.course.id = :courseId) "
			+ "AND (:dateFrom IS NULL OR ie.paidAt >= :dateFrom) "
			+ "AND (:dateTo IS NULL OR ie.paidAt <= :dateTo)")
	Page<InstructorEarning> findEarningsWithFilters(
			@Param("instructorId") String instructorId,
			@Param("courseId") String courseId,
			@Param("dateFrom") LocalDateTime dateFrom,
			@Param("dateTo") LocalDateTime dateTo,
			Pageable pageable);

	@Query("SELECT COALESCE(SUM(ie.amount), 0) FROM InstructorEarning ie "
			+ "WHERE ie.instructor.id = :instructorId "
			+ "AND (:courseId IS NULL OR ie.course.id = :courseId) "
			+ "AND (:dateFrom IS NULL OR ie.createdAt >= :dateFrom) "
			+ "AND (:dateTo IS NULL OR ie.createdAt <= :dateTo)")
	BigDecimal getTotalEarningsWithFilters(
			@Param("instructorId") String instructorId,
			@Param("courseId") String courseId,
			@Param("dateFrom") LocalDateTime dateFrom,
			@Param("dateTo") LocalDateTime dateTo);

	@Query("SELECT COUNT(ie) FROM InstructorEarning ie "
			+ "WHERE ie.instructor.id = :instructorId "
			+ "AND (:courseId IS NULL OR ie.course.id = :courseId) "
			+ "AND (:dateFrom IS NULL OR ie.createdAt >= :dateFrom) "
			+ "AND (:dateTo IS NULL OR ie.createdAt <= :dateTo)")
	Long getTotalTransactionsWithFilters(
			@Param("instructorId") String instructorId,
			@Param("courseId") String courseId,
			@Param("dateFrom") LocalDateTime dateFrom,
			@Param("dateTo") LocalDateTime dateTo);

	@Query("SELECT COALESCE(SUM(ie.amount), 0) FROM InstructorEarning ie "
			+ "WHERE ie.instructor.id = :instructorId AND ie.status = 'PENDING'")
	BigDecimal getPendingAmountByInstructor(@Param("instructorId") String instructorId);

	@Query("SELECT COALESCE(SUM(ie.amount), 0) FROM InstructorEarning ie "
			+ "WHERE ie.instructor.id = :instructorId AND ie.status = 'AVAILABLE'")
	BigDecimal getAvailableAmountByInstructor(@Param("instructorId") String instructorId);

	@Query("SELECT COALESCE(SUM(ie.amount), 0) FROM InstructorEarning ie "
			+ "WHERE ie.instructor.id = :instructorId AND ie.status = 'PAID'")
	BigDecimal getPaidAmountByInstructor(@Param("instructorId") String instructorId);

	@Query("SELECT COALESCE(SUM(ie.amount), 0) FROM InstructorEarning ie "
			+ "WHERE ie.instructor.id = :instructorId")
	BigDecimal getTotalEarningsByInstructor(@Param("instructorId") String instructorId);

	@Query("SELECT COALESCE(SUM(ie.amount), 0) as revenue FROM InstructorEarning ie "
			+ "WHERE ie.instructor.id = :instructorId AND YEAR(ie.createdAt) = :year AND MONTH(ie.createdAt) = :month")
	BigDecimal getTotalEarningsByMonth(
			@Param("instructorId") String instructorId,
			@Param("year") int year,
			@Param("month") int month);

	@Query("SELECT COUNT(ie) FROM InstructorEarning ie WHERE ie.instructor.id = :instructorId")
	Long getTotalTransactionsByInstructor(@Param("instructorId") String instructorId);

	@Query("SELECT ie FROM InstructorEarning ie "
			+ "JOIN FETCH ie.course c "
			+ "JOIN FETCH ie.payment p "
			+ "WHERE ie.id = :earningId AND ie.instructor.id = :instructorId")
	java.util.Optional<InstructorEarning> findByIdAndInstructorId(
			@Param("earningId") String earningId, @Param("instructorId") String instructorId);

	@Query("SELECT ie FROM InstructorEarning ie WHERE ie.payment.id = :paymentId")
	java.util.Optional<InstructorEarning> findByPaymentId(@Param("paymentId") String paymentId);

	/**
	 * Find instructor earnings created between two dates Used for scheduled reports
	 * and analysis
	 */
	@Query("SELECT ie FROM InstructorEarning ie WHERE ie.createdAt BETWEEN :startDate AND :endDate ORDER BY ie.createdAt DESC")
	java.util.List<InstructorEarning> findByCreatedAtBetween(
			@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
