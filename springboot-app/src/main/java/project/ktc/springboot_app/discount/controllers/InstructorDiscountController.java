package project.ktc.springboot_app.discount.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.discount.dto.InstructorAffiliatePayoutResponseDto;
import project.ktc.springboot_app.discount.dto.InstructorDiscountUsageResponseDto;
import project.ktc.springboot_app.discount.enums.DiscountType;
import project.ktc.springboot_app.discount.enums.PayoutStatus;
import project.ktc.springboot_app.discount.interfaces.InstructorAffiliatePayoutService;
import project.ktc.springboot_app.discount.interfaces.InstructorDiscountUsageService;

/**
 * REST Controller for instructor discount operations Allows instructors to view discount usage and
 * affiliate payouts for their courses
 */
@RestController
@RequestMapping("/api/instructor")
@PreAuthorize("hasRole('INSTRUCTOR')")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(
    name = "Instructor Discount API",
    description = "API for instructor discount and affiliate operations")
@SecurityRequirement(name = "bearerAuth")
public class InstructorDiscountController {

  private final InstructorDiscountUsageService instructorDiscountUsageService;
  private final InstructorAffiliatePayoutService instructorAffiliatePayoutService;

  /**
   * Get discount usages for courses owned by the instructor
   *
   * @param search Search by discount usage ID, discount code, user name, or course title
   * @param type Filter by discount type
   * @param fromDate Filter by usage date from (ISO date string)
   * @param toDate Filter by usage date to (ISO date string)
   * @param page Page number (0-based)
   * @param size Page size
   * @return ResponseEntity containing paginated list of discount usages
   */
  @GetMapping("/discount-usage")
  @Operation(
      summary = "Get instructor discount usages",
      description =
          """
                        Retrieves all discount usages for courses owned by the current instructor with advanced search and filtering.
                        This shows which discounts have been used on the instructor's courses.

                        **Features:**
                        - Shows discount usage on instructor's courses
                        - Includes user details, course info, and discount details
                        - Shows referral information when applicable
                        - Advanced search and filtering capabilities
                        - Supports pagination for better performance
                        - Ordered by usage date (most recent first)

                        **Search & Filter Options:**
                        - Search by discount usage ID, discount code, user name, or course title
                        - Filter by discount type (GENERAL, REFERRAL)
                        - Filter by usage date range

                        **Example Usage:**
                        - `/api/instructor/discount-usage?search=john&type=REFERRAL&page=0&size=10`
                        - `/api/instructor/discount-usage?fromDate=2024-01-01&toDate=2024-12-31`
                        - `/api/instructor/discount-usage?search=SAVE20&type=GENERAL`

                        **Instructor Only:**
                        - This endpoint requires INSTRUCTOR role
                        - Instructors can only see discount usage for their own courses
                        """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Discount usages retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Instructor role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<
          project.ktc.springboot_app.common.dto.ApiResponse<
              PaginatedResponse<InstructorDiscountUsageResponseDto>>>
      getDiscountUsages(
          @Parameter(
                  description =
                      "Search by discount usage ID, discount code, user name, or course title")
              @RequestParam(required = false)
              String search,
          @Parameter(description = "Filter by discount type", example = "GENERAL")
              @RequestParam(required = false)
              DiscountType type,
          @Parameter(
                  description = "Filter by usage date from (ISO format: yyyy-MM-dd)",
                  example = "2024-01-01")
              @RequestParam(required = false)
              String fromDate,
          @Parameter(
                  description = "Filter by usage date to (ISO format: yyyy-MM-dd)",
                  example = "2024-12-31")
              @RequestParam(required = false)
              String toDate,
          @Parameter(description = "Page number (0-based)")
              @RequestParam(defaultValue = "0")
              @Min(0)
              Integer page,
          @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100)
              Integer size) {

    Pageable pageable = PageRequest.of(page, size);
    log.info(
        "Instructor requesting discount usages with filters - search: {}, type: {}, fromDate: {}, toDate: {}, page: {}, size: {}",
        search,
        type,
        fromDate,
        toDate,
        page,
        size);

    return instructorDiscountUsageService.getDiscountUsages(
        search, type, fromDate, toDate, pageable);
  }

