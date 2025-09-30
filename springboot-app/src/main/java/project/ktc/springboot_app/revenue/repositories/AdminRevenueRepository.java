package project.ktc.springboot_app.revenue.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.payment.entity.Payment;

@Repository
public interface AdminRevenueRepository extends JpaRepository<Payment, String> {

  /** Get seasonal daily revenue data for heatmap */
  @Query(
      "SELECT DATE(p.paidAt) as date, "
          + "COALESCE(SUM(p.amount * 0.3), 0.0) as revenue, "
          + "COUNT(p) as transactions "
          + "FROM Payment p "
          + "WHERE p.status = 'COMPLETED' "
          + "AND YEAR(p.paidAt) = :year "
          + "GROUP BY DATE(p.paidAt) "
          + "ORDER BY DATE(p.paidAt)")
  List<Object[]> getSeasonalDailyRevenue(@Param("year") Integer year);

  /** Get revenue growth comparison between two periods */
  @Query(
      "SELECT "
          + "COALESCE(SUM(CASE WHEN YEAR(p.paidAt) = :currentYear THEN p.amount * 0.3 END), 0.0) as currentRevenue, "
          + "COALESCE(SUM(CASE WHEN YEAR(p.paidAt) = :previousYear THEN p.amount * 0.3 END), 0.0) as previousRevenue "
          + "FROM Payment p "
          + "WHERE p.status = 'COMPLETED' "
          + "AND YEAR(p.paidAt) IN (:currentYear, :previousYear)")
  List<Object[]> getYearlyRevenueComparison(
      @Param("currentYear") Integer currentYear, @Param("previousYear") Integer previousYear);

  /** Get best and worst performing months for a year */
  @Query(
      "SELECT MONTH(p.paidAt) as month, "
          + "COALESCE(SUM(p.amount * 0.3), 0.0) as revenue "
          + "FROM Payment p "
          + "WHERE p.status = 'COMPLETED' "
          + "AND YEAR(p.paidAt) = :year "
          + "GROUP BY MONTH(p.paidAt) "
          + "ORDER BY revenue")
  List<Object[]> getMonthlyPerformanceRanking(@Param("year") Integer year);

  /** Get average revenue per user */
  @Query(
      "SELECT "
          + "COALESCE(SUM(p.amount * 0.3), 0.0) / COUNT(DISTINCT p.user.id) "
          + "FROM Payment p "
          + "WHERE p.status = 'COMPLETED'")
  Double getAverageRevenuePerUser();

  /** Get monthly growth rate for a specific year compared to previous year */
  @Query(
      "SELECT "
          + "MONTH(p.paidAt) as month, "
          + "COALESCE(SUM(CASE WHEN YEAR(p.paidAt) = :currentYear THEN p.amount * 0.3 END), 0.0) as currentRevenue, "
          + "COALESCE(SUM(CASE WHEN YEAR(p.paidAt) = :previousYear THEN p.amount * 0.3 END), 0.0) as previousRevenue "
          + "FROM Payment p "
          + "WHERE p.status = 'COMPLETED' "
          + "AND YEAR(p.paidAt) IN (:currentYear, :previousYear) "
          + "GROUP BY MONTH(p.paidAt) "
          + "ORDER BY MONTH(p.paidAt)")
  List<Object[]> getMonthlyGrowthComparison(
      @Param("currentYear") Integer currentYear, @Param("previousYear") Integer previousYear);

  /** Get total transactions count */
  @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED'")
  Long getTotalTransactions();

  /** Get yearly revenue totals */
  @Query(
      "SELECT YEAR(p.paidAt) as year, "
          + "COALESCE(SUM(p.amount * 0.3), 0.0) as revenue "
          + "FROM Payment p "
          + "WHERE p.status = 'COMPLETED' "
          + "GROUP BY YEAR(p.paidAt) "
          + "ORDER BY YEAR(p.paidAt) DESC")
  List<Object[]> getYearlyRevenueTotals();

  /** Get course category revenue with additional metrics for performance analysis */
  @Query(
      "SELECT cat.name, "
          + "COALESCE(SUM(p.amount * 0.3), 0.0) as revenue, "
          + "COUNT(DISTINCT p.user.id) as studentsCount, "
          + "COUNT(DISTINCT p.course.id) as coursesCount, "
          + "COUNT(p) as totalTransactions, "
          + "AVG(r.rating) as averageRating "
          + "FROM Payment p "
          + "LEFT JOIN p.course c "
          + "LEFT JOIN c.categories cat "
          + "LEFT JOIN Review r ON r.course.id = c.id "
          + "WHERE p.status = 'COMPLETED' "
          + "GROUP BY cat.id, cat.name "
          + "ORDER BY revenue DESC")
  List<Object[]> getDetailedCategoryMetrics();

  /** Get total revenue (30% of completed payments) for all time */
  @Query("SELECT COALESCE(SUM(p.amount * 0.3), 0.0) FROM Payment p WHERE p.status = 'COMPLETED'")
  Double getTotalRevenue();

