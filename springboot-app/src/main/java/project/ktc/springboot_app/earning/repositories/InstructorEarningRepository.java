package project.ktc.springboot_app.earning.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.earning.dto.MonthlyEarningsDto;
import project.ktc.springboot_app.earning.entity.InstructorEarning;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface InstructorEarningRepository extends JpaRepository<InstructorEarning, String> {

        @Query("SELECT ie FROM InstructorEarning ie " +
                        "JOIN FETCH ie.course c " +
                        "JOIN FETCH ie.payment p " +
                        "WHERE ie.instructor.id = :instructorId " +
                        "AND (:courseId IS NULL OR ie.course.id = :courseId) " +
                        "AND (:dateFrom IS NULL OR ie.paidAt >= :dateFrom) " +
                        "AND (:dateTo IS NULL OR ie.paidAt <= :dateTo)")
        Page<InstructorEarning> findEarningsWithFilters(
                        @Param("instructorId") String instructorId,
                        @Param("courseId") String courseId,
                        @Param("dateFrom") LocalDateTime dateFrom,
                        @Param("dateTo") LocalDateTime dateTo,
                        Pageable pageable);

        @Query("SELECT COALESCE(SUM(ie.amount), 0) FROM InstructorEarning ie " +
                        "WHERE ie.instructor.id = :instructorId " +
                        "AND (:courseId IS NULL OR ie.course.id = :courseId) " +
                        "AND (:dateFrom IS NULL OR ie.createdAt >= :dateFrom) " +
                        "AND (:dateTo IS NULL OR ie.createdAt <= :dateTo)")
        BigDecimal getTotalEarningsWithFilters(
                        @Param("instructorId") String instructorId,
                        @Param("courseId") String courseId,
                        @Param("dateFrom") LocalDateTime dateFrom,
                        @Param("dateTo") LocalDateTime dateTo);

        @Query("SELECT COUNT(ie) FROM InstructorEarning ie " +
                        "WHERE ie.instructor.id = :instructorId " +
                        "AND (:courseId IS NULL OR ie.course.id = :courseId) " +
                        "AND (:dateFrom IS NULL OR ie.createdAt >= :dateFrom) " +
                        "AND (:dateTo IS NULL OR ie.createdAt <= :dateTo)")
        Long getTotalTransactionsWithFilters(
                        @Param("instructorId") String instructorId,
                        @Param("courseId") String courseId,
                        @Param("dateFrom") LocalDateTime dateFrom,
                        @Param("dateTo") LocalDateTime dateTo);

        @Query("SELECT COALESCE(SUM(ie.amount), 0) FROM InstructorEarning ie " +
                        "WHERE ie.instructor.id = :instructorId AND ie.status = 'PENDING'")
        BigDecimal getPendingAmountByInstructor(@Param("instructorId") String instructorId);

        @Query("SELECT COALESCE(SUM(ie.amount), 0) FROM InstructorEarning ie " +
                        "WHERE ie.instructor.id = :instructorId AND ie.status = 'AVAILABLE'")
        BigDecimal getAvailableAmountByInstructor(@Param("instructorId") String instructorId);

        @Query("SELECT COALESCE(SUM(ie.amount), 0) FROM InstructorEarning ie " +
                        "WHERE ie.instructor.id = :instructorId AND ie.status = 'PAID'")
        BigDecimal getPaidAmountByInstructor(@Param("instructorId") String instructorId);

        @Query("SELECT COALESCE(SUM(ie.amount), 0) FROM InstructorEarning ie " +
                        "WHERE ie.instructor.id = :instructorId")
        BigDecimal getTotalEarningsByInstructor(@Param("instructorId") String instructorId);

        @Query("SELECT COALESCE(SUM(ie.amount), 0) as revenue FROM InstructorEarning ie "
                        +
                        "WHERE ie.instructor.id = :instructorId AND YEAR(ie.paidAt) = :year AND MONTH(ie.paidAt) = :month")
        BigDecimal getTotalEarningsByMonth(@Param("instructorId") String instructorId, @Param("year") int year,
                        @Param("month") int month);

        @Query("SELECT COUNT(ie) FROM InstructorEarning ie WHERE ie.instructor.id = :instructorId")
        Long getTotalTransactionsByInstructor(@Param("instructorId") String instructorId);

        @Query("SELECT ie FROM InstructorEarning ie " +
                        "JOIN FETCH ie.course c " +
                        "JOIN FETCH ie.payment p " +
                        "WHERE ie.id = :earningId AND ie.instructor.id = :instructorId")
        java.util.Optional<InstructorEarning> findByIdAndInstructorId(
                        @Param("earningId") String earningId,
                        @Param("instructorId") String instructorId);

        @Query("SELECT ie FROM InstructorEarning ie WHERE ie.payment.id = :paymentId")
        java.util.Optional<InstructorEarning> findByPaymentId(@Param("paymentId") String paymentId);
}