  /**
   * Get affiliate payouts for courses owned by the instructor with search and filter options (Shows
   * who gets commission on instructor's courses)
   */
  @GetMapping("/affiliate-payout")
  @Operation(
      summary = "Get instructor affiliate payouts with search and filters",
      description =
          """
                        Retrieves all affiliate payouts for courses owned by the current instructor with advanced search and filtering options.
                        This shows who receives commissions from sales of the instructor's courses.

                        **Features:**
                        - Advanced search by affiliate payout ID, referrer name, course title, or discount code
                        - Filter by payout status (PENDING, PAID, CANCELLED)
                        - Filter by date range for precise time-based queries
                        - Shows commission payouts related to instructor's courses
                        - Includes affiliate user details and commission amounts
                        - Shows payout status and timing information
                        - Supports pagination for better performance
                        - Ordered by creation date (most recent first)

                        **Search Capabilities:**
                        - Search by affiliate payout ID for exact matches
                        - Search by referrer name (user who gets the commission)
                        - Search by course title
                        - Search by discount code used

                        **Filter Options:**
                        - Status: Filter by payout status (PENDING, PAID, CANCELLED)
                        - Date Range: Filter by payout creation date

                        **Instructor Only:**
                        - This endpoint requires INSTRUCTOR role
                        - Instructors can only see affiliate payouts for their own courses

                        **Use Cases:**
                        - Track who is promoting instructor's courses
                        - Monitor commission payouts and their status
                        - Search for specific payouts or referrers
                        - Analyze payout patterns over time
                        - Understand referral patterns for courses
                        """)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Affiliate payouts retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Instructor role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<
          project.ktc.springboot_app.common.dto.ApiResponse<
              PaginatedResponse<InstructorAffiliatePayoutResponseDto>>>
      getAffiliatePayouts(
          @Parameter(
                  description =
                      "Search by affiliate payout ID, referrer name, course title, or discount code",
                  example = "john.doe or Spring Boot Course")
              @RequestParam(required = false)
              String search,
          @Parameter(description = "Filter by payout status", example = "PENDING")
              @RequestParam(required = false)
              PayoutStatus status,
          @Parameter(
                  description = "Filter by payout date from (ISO format: yyyy-MM-dd)",
                  example = "2024-01-01")
              @RequestParam(required = false)
              String fromDate,
          @Parameter(
                  description = "Filter by payout date to (ISO format: yyyy-MM-dd)",
                  example = "2024-12-31")
              @RequestParam(required = false)
              String toDate,
          @Parameter(description = "Page number (0-based)")
              @RequestParam(defaultValue = "0")
              @Min(0)
              Integer page,
          @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100)
              Integer size) {

    Pageable pageable = PageRequest.of(page, size);
    log.info(
        "Instructor requesting affiliate payouts with filters - search: {}, status: {}, fromDate: {}, toDate: {}, page: {}, size: {}",
        search,
        status,
        fromDate,
        toDate,
        page,
        size);

    return instructorAffiliatePayoutService.getAffiliatePayouts(
        search, status, fromDate, toDate, pageable);
  }

  /**
   * Get a specific discount usage by ID for courses owned by the instructor
   *
   * @param discountUsageId The ID of the discount usage
   * @return ResponseEntity containing the discount usage details
   */
  @GetMapping("/discount-usage/{discountUsageId}")
  @Operation(
      summary = "Get specific discount usage by ID",
      description =
          """
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
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Discount usage retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Instructor role required"),
        @ApiResponse(
            responseCode = "404",
            description = "Discount usage not found or access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<
          project.ktc.springboot_app.common.dto.ApiResponse<InstructorDiscountUsageResponseDto>>
      getDiscountUsageById(@PathVariable String discountUsageId) {

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
  @Operation(
      summary = "Get specific affiliate payout by ID",
      description =
          """
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
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Affiliate payout retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Instructor role required"),
        @ApiResponse(
            responseCode = "404",
            description = "Affiliate payout not found or access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<
          project.ktc.springboot_app.common.dto.ApiResponse<InstructorAffiliatePayoutResponseDto>>
      getAffiliatePayoutById(@PathVariable String affiliatePayoutId) {

    log.info("Instructor requesting affiliate payout details for ID: {}", affiliatePayoutId);

    return instructorAffiliatePayoutService.getAffiliatePayoutById(affiliatePayoutId);
  }
}
