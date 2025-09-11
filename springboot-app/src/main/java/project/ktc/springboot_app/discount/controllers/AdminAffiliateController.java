package project.ktc.springboot_app.discount.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.discount.dto.request.CancelPayoutRequestDto;
import project.ktc.springboot_app.discount.dto.response.AffiliatePayoutDetailResponseDto;
import project.ktc.springboot_app.discount.dto.response.AffiliatePayoutResponseDto;
import project.ktc.springboot_app.discount.dto.response.AffiliateStatisticsResponseDto;
import project.ktc.springboot_app.discount.enums.PayoutStatus;
import project.ktc.springboot_app.discount.interfaces.AdminAffiliatePayoutService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/admin/affiliate")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Affiliate Controller", description = "APIs for managing affiliate payouts")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminAffiliateController {

        private final AdminAffiliatePayoutService adminAffiliatePayoutService;

        @GetMapping("/payouts")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get affiliate payouts", description = """
                        Retrieve affiliate payouts with filtering and pagination options.

                        **Filtering Options:**
                        - **userId**: Filter by specific user ID
                        - **status**: Filter by payout status (PENDING, PAID, CANCELLED)
                        - **startDate/endDate**: Filter by date range
                        - **minAmount/maxAmount**: Filter by amount range

                        **Sorting Options:**
                        - Sort by: createdAt, amount, status, userId (default: createdAt)
                        - Direction: asc, desc (default: desc)

                        **Permissions Required:**
                        - ADMIN role
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payouts retrieved successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<ApiResponse<PaginatedResponse<AffiliatePayoutResponseDto>>> getAffiliatePayouts(
                        @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
                        @Parameter(description = "Filter by user ID") @RequestParam(required = false) Long userId,
                        @Parameter(description = "Filter by payout status") @RequestParam(required = false) PayoutStatus status,
                        @Parameter(description = "Filter by start date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @Parameter(description = "Filter by end date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @Parameter(description = "Filter by minimum amount") @RequestParam(required = false) BigDecimal minAmount,
                        @Parameter(description = "Filter by maximum amount") @RequestParam(required = false) BigDecimal maxAmount) {

                LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
                LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

                Page<AffiliatePayoutResponseDto> payouts = adminAffiliatePayoutService.getAffiliatePayouts(
                                pageable, userId, status, startDateTime, endDateTime, minAmount, maxAmount);

                PaginatedResponse<AffiliatePayoutResponseDto> response = PaginatedResponse.of(payouts);
                return ResponseEntity.ok(ApiResponse.success(response, "Affiliate payouts retrieved successfully"));
        }

        @GetMapping("/statistics")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get affiliate statistics", description = """
                        Retrieve comprehensive affiliate statistics including:
                        - Total commission amount
                        - Paid and pending amounts
                        - Number of affiliates
                        - Growth metrics

                        **Permissions Required:**
                        - ADMIN role
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<ApiResponse<AffiliateStatisticsResponseDto>> getAffiliateStatistics() {
                AffiliateStatisticsResponseDto statistics = adminAffiliatePayoutService.getAffiliateStatistics();
                return ResponseEntity
                                .ok(ApiResponse.success(statistics, "Affiliate statistics retrieved successfully"));
        }

        @GetMapping("/payouts/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get affiliate payout by ID", description = """
                        Retrieve detailed information about a specific affiliate payout.

                        **Permissions Required:**
                        - ADMIN role
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payout retrieved successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payout not found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<ApiResponse<AffiliatePayoutResponseDto>> getPayoutById(
                        @Parameter(description = "Payout ID", required = true) @PathVariable Long id) {
                AffiliatePayoutResponseDto payout = adminAffiliatePayoutService.getPayoutById(id);
                return ResponseEntity.ok(ApiResponse.success(payout, "Affiliate payout retrieved successfully"));
        }

        @GetMapping("/payouts/{id}/detail")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get detailed affiliate payout information", description = """
                        Retrieve comprehensive information about a specific affiliate payout including:
                        - Referrer details (who gets the commission)
                        - Course information with instructor details
                        - Discount code information
                        - Purchaser details (who used the discount)
                        - Usage information with pricing breakdown

                        **Permissions Required:**
                        - ADMIN role
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Detailed payout information retrieved successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payout not found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<ApiResponse<AffiliatePayoutDetailResponseDto>> getPayoutDetailById(
                        @Parameter(description = "Payout ID", required = true) @PathVariable String id) {
                AffiliatePayoutDetailResponseDto payoutDetail = adminAffiliatePayoutService.getPayoutDetailById(id);
                return ResponseEntity.ok(ApiResponse.success(payoutDetail,
                                "Detailed affiliate payout information retrieved successfully"));
        }

        @PatchMapping("/payouts/{id}/mark-paid")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Mark payout as paid", description = """
                        Mark a pending affiliate payout as paid.

                        **Business Rules:**
                        - Only PENDING payouts can be marked as paid
                        - Paid timestamp will be set to current time
                        - Action is logged for audit purposes

                        **Permissions Required:**
                        - ADMIN role
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payout marked as paid successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Payout cannot be marked as paid"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payout not found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<ApiResponse<AffiliatePayoutResponseDto>> markPayoutAsPaid(
                        @Parameter(description = "Payout ID", required = true) @PathVariable Long id) {
                AffiliatePayoutResponseDto payout = adminAffiliatePayoutService.markPayoutAsPaid(id);
                return ResponseEntity.ok(ApiResponse.success(payout, "Payout marked as paid successfully"));
        }

        @PatchMapping("/payouts/{id}/cancel")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Cancel payout", description = """
                        Cancel a pending affiliate payout with a reason.

                        **Business Rules:**
                        - Only PENDING payouts can be cancelled
                        - Cancellation reason is required
                        - Action is logged for audit purposes

                        **Permissions Required:**
                        - ADMIN role
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payout cancelled successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Invalid cancellation request"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payout not found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<ApiResponse<AffiliatePayoutResponseDto>> cancelPayout(
                        @Parameter(description = "Payout ID", required = true) @PathVariable Long id,
                        @RequestBody CancelPayoutRequestDto cancelRequest) {
                AffiliatePayoutResponseDto payout = adminAffiliatePayoutService.cancelPayout(id,
                                cancelRequest.getReason());
                return ResponseEntity.ok(ApiResponse.success(payout, "Payout cancelled successfully"));
        }

        @GetMapping("/payouts/export")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Export affiliate payouts", description = """
                        Export affiliate payouts to CSV format with filtering options.

                        **Export Features:**
                        - All filtering options supported
                        - CSV format with comprehensive data
                        - Suitable for accounting and reporting purposes

                        **Permissions Required:**
                        - ADMIN role
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Export generated successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<byte[]> exportPayouts(
                        @Parameter(description = "Filter by user ID") @RequestParam(required = false) Long userId,
                        @Parameter(description = "Filter by payout status") @RequestParam(required = false) PayoutStatus status,
                        @Parameter(description = "Filter by start date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @Parameter(description = "Filter by end date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @Parameter(description = "Filter by minimum amount") @RequestParam(required = false) BigDecimal minAmount,
                        @Parameter(description = "Filter by maximum amount") @RequestParam(required = false) BigDecimal maxAmount) {

                LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
                LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

                byte[] csvData = adminAffiliatePayoutService.exportPayouts(
                                userId, status, startDateTime, endDateTime, minAmount, maxAmount);

                return ResponseEntity.ok()
                                .header("Content-Type", "text/csv")
                                .header("Content-Disposition",
                                                "attachment; filename=\"affiliate_payouts_" + LocalDate.now()
                                                                + ".csv\"")
                                .body(csvData);
        }
}
