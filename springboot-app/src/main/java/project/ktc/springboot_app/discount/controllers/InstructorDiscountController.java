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
import project.ktc.springboot_app.discount.dto.InstructorAffiliatePayoutResponseDto;
import project.ktc.springboot_app.discount.dto.InstructorDiscountUsageResponseDto;
import project.ktc.springboot_app.discount.interfaces.InstructorAffiliatePayoutService;
import project.ktc.springboot_app.discount.interfaces.InstructorDiscountUsageService;

/**
 * REST Controller for instructor discount operations
 * Allows instructors to view discount usage and affiliate payouts for their
 * courses
 */
@RestController
@RequestMapping("/api/instructor")
@PreAuthorize("hasRole('INSTRUCTOR')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Instructor Discount API", description = "API for instructor discount and affiliate operations")
@SecurityRequirement(name = "bearerAuth")
public class InstructorDiscountController {

    private final InstructorDiscountUsageService instructorDiscountUsageService;
    private final InstructorAffiliatePayoutService instructorAffiliatePayoutService;

    /**
     * Get discount usages for courses owned by the instructor
     * 
     * @param pageable Pagination parameters
     * @return ResponseEntity containing paginated list of discount usages
     */
    @GetMapping("/discount-usage")
    @Operation(summary = "Get instructor discount usages", description = """
            Retrieves all discount usages for courses owned by the current instructor.
            This shows which discounts have been used on the instructor's courses.

            **Features:**
            - Shows discount usage on instructor's courses
            - Includes user details, course info, and discount details
            - Shows referral information when applicable
            - Supports pagination for better performance
            - Ordered by usage date (most recent first)

            **Instructor Only:**
            - This endpoint requires INSTRUCTOR role
            - Instructors can only see discount usage for their own courses
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Discount usages retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Instructor role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<InstructorDiscountUsageResponseDto>>> getDiscountUsages(
            @PageableDefault(size = 10) Pageable pageable) {

        log.info("Instructor requesting discount usages with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        return instructorDiscountUsageService.getDiscountUsages(pageable);
    }

    /**
     * Get affiliate payouts for courses owned by the instructor
     * (Shows who gets commission on instructor's courses)
     * 
     * @param pageable Pagination parameters
     * @return ResponseEntity containing paginated list of affiliate payouts
     */
    @GetMapping("/affiliate-payout")
    @Operation(summary = "Get instructor affiliate payouts", description = """
            Retrieves all affiliate payouts for courses owned by the current instructor.
            This shows who receives commissions from sales of the instructor's courses.

            **Features:**
            - Shows commission payouts related to instructor's courses
            - Includes affiliate user details and commission amounts
            - Shows payout status and timing information
            - Supports pagination for better performance
            - Ordered by creation date (most recent first)

            **Instructor Only:**
            - This endpoint requires INSTRUCTOR role
            - Instructors can only see affiliate payouts for their own courses

            **Use Cases:**
            - Track who is promoting instructor's courses
            - Monitor commission payouts and their status
            - Understand referral patterns for courses
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Affiliate payouts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Instructor role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<InstructorAffiliatePayoutResponseDto>>> getAffiliatePayouts(
            @PageableDefault(size = 10) Pageable pageable) {

        log.info("Instructor requesting affiliate payouts with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        return instructorAffiliatePayoutService.getAffiliatePayouts(pageable);
    }
}