  /** Get monthly revenue data for a specific year */
  @Query(
      "SELECT YEAR(p.paidAt) as year, MONTH(p.paidAt) as month, "
          + "COALESCE(SUM(p.amount * 0.3), 0.0) as revenue, "
          + "COUNT(p) as transactions "
          + "FROM Payment p "
          + "WHERE p.status = 'COMPLETED' "
          + "AND YEAR(p.paidAt) = :year "
          + "GROUP BY YEAR(p.paidAt), MONTH(p.paidAt) "
          + "ORDER BY MONTH(p.paidAt)")
  List<Object[]> getMonthlyRevenueForYear(@Param("year") Integer year);

  /** Get monthly revenue data for a specific year */
  @Query(
      "SELECT YEAR(p.paidAt) as year, MONTH(p.paidAt) as month, "
          + "COALESCE(SUM(p.amount * 0.3), 0.0) as revenue, "
          + "COUNT(p) as transactions "
          + "FROM Payment p "
          + "WHERE p.status = 'COMPLETED' "
          + "AND YEAR(p.paidAt) = :year AND MONTH(p.paidAt) = :month "
          + "GROUP BY YEAR(p.paidAt), MONTH(p.paidAt) "
          + "ORDER BY MONTH(p.paidAt)")
  List<Object[]> getRecentRevenueForYear(
      @Param("year") Integer year, @Param("month") Integer month);

  /** Get daily revenue data for a specific month and year */
  @Query(
      "SELECT DATE(p.paidAt) as date, "
          + "COALESCE(SUM(p.amount * 0.3), 0.0) as revenue, "
          + "COUNT(p) as transactions "
          + "FROM Payment p "
          + "WHERE p.status = 'COMPLETED' "
          + "AND YEAR(p.paidAt) = :year "
          + "AND MONTH(p.paidAt) = :month "
          + "GROUP BY DATE(p.paidAt) "
          + "ORDER BY DATE(p.paidAt)")
  List<Object[]> getDailyRevenueForMonth(
      @Param("year") Integer year, @Param("month") Integer month);

  /** Get revenue by course category */
  @Query(
      "SELECT cat.name, "
          + "COALESCE(SUM(p.amount * 0.3), 0.0) as revenue, "
          + "COUNT(DISTINCT p.user.id) as studentsCount, "
          + "COUNT(DISTINCT p.course.id) as coursesCount "
          + "FROM Payment p "
          + "LEFT JOIN p.course c "
          + "LEFT JOIN c.categories cat "
          + "WHERE p.status = 'COMPLETED' "
          + "GROUP BY cat.id, cat.name "
          + "ORDER BY revenue DESC")
  List<Object[]> getRevenueByCategory();

  /** Get top spending students */
  @Query(
      "SELECT p.user.id, p.user.name, p.user.email, p.user.thumbnailUrl, "
          + "COALESCE(SUM(p.amount), 0.0) as totalSpent, "
          + "COUNT(DISTINCT p.course.id) as coursesEnrolled "
          + "FROM Payment p "
          + "WHERE p.status = 'COMPLETED' "
          + "GROUP BY p.user.id, p.user.name, p.user.email, p.user.thumbnailUrl "
          + "ORDER BY totalSpent DESC")
  List<Object[]> getTopSpendingStudents();

  /** Get instructor performance metrics */
  @Query(
      "SELECT c.instructor.id, c.instructor.name, c.instructor.email, "
          + "COUNT(DISTINCT c.id) as coursesCount, "
          + "COALESCE(SUM(p.amount * 0.3), 0.0) as totalRevenue, "
          + "COUNT(DISTINCT p.user.id) as totalStudents "
          + "FROM Payment p "
          + "LEFT JOIN p.course c "
          + "WHERE p.status = 'COMPLETED' "
          + "GROUP BY c.instructor.id, c.instructor.name, c.instructor.email "
          + "ORDER BY totalRevenue DESC")
  List<Object[]> getInstructorPerformanceMetrics();

  /** Get all distinct years with completed payments */
  @Query(
      "SELECT DISTINCT YEAR(p.paidAt) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paidAt IS NOT NULL ORDER BY YEAR(p.paidAt) DESC")
  List<Integer> getDistinctPaymentYears();

  /** Get total count of active users (users with at least one completed payment) */
  @Query("SELECT COUNT(DISTINCT p.user.id) FROM Payment p WHERE p.status = 'COMPLETED'")
  Long getTotalActiveUsers();

  /** Get quarterly revenue data for a specific year */
  @Query(
      "SELECT QUARTER(p.paidAt) as quarter, "
          + "COALESCE(SUM(p.amount * 0.3), 0.0) as revenue, "
          + "COUNT(p) as transactions "
          + "FROM Payment p "
          + "WHERE p.status = 'COMPLETED' "
          + "AND YEAR(p.paidAt) = :year "
          + "GROUP BY QUARTER(p.paidAt) "
          + "ORDER BY QUARTER(p.paidAt)")
  List<Object[]> getQuarterlyRevenueForYear(@Param("year") Integer year);
}
