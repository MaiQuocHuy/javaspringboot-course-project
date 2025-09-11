package project.ktc.springboot_app.discount.controllers;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.discount.dto.StudentAffiliatePayoutResponseDto;
import project.ktc.springboot_app.discount.dto.StudentAffiliatePayoutStatisticsDto;
import project.ktc.springboot_app.discount.dto.StudentDiscountUsageResponseDto;
import project.ktc.springboot_app.discount.interfaces.StudentAffiliatePayoutService;
import project.ktc.springboot_app.discount.interfaces.StudentDiscountUsageService;

/**
 * REST Controller for student discount operations
 * Allows students to view their discount usage and affiliate payouts
 */
@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Student Discount API", description = "API for student discount and affiliate operations")
public class StudentDiscountController {

    private final StudentDiscountUsageService studentDiscountUsageService;
    private final StudentAffiliatePayoutService studentAffiliatePayoutService;

    /**
     * Get discount usages where the current student is the referrer
     * (People using the student's discount code)
     * 
     * @param pageable Pagination parameters
     * @return ResponseEntity containing paginated list of discount usages
     */
    @GetMapping("/discount-usage")
    @Operation(summary = "Get student discount usages", description = """
            Retrieves all discount usages where the current student is the referrer.
            This shows who has used the student's discount codes.

            **Features:**
            - Shows people who used the student's discount codes
            - Includes user, course, and usage details
            - Supports pagination for better performance
            - Ordered by usage date (most recent first)

            **Student Only:**
            - This endpoint requires STUDENT role
            - Students can only see their own discount usage data
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Discount usages retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Student role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<StudentDiscountUsageResponseDto>>> getDiscountUsages(
            @PageableDefault(size = 10) Pageable pageable) {

        log.info("Student requesting discount usages with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        return studentDiscountUsageService.getDiscountUsages(pageable);
    }

    /**
     * Get affiliate payouts for the student (commissions they receive)
     * 
     * @param pageable Pagination parameters
     * @return ResponseEntity containing paginated list of affiliate payouts
     */
    @GetMapping("/affiliate-payout")
    @Operation(summary = "Get student affiliate payouts", description = """
            Retrieves all affiliate payouts for the current student.
            This shows the commissions the student receives from referrals.

            **Features:**
            - Shows commission payouts from referrals
            - Includes course, commission amount, and payout status
            - Supports pagination for better performance
            - Ordered by creation date (most recent first)

            **Student Only:**
            - This endpoint requires STUDENT role
            - Students can only see their own affiliate payout data
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Affiliate payouts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Student role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<StudentAffiliatePayoutResponseDto>>> getAffiliatePayouts(
            @PageableDefault(size = 10) Pageable pageable) {

        log.info("Student requesting affiliate payouts with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        return studentAffiliatePayoutService.getAffiliatePayouts(pageable);
    }

    /**
     * Get affiliate payout statistics for the student
     * 
     * @return ResponseEntity containing affiliate payout statistics
     */
    @GetMapping("/affiliate-payout/statistics")
    @Operation(summary = "Get affiliate payout statistics", description = """
            Retrieves comprehensive statistics about the student's affiliate payouts.

            **Response Includes:**
            - Payout counts by status (total, pending, paid, cancelled)
            - Commission amounts by status
            - Overall earning summary

            **Use Cases:**
            - Student dashboard overview
            - Earnings tracking
            - Performance monitoring

            **Student Only:**
            - This endpoint requires STUDENT role
            - Students can only see their own statistics
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Student role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<StudentAffiliatePayoutStatisticsDto>> getAffiliatePayoutStatistics() {

        log.info("Student requesting affiliate payout statistics");

        return studentAffiliatePayoutService.getAffiliatePayoutStatistics();
    }
}
