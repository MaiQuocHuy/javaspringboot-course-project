package project.ktc.springboot_app.discount.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.discount.dto.DiscountResponseDto;
import project.ktc.springboot_app.discount.interfaces.DiscountService;
import project.ktc.springboot_app.utils.SecurityUtil;

/**
 * REST Controller for common discount operations Provides endpoints for
 * creating discounts with
 * flexible date requirements
 */
@RestController
@RequestMapping("/api/common/discounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Common Discount Management", description = "APIs for managing discounts with flexible date requirements")
@SecurityRequirement(name = "bearerAuth")
public class CommonDiscountController {

	private final DiscountService discountService;

	/**
	 * Create an individual induction discount code for the authenticated user Each
	 * user can only
	 * create one REFERRAL discount This endpoint is specifically designed for the
	 * "receive induction
	 * code" feature
	 */
	@PostMapping("/induction")
	@PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR')")
	@Operation(summary = "Create individual induction discount code", description = "Create a personal REFERRAL discount code for the authenticated user. "
			+ "Each user can only create one induction discount. The discount cannot be used by the owner.")
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Induction discount created successfully", content = @Content(schema = @Schema(implementation = DiscountResponseDto.class))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - User already has an induction discount"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<ApiResponse<DiscountResponseDto>> createInductionDiscount(
			Authentication authentication) {

		// String userId = authentication.getName(); // Get user ID from JWT
		String userId = SecurityUtil.getCurrentUserId();
		log.info("Creating individual induction discount for user: {}", userId);

		return discountService.createUserInductionDiscount(userId);
	}

	/**
	 * Get the induction discount code for the authenticated user Returns the user's
	 * personal REFERRAL
	 * discount if it exists
	 */
	@GetMapping("/my-induction")
	@PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR')")
	@Operation(summary = "Get user's induction discount code", description = "Retrieve the authenticated user's personal REFERRAL discount code if it exists.")
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Induction discount found", content = @Content(schema = @Schema(implementation = DiscountResponseDto.class))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found or no induction discount exists"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<ApiResponse<DiscountResponseDto>> getMyInductionDiscount(
			Authentication authentication) {

		String userId = SecurityUtil.getCurrentUserId();
		log.info("Getting induction discount for user: {}", userId);

		return discountService.getUserInductionDiscount(userId);
	}

	/**
	 * Get all active GENERAL discount codes available for public use Returns list
	 * of active GENERAL
	 * discounts with usage information
	 */
	@GetMapping("/available")
	@Operation(summary = "Get available public discount codes", description = "Retrieve all active GENERAL discount codes that are currently available for public use.")
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Available discounts retrieved successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<ApiResponse<List<DiscountResponseDto>>> getAvailableDiscounts() {

		log.info("Getting available public discount codes");

		return discountService.getAvailablePublicDiscounts();
	}
}
