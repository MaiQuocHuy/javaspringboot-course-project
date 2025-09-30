package project.ktc.springboot_app.discount.interfaces;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.discount.dto.CreateDiscountRequest;
import project.ktc.springboot_app.discount.dto.DiscountResponseDto;
import project.ktc.springboot_app.discount.entity.Discount;
import project.ktc.springboot_app.discount.enums.DiscountType;

/** Service interface for discount operations */
public interface DiscountService {

  /**
   * Create a new discount
   *
   * @param request The discount creation request
   * @return ResponseEntity with created discount information
   */
  ResponseEntity<ApiResponse<DiscountResponseDto>> createDiscount(CreateDiscountRequest request);

  /**
   * Create a user-specific induction discount (REFERRAL type) Each user can only create one
   * REFERRAL discount
   *
   * @param userId The user ID to create induction discount for
   * @return ResponseEntity with created discount information
   */
  ResponseEntity<ApiResponse<DiscountResponseDto>> createUserInductionDiscount(String userId);

  /**
   * Get user's induction discount (REFERRAL type) Returns the user's personal REFERRAL discount if
   * it exists
   *
   * @param userId The user ID to get induction discount for
   * @return ResponseEntity with user's induction discount information
   */
  ResponseEntity<ApiResponse<DiscountResponseDto>> getUserInductionDiscount(String userId);

  /**
   * Get available public discount codes (GENERAL type) Returns all active GENERAL discounts that
   * are currently available for public use
   *
   * @return ResponseEntity with list of available public discounts
   */
  ResponseEntity<ApiResponse<List<DiscountResponseDto>>> getAvailablePublicDiscounts();

  /** Get all discounts with pagination */
  ResponseEntity<ApiResponse<PaginatedResponse<DiscountResponseDto>>> getAllDiscounts(
      Pageable pageable);

  /** Get discounts by type */
  ResponseEntity<ApiResponse<PaginatedResponse<DiscountResponseDto>>> getDiscountsByType(
      DiscountType type, Pageable pageable);

  /** Get discount by ID */
  ResponseEntity<ApiResponse<DiscountResponseDto>> getDiscountById(String id);

  /** Get discount by code */
  ResponseEntity<ApiResponse<DiscountResponseDto>> getDiscountByCode(String code);

  /** Get discounts by owner user ID */
  ResponseEntity<ApiResponse<PaginatedResponse<DiscountResponseDto>>> getDiscountsByOwnerUserId(
      String ownerUserId, Pageable pageable);

  /** Update discount status (activate/deactivate) */
  ResponseEntity<ApiResponse<DiscountResponseDto>> updateDiscountStatus(
      String id, boolean isActive);

  /** Delete discount */
  ResponseEntity<ApiResponse<Void>> deleteDiscount(String id);

  /** Check if discount is valid and available for use */
  boolean isDiscountValidForUse(String discountCode, String userId, String courseId);

  /** Get currently valid discounts */
  List<Discount> getCurrentlyValidDiscounts();

  /** Convert entity to DTO */
  DiscountResponseDto convertToDto(Discount discount);

  /** Get discount entity by ID (for internal use) */
  Discount getDiscountEntityById(String id);
}
