package project.ktc.springboot_app.revenue.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.ktc.springboot_app.payment.dto.SampleDataGenerationDTO;
import project.ktc.springboot_app.payment.interfaces.PaymentDataGenerationService;
import project.ktc.springboot_app.revenue.dto.*;
import project.ktc.springboot_app.revenue.interfaces.AdminRevenueService;

@RestController
@Tag(
    name = "Admin Revenue Management",
    description = "APIs for managing and analyzing revenue data - Admin only")
@RequestMapping("/api/admin/revenues")
@Validated
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRevenueController {

  private final AdminRevenueService adminRevenueService;
  private final PaymentDataGenerationService paymentDataGenerationService;

  @GetMapping("/statistics")
  @Operation(
      summary = "Get revenue statistics overview",
      description =
          "Retrieves key revenue metrics including total revenue, growth rates, and user statistics. Revenue is calculated as 30% of completed payments.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<StatisticsDTO>>
      getStatistics() {
    return adminRevenueService.getStatistics();
  }

  @GetMapping("/monthly")
  @Operation(
      summary = "Get monthly revenue data",
      description =
          "Retrieves detailed monthly revenue breakdown for a specific year. Includes monthly totals, growth rates, and daily breakdowns.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved monthly revenue data"),
        @ApiResponse(responseCode = "400", description = "Invalid year parameter"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<MonthlyRevenueDTO>>
      getMonthlyRevenue(
          @Parameter(description = "Year for revenue data (e.g., 2025)", example = "2025")
              @RequestParam(required = false, defaultValue = "2025")
              Integer year) {
    return adminRevenueService.getMonthlyRevenue(year);
  }

  @GetMapping("/recent")
  @Operation(
      summary = "Get recent revenue data",
      description =
          "Retrieves detailed the most recent 3 months revenue breakdown for a specific year. Includes monthly totals, growth rates, and daily breakdowns.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved recent revenue data"),
        @ApiResponse(responseCode = "400", description = "Invalid year parameter"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<
          project.ktc.springboot_app.common.dto.ApiResponse<MonthlyRevenueDTO.RecentMonthlyData>>
      getRecentRevenue() {
    return adminRevenueService.getRecentRevenues();
  }

  @GetMapping("/daily")
  @Operation(
      summary = "Get daily revenue data",
      description =
          "Retrieves detailed daily revenue breakdown for a specific month and year. Provides granular day-by-day revenue analysis.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved daily revenue data"),
        @ApiResponse(responseCode = "400", description = "Invalid year or month parameter"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<
          project.ktc.springboot_app.common.dto.ApiResponse<List<MonthlyRevenueDTO.DailyData>>>
      getDailysRevenue(
          @Parameter(description = "Year for revenue data", example = "2025")
              @RequestParam(required = false, defaultValue = "2025")
              Integer year,
          @Parameter(description = "Month (1-12)", example = "6") @RequestParam @Min(1) @Max(12)
              Integer month) {
    return adminRevenueService.getDailysRevenue(year, month);
  }

  @GetMapping("/top-spenders")
  @Operation(
      summary = "Get top spending users",
      description =
          "Retrieves list of users with highest spending amounts. Useful for identifying VIP customers and revenue contributors.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved top spenders"),
        @ApiResponse(responseCode = "400", description = "Invalid limit parameter"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<TopSpendersDTO>>
      getTopSpenders(
          @Parameter(description = "Number of top spenders to return", example = "5")
              @RequestParam(required = false, defaultValue = "5")
              @Min(1)
              @Max(100)
              Integer limit) {
    return adminRevenueService.getTopSpenders(limit);
  }

  @GetMapping("/performance")
  @Operation(
      summary = "Get performance metrics",
      description =
          "Retrieves comprehensive performance analytics including conversion rates, average transaction values, and key performance indicators.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved performance metrics"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PerformanceMetricsDTO>>
      getPerformanceMetrics() {
    return adminRevenueService.getPerformanceMetrics();
  }

  @GetMapping("/comparative")
  @Operation(
      summary = "Get comparative analysis",
      description =
          "Provides comparative revenue analysis across different time periods. Supports monthly, quarterly, and yearly comparisons with growth insights.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved comparative analysis"),
        @ApiResponse(responseCode = "400", description = "Invalid period or year parameter"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<ComparativeAnalysisDTO>>
      getComparativeAnalysis(
          @Parameter(
                  description = "Type of comparison: monthly, quarterly, or yearly",
                  example = "monthly")
              @RequestParam(defaultValue = "monthly")
              @Pattern(
                  regexp = "monthly|quarterly|yearly",
                  message = "Period must be monthly, quarterly, or yearly")
              String period,
          @Parameter(description = "Base year for comparison", example = "2025")
              @RequestParam(required = false, defaultValue = "2025")
              Integer year) {
    return adminRevenueService.getComparativeAnalysis(period, year);
  }

  @GetMapping("/seasonal")
  @Operation(
      summary = "Get seasonal heatmap data",
      description =
          "Provides seasonal revenue patterns and heatmap visualization data. Shows revenue distribution across seasons and months for trend analysis.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved seasonal heatmap data"),
        @ApiResponse(responseCode = "400", description = "Invalid year parameter"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<SeasonalHeatmapDTO>>
      getSeasonalHeatmap(
          @Parameter(description = "Year for seasonal analysis", example = "2025")
              @RequestParam(required = false, defaultValue = "2025")
              Integer year) {
    return adminRevenueService.getSeasonalHeatmap(year);
  }

  @GetMapping("/available-years")
  @Operation(
      summary = "Get available years with revenue data",
      description =
          "Returns list of years that have revenue data available in the system. Useful for populating year selection dropdowns in the frontend.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved available years"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<Integer>>>
      getAvailableYears() {
    return adminRevenueService.getAvailableYears();
  }

  @GetMapping("/summary")
  @Operation(
      summary = "Get comprehensive revenue summary",
      description =
          "Provides a comprehensive revenue summary combining multiple metrics and insights. Acts as a dashboard overview with key performance indicators and trends.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved revenue summary"),
        @ApiResponse(responseCode = "400", description = "Invalid year parameter"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Object>>
      getRevenueSummary(
          @Parameter(description = "Year for summary data", example = "2025")
              @RequestParam(required = false, defaultValue = "2025")
              Integer year) {
    return adminRevenueService.getRevenueSummary(year);
  }

  @GetMapping("/generate-sample-payment-data")
  @Operation(
      summary = "Generate sample payment data",
      description =
          "Creates sample payment data for analytics testing and development. Generates realistic payment patterns for current year and two previous years including seasonal variations, discount periods, and special events like Black Friday and school seasons.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sample payment data generation completed"),
        @ApiResponse(
            responseCode = "400",
            description = "Data generation failed or data already sufficient"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during data generation")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<SampleDataGenerationDTO>>
      generateSamplePaymentData() {
    SampleDataGenerationDTO response = paymentDataGenerationService.generateSamplePaymentData();

    if (response.isSuccess()) {
      return ResponseEntity.ok(
          project.ktc.springboot_app.common.dto.ApiResponse.success(
              response, "Sample payment data generated successfully"));
    } else {
      return ResponseEntity.internalServerError()
          .body(
              project.ktc.springboot_app.common.dto.ApiResponse.error(500, response.getMessage()));
    }
  }
}
