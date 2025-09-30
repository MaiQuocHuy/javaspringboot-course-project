package project.ktc.springboot_app.revenue.interfaces;

import java.util.List;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.revenue.dto.*;

public interface AdminRevenueService {

  /** Get statistics overview for revenue dashboard */
  ResponseEntity<ApiResponse<StatisticsDTO>> getStatistics();

  /** Get monthly revenue data for a specific year */
  ResponseEntity<ApiResponse<MonthlyRevenueDTO>> getMonthlyRevenue(Integer year);

  /** Get recent revenue (last 3 months) data for a specific year */
  ResponseEntity<ApiResponse<MonthlyRevenueDTO.RecentMonthlyData>> getRecentRevenues();

  /** Get daily revenue data for a specific month and year */
  ResponseEntity<ApiResponse<List<MonthlyRevenueDTO.DailyData>>> getDailysRevenue(
      Integer year, Integer month);

  /** Get top spending students */
  ResponseEntity<ApiResponse<TopSpendersDTO>> getTopSpenders(Integer limit);

  /** Get performance metrics including category revenue and instructor performance */
  ResponseEntity<ApiResponse<PerformanceMetricsDTO>> getPerformanceMetrics();

  /** Get comparative analysis data for different time periods */
  ResponseEntity<ApiResponse<ComparativeAnalysisDTO>> getComparativeAnalysis(
      String comparisonType, Integer year);

  /** Get seasonal heatmap data for revenue patterns */
  ResponseEntity<ApiResponse<SeasonalHeatmapDTO>> getSeasonalHeatmap(Integer year);

  /** Get all available years with payment data */
  ResponseEntity<ApiResponse<List<Integer>>> getAvailableYears();

  /** Get comprehensive revenue summary for dashboard overview */
  ResponseEntity<ApiResponse<Object>> getRevenueSummary(Integer year);
}
