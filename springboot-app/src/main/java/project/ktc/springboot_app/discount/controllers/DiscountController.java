package project.ktc.springboot_app.discount.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.discount.dto.CreateDiscountRequest;
import project.ktc.springboot_app.discount.dto.DiscountEmailRequest;
import project.ktc.springboot_app.discount.dto.DiscountEmailResponse;
import project.ktc.springboot_app.discount.dto.DiscountResponseDto;
import project.ktc.springboot_app.discount.enums.DiscountType;
import project.ktc.springboot_app.discount.interfaces.DiscountService;
import project.ktc.springboot_app.email.interfaces.EmailService;

/**
 * REST Controller for discount management operations All endpoints require ADMIN role for access
 */
@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Discount API", description = "API for managing discounts (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class DiscountController {

  private final DiscountService discountService;
  private final EmailService emailService;

  /** Create a new discount Only users with ADMIN role can create discounts */
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Create a new discount",
      description =
          "Creates a new discount with the specified parameters. "
              + "For GENERAL type discounts, ownerUserId must be null. "
              + "For REFERRAL type discounts, ownerUserId is required. "
              + "Only ADMIN users can access this endpoint.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Discount created successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data or business rule violation"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have ADMIN role"),
        @ApiResponse(
            responseCode = "404",
            description = "Owner user not found (for REFERRAL type)"),
        @ApiResponse(responseCode = "409", description = "Discount code already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<DiscountResponseDto>>
      createDiscount(@Valid @RequestBody CreateDiscountRequest request) {

    log.info(
        "Admin creating discount with code: {} and type: {}", request.getCode(), request.getType());
    return discountService.createDiscount(request);
  }

  /** Get all discounts with pagination */
  @GetMapping
  @Operation(
      summary = "Get all discounts",
      description =
          "Retrieves all discounts with pagination and sorting. Only ADMIN users can access this endpoint.")
  @PreAuthorize("hasRole('ADMIN')")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Discounts retrieved successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have ADMIN role"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<
          project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<DiscountResponseDto>>>
      getAllDiscounts(
          @Parameter(description = "Page number (0-based)", example = "0")
              @RequestParam(defaultValue = "0")
              int page,
          @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10")
              int size,
          @Parameter(description = "Sort field", example = "createdAt")
              @RequestParam(defaultValue = "createdAt")
              String sortBy,
          @Parameter(description = "Sort direction", example = "DESC")
              @RequestParam(defaultValue = "DESC")
              String sortDir) {

    Sort sort =
        Sort.by(sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
    Pageable pageable = PageRequest.of(page, size, sort);

    log.info(
        "Admin retrieving all discounts - page: {}, size: {}, sort: {} {}",
        page,
        size,
        sortBy,
        sortDir);

    return discountService.getAllDiscounts(pageable);
  }

  /** Get discounts by type */
  @GetMapping("/type/{type}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Get discounts by type",
      description =
          "Retrieves discounts filtered by type (GENERAL or REFERRAL). Only ADMIN users can access this endpoint.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Discounts retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid discount type"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have ADMIN role"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<
          project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<DiscountResponseDto>>>
      getDiscountsByType(
          @Parameter(description = "Discount type", example = "GENERAL", required = true)
              @PathVariable
              DiscountType type,
          @Parameter(description = "Page number (0-based)", example = "0")
              @RequestParam(defaultValue = "0")
              int page,
          @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10")
              int size,
          @Parameter(description = "Sort field", example = "createdAt")
              @RequestParam(defaultValue = "createdAt")
              String sortBy,
          @Parameter(description = "Sort direction", example = "DESC")
              @RequestParam(defaultValue = "DESC")
              String sortDir) {

    Sort sort =
        Sort.by(sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
    Pageable pageable = PageRequest.of(page, size, sort);

    log.info("Admin retrieving discounts by type: {} - page: {}, size: {}", type, page, size);

    return discountService.getDiscountsByType(type, pageable);
  }

  /** Get discount by ID */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Get discount by ID",
      description =
          "Retrieves a specific discount by its unique identifier. Only ADMIN users can access this endpoint.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Discount retrieved successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Discount not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<DiscountResponseDto>>
      getDiscountById(
          @Parameter(description = "Discount ID", example = "discount-uuid-123", required = true)
              @PathVariable
              String id) {

    log.info("Admin retrieving discount by ID: {}", id);
    return discountService.getDiscountById(id);
  }

  /** Get discount by code */
  @GetMapping("/code/{code}")
  @Operation(
      summary = "Get discount by code",
      description =
          "Retrieves a specific discount by its code. Only ADMIN users can access this endpoint.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Discount retrieved successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Discount not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<DiscountResponseDto>>
      getDiscountByCode(
          @Parameter(description = "Discount code", example = "WELCOME10", required = true)
              @PathVariable
              String code) {

    log.info("Admin retrieving discount by code: {}", code);
    return discountService.getDiscountByCode(code);
  }

  /** Get discounts by owner user ID */
  @GetMapping("/owner/{ownerUserId}")
  @Operation(
      summary = "Get discounts by owner user ID",
      description =
          "Retrieves discounts created by a specific user (REFERRAL type discounts). Only ADMIN users can access this endpoint.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Discounts retrieved successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have ADMIN role"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<
          project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<DiscountResponseDto>>>
      getDiscountsByOwnerUserId(
          @Parameter(description = "Owner user ID", example = "user-001", required = true)
              @PathVariable
              String ownerUserId,
          @Parameter(description = "Page number (0-based)", example = "0")
              @RequestParam(defaultValue = "0")
              int page,
          @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10")
              int size,
          @Parameter(description = "Sort field", example = "createdAt")
              @RequestParam(defaultValue = "createdAt")
              String sortBy,
          @Parameter(description = "Sort direction", example = "DESC")
              @RequestParam(defaultValue = "DESC")
              String sortDir) {

    Sort sort =
        Sort.by(sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
    Pageable pageable = PageRequest.of(page, size, sort);

    log.info(
        "Admin retrieving discounts by owner user ID: {} - page: {}, size: {}",
        ownerUserId,
        page,
        size);

    return discountService.getDiscountsByOwnerUserId(ownerUserId, pageable);
  }

  /** Update discount status (activate/deactivate) */
  @PatchMapping("/{id}/status")
  @Operation(
      summary = "Update discount status",
      description =
          "Activates or deactivates a discount. Only ADMIN users can access this endpoint.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Discount status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Discount not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<DiscountResponseDto>>
      updateDiscountStatus(
          @Parameter(description = "Discount ID", example = "discount-uuid-123", required = true)
              @PathVariable
              String id,
          @Parameter(
                  description = "New status (true for active, false for inactive)",
                  example = "true",
                  required = true)
              @RequestParam
              boolean isActive) {

    log.info("Admin updating discount status for ID: {} to {}", id, isActive);
    return discountService.updateDiscountStatus(id, isActive);
  }

  /** Delete discount */
  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete discount",
      description =
          "Deletes a discount permanently. Can only delete discounts that have never been used. Only ADMIN users can access this endpoint.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Discount deleted successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Cannot delete discount that has been used"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Discount not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> deleteDiscount(
      @Parameter(description = "Discount ID", example = "discount-uuid-123", required = true)
          @PathVariable
          String id) {

    log.info("Admin deleting discount with ID: {}", id);
    return discountService.deleteDiscount(id);
  }

  /** Validate discount for use (utility endpoint for testing) */
  @GetMapping("/validate")
  @Operation(
      summary = "Validate discount for use",
      description =
          "Validates if a discount can be used by a specific user for a course. Utility endpoint for testing and validation. Only ADMIN users can access this endpoint.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Validation result returned"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have ADMIN role"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Boolean>>
      validateDiscount(
          @Parameter(description = "Discount code", example = "WELCOME10", required = true)
              @RequestParam
              String discountCode,
          @Parameter(description = "User ID", example = "user-001", required = true) @RequestParam
              String userId,
          @Parameter(description = "Course ID", example = "course-001", required = true)
              @RequestParam
              String courseId) {

    log.info(
        "Admin validating discount: {} for user: {} and course: {}",
        discountCode,
        userId,
        courseId);

    boolean isValid = discountService.isDiscountValidForUse(discountCode, userId, courseId);
    String message = isValid ? "Discount is valid for use" : "Discount is not valid for use";

    return ResponseEntity.ok(
        project.ktc.springboot_app.common.dto.ApiResponse.success(isValid, message));
  }

  /** Send discount code email to all students, specific users, or single user */
  @PostMapping("/email")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Send discount code email to all students, specific users, or single user",
      description =
          "Sends discount code email to all STUDENT users, specific users, or a single user using discount ID. Priority: userIds > userId > all students. Backend will fetch discount details automatically. Only ADMIN users can access this endpoint.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Email sending initiated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid request data"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have ADMIN role"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<DiscountEmailResponse>>
      sendDiscountEmail(@Valid @RequestBody DiscountEmailRequest request) {

    // Log request details
    if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
      log.info(
          "Admin sending discount email to {} specific users - ID: {}, Subject: {}",
          request.getUserIds().size(),
          request.getDiscountId(),
          request.getSubject());
    } else if (request.getUserId() != null && !request.getUserId().trim().isEmpty()) {
      log.info(
          "Admin sending discount email to specific user - ID: {}, User: {}, Subject: {}",
          request.getDiscountId(),
          request.getUserId(),
          request.getSubject());
    } else {
      log.info(
          "Admin sending discount email to all students - ID: {}, Subject: {}",
          request.getDiscountId(),
          request.getSubject());
    }

    try {
      // Get discount details first for response
      ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<DiscountResponseDto>>
          discountResponseEntity = discountService.getDiscountById(request.getDiscountId());

      project.ktc.springboot_app.common.dto.ApiResponse<DiscountResponseDto> discountResponse =
          discountResponseEntity.getBody();

      if (discountResponseEntity.getStatusCode().value() != 200
          || discountResponse == null
          || discountResponse.getStatusCode() != 200
          || discountResponse.getData() == null) {
        return ApiResponseUtil.badRequest("Discount not found with ID: " + request.getDiscountId());
      }

      String discountCode = discountResponse.getData().getCode();

      // Send emails based on priority: userIds > userId > all students
      Long totalRecipients;

      if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
        // Send to multiple specific users (highest priority)
        totalRecipients =
            emailService
                .sendDiscountCodeToMultipleUsers(
                    request.getDiscountId(), request.getSubject(), request.getUserIds())
                .get(); // Wait for completion to get count
      } else if (request.getUserId() != null && !request.getUserId().trim().isEmpty()) {
        // Send to single specific user (medium priority)
        totalRecipients =
            emailService
                .sendDiscountCodeToSpecificUser(
                    request.getDiscountId(), request.getSubject(), request.getUserId())
                .get(); // Wait for completion to get count
      } else {
        // Send to all students (lowest priority)
        totalRecipients =
            emailService
                .sendDiscountCodeToAllStudents(request.getDiscountId(), request.getSubject())
                .get(); // Wait for completion to get count
      }

      DiscountEmailResponse response =
          DiscountEmailResponse.builder()
              .estimatedRecipients(totalRecipients)
              .discountCode(discountCode)
              .subject(request.getSubject())
              .build();

      String message;
      if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
        message =
            String.format(
                "Successfully sent discount emails to %d out of %d requested users",
                totalRecipients, request.getUserIds().size());
      } else if (request.getUserId() != null && !request.getUserId().trim().isEmpty()) {
        message =
            totalRecipients > 0
                ? String.format("Successfully sent discount email to user %s", request.getUserId())
                : String.format("Failed to send discount email to user %s", request.getUserId());
      } else {
        message =
            String.format("Successfully sent discount emails to %d students", totalRecipients);
      }

      return ApiResponseUtil.success(response, message);

    } catch (Exception e) {
      log.error("Failed to send discount emails: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError(
          "Failed to send discount emails: " + e.getMessage());
    }
  }
}
