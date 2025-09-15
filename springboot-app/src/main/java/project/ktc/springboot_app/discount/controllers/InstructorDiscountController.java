package project.ktc.springboot_app.discount.controllers;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

        /**
         * Get a specific discount usage by ID for courses owned by the instructor
         * 
         * @param discountUsageId The ID of the discount usage
         * @return ResponseEntity containing the discount usage details
         */
        @GetMapping("/discount-usage/{discountUsageId}")
        @Operation(summary = "Get specific discount usage by ID", description = """
                        Retrieves a specific discount usage by ID for courses owned by the current instructor.
                        This allows instructors to view detailed information about a particular discount usage.

                        **Features:**
                        - Get detailed information about a specific discount usage
                        - Includes user details, course info, and discount details
                        - Shows referral information when applicable
                        - Security: Only shows discount usage for instructor's own courses

                        **Instructor Only:**
                        - This endpoint requires INSTRUCTOR role
                        - Instructors can only see discount usage for their own courses
                        - Returns 404 if the discount usage doesn't exist or doesn't belong to instructor's courses
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Discount usage retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Instructor role required"),
                        @ApiResponse(responseCode = "404", description = "Discount usage not found or access denied"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<InstructorDiscountUsageResponseDto>> getDiscountUsageById(
                        @PathVariable String discountUsageId) {

                log.info("Instructor requesting discount usage details for ID: {}", discountUsageId);

                return instructorDiscountUsageService.getDiscountUsageById(discountUsageId);
        }

        /**
         * Get a specific affiliate payout by ID for courses owned by the instructor
         * 
         * @param affiliatePayoutId The ID of the affiliate payout
         * @return ResponseEntity containing the affiliate payout details
         */
        @GetMapping("/affiliate-payout/{affiliatePayoutId}")
        @Operation(summary = "Get specific affiliate payout by ID", description = """
                        Retrieves a specific affiliate payout by ID for courses owned by the current instructor.
                        This allows instructors to view detailed information about a particular affiliate commission payout.

                        **Features:**
                        - Get detailed information about a specific affiliate payout
                        - Includes affiliate user details and commission amounts
                        - Shows payout status and timing information
                        - Security: Only shows affiliate payouts for instructor's own courses

                        **Instructor Only:**
                        - This endpoint requires INSTRUCTOR role
                        - Instructors can only see affiliate payouts for their own courses
                        - Returns 404 if the affiliate payout doesn't exist or doesn't belong to instructor's courses

                        **Use Cases:**
                        - View detailed commission information for a specific payout
                        - Check payout status and payment timing
                        - Understand referral patterns for specific sales
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Affiliate payout retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Instructor role required"),
                        @ApiResponse(responseCode = "404", description = "Affiliate payout not found or access denied"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<InstructorAffiliatePayoutResponseDto>> getAffiliatePayoutById(
                        @PathVariable String affiliatePayoutId) {

                log.info("Instructor requesting affiliate payout details for ID: {}", affiliatePayoutId);

                return instructorAffiliatePayoutService.getAffiliatePayoutById(affiliatePayoutId);
        }
}
