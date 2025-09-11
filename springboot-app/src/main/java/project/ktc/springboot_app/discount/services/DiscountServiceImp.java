package project.ktc.springboot_app.discount.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.discount.dto.CreateDiscountRequest;
import project.ktc.springboot_app.discount.dto.DiscountResponseDto;
import project.ktc.springboot_app.discount.entity.Discount;
import project.ktc.springboot_app.discount.enums.DiscountType;
import project.ktc.springboot_app.discount.interfaces.DiscountService;
import project.ktc.springboot_app.discount.repositories.DiscountRepository;
import project.ktc.springboot_app.discount.repositories.DiscountUsageRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of DiscountService with comprehensive business logic
 * Handles all discount-related operations with proper validation
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscountServiceImp implements DiscountService {

    private final DiscountRepository discountRepository;
    private final DiscountUsageRepository discountUsageRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<DiscountResponseDto>> createDiscount(CreateDiscountRequest request) {
        try {
            log.info("Creating discount with code: {} and type: {}", request.getCode(), request.getType());

            // Check if discount code already exists
            if (discountRepository.existsByCodeIgnoreCase(request.getCode())) {
                log.warn("Discount code already exists: {}", request.getCode());
                return ApiResponseUtil.conflict("Discount code already exists");
            }

            // Validate owner user for REFERRAL type
            User ownerUser = null;
            if (request.getType() == DiscountType.REFERRAL) {
                if (request.getOwnerUserId() == null || request.getOwnerUserId().trim().isEmpty()) {
                    log.warn("Owner user ID is required for REFERRAL discount type");
                    return ApiResponseUtil.badRequest("Owner user ID is required for REFERRAL discount type");
                }

                ownerUser = userRepository.findById(request.getOwnerUserId())
                        .orElse(null);

                if (ownerUser == null) {
                    log.warn("Owner user not found with ID: {}", request.getOwnerUserId());
                    return ApiResponseUtil.notFound("Owner user not found");
                }

                // Check if user already has a REFERRAL discount
                Optional<Discount> existingReferralDiscount = discountRepository.findByOwnerUserIdAndType(
                        request.getOwnerUserId(), DiscountType.REFERRAL);

                if (existingReferralDiscount.isPresent()) {
                    log.warn("User already has a REFERRAL discount. Owner user ID: {}", request.getOwnerUserId());
                    return ApiResponseUtil.conflict("User already has a referral discount. Cannot create another one.");
                }

            } else {
                // For GENERAL type, owner user should be null
                if (request.getOwnerUserId() != null) {
                    log.warn("Owner user ID must be null for GENERAL discount type");
                    return ApiResponseUtil.badRequest("Owner user ID must be null for GENERAL discount type");
                }
            }

            // Validate date ranges
            LocalDateTime now = LocalDateTime.now();
            if (request.getStartDate().isBefore(now)) {
                log.warn("Start date cannot be in the past: {}", request.getStartDate());
                return ApiResponseUtil.badRequest("Start date cannot be in the past");
            }

            if (request.getEndDate().isBefore(request.getStartDate())) {
                log.warn("End date must be after start date");
                return ApiResponseUtil.badRequest("End date must be after start date");
            }

            // Create discount entity
            Discount discount = Discount.builder()
                    .code(request.getCode().toUpperCase().trim())
                    .discountPercent(request.getDiscountPercent())
                    .description(request.getDescription())
                    .type(request.getType())
                    .ownerUser(ownerUser)
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .usageLimit(request.getUsageLimit())
                    .perUserLimit(request.getPerUserLimit())
                    .isActive(true)
                    .build();

            // Save discount
            Discount savedDiscount = discountRepository.save(discount);

            // Convert to DTO and return
            DiscountResponseDto responseDto = convertToDto(savedDiscount);

            log.info("Discount created successfully with ID: {} and code: {}",
                    savedDiscount.getId(), savedDiscount.getCode());
            return ApiResponseUtil.created(responseDto, "Discount created successfully");

        } catch (Exception e) {
            log.error("Error creating discount: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to create discount. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<DiscountResponseDto>>> getAllDiscounts(Pageable pageable) {
        try {
            log.info("Retrieving all discounts with pagination: page={}, size={}",
                    pageable.getPageNumber(), pageable.getPageSize());

            Page<Discount> discountPage = discountRepository.findAll(pageable);
            Page<DiscountResponseDto> responsePage = discountPage.map(this::convertToDto);
            PaginatedResponse<DiscountResponseDto> paginatedResponse = convertPageToPaginatedResponse(responsePage);

            return ApiResponseUtil.success(paginatedResponse, "Discounts retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving discounts: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve discounts. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<DiscountResponseDto>>> getDiscountsByType(DiscountType type,
            Pageable pageable) {
        try {
            log.info("Retrieving discounts by type: {} with pagination", type);

            Page<Discount> discountPage = discountRepository.findByType(type, pageable);
            Page<DiscountResponseDto> responsePage = discountPage.map(this::convertToDto);
            PaginatedResponse<DiscountResponseDto> paginatedResponse = convertPageToPaginatedResponse(responsePage);

            return ApiResponseUtil.success(paginatedResponse, "Discounts retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving discounts by type: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve discounts. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<DiscountResponseDto>> getDiscountById(String id) {
        try {
            log.info("Retrieving discount by ID: {}", id);

            Optional<Discount> discountOpt = discountRepository.findById(id);
            if (discountOpt.isEmpty()) {
                log.warn("Discount not found with ID: {}", id);
                return ApiResponseUtil.notFound("Discount not found");
            }

            DiscountResponseDto responseDto = convertToDto(discountOpt.get());
            return ApiResponseUtil.success(responseDto, "Discount retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving discount by ID: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve discount. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<DiscountResponseDto>> getDiscountByCode(String code) {
        try {
            log.info("Retrieving discount by code: {}", code);

            Optional<Discount> discountOpt = discountRepository.findByCodeIgnoreCase(code);
            if (discountOpt.isEmpty()) {
                log.warn("Discount not found with code: {}", code);
                return ApiResponseUtil.notFound("Discount not found");
            }

            DiscountResponseDto responseDto = convertToDto(discountOpt.get());
            return ApiResponseUtil.success(responseDto, "Discount retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving discount by code: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve discount. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<DiscountResponseDto>>> getDiscountsByOwnerUserId(
            String ownerUserId,
            Pageable pageable) {
        try {
            log.info("Retrieving discounts by owner user ID: {}", ownerUserId);

            // Verify user exists
            if (!userRepository.existsById(ownerUserId)) {
                log.warn("User not found with ID: {}", ownerUserId);
                return ApiResponseUtil.notFound("User not found");
            }

            Page<Discount> discountPage = discountRepository.findByOwnerUserId(ownerUserId, pageable);
            Page<DiscountResponseDto> responsePage = discountPage.map(this::convertToDto);
            PaginatedResponse<DiscountResponseDto> paginatedResponse = convertPageToPaginatedResponse(responsePage);

            return ApiResponseUtil.success(paginatedResponse, "Discounts retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving discounts by owner user ID: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve discounts. Please try again later.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<DiscountResponseDto>> updateDiscountStatus(String id, boolean isActive) {
        try {
            log.info("Updating discount status for ID: {} to {}", id, isActive);

            Optional<Discount> discountOpt = discountRepository.findById(id);
            if (discountOpt.isEmpty()) {
                log.warn("Discount not found with ID: {}", id);
                return ApiResponseUtil.notFound("Discount not found");
            }

            Discount discount = discountOpt.get();
            discount.setIsActive(isActive);
            Discount updatedDiscount = discountRepository.save(discount);

            DiscountResponseDto responseDto = convertToDto(updatedDiscount);

            log.info("Discount status updated successfully for ID: {}", id);
            return ApiResponseUtil.success(responseDto, "Discount status updated successfully");

        } catch (Exception e) {
            log.error("Error updating discount status: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to update discount status. Please try again later.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteDiscount(String id) {
        try {
            log.info("Deleting discount with ID: {}", id);

            Optional<Discount> discountOpt = discountRepository.findById(id);
            if (discountOpt.isEmpty()) {
                log.warn("Discount not found with ID: {}", id);
                return ApiResponseUtil.notFound("Discount not found");
            }

            Discount discount = discountOpt.get();

            // Check if discount has been used
            long usageCount = discountUsageRepository.countByDiscountId(id);
            if (usageCount > 0) {
                log.warn("Cannot delete discount with ID: {} because it has been used {} times", id, usageCount);
                return ApiResponseUtil
                        .badRequest("Cannot delete discount that has been used. Consider deactivating instead.");
            }

            discountRepository.delete(discount);

            log.info("Discount deleted successfully with ID: {}", id);
            return ApiResponseUtil.success(null, "Discount deleted successfully");

        } catch (Exception e) {
            log.error("Error deleting discount: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to delete discount. Please try again later.");
        }
    }

    @Override
    public boolean isDiscountValidForUse(String discountCode, String userId, String courseId) {
        try {
            log.info("Validating discount code: {} for user: {} and course: {}", discountCode, userId, courseId);

            // Find discount by code
            Optional<Discount> discountOpt = discountRepository.findByCodeIgnoreCase(discountCode);
            if (discountOpt.isEmpty()) {
                log.warn("Discount not found with code: {}", discountCode);
                return false;
            }

            Discount discount = discountOpt.get();

            // Check if discount is currently valid
            if (!discount.isCurrentlyValid()) {
                log.warn("Discount is not currently valid: {}", discountCode);
                return false;
            }

            // Check if discount has usage remaining
            if (!discount.hasUsageRemaining()) {
                log.warn("Discount has no usage remaining: {}", discountCode);
                return false;
            }

            // Check if user can still use this discount
            if (!discount.canUserUse(userId)) {
                log.warn("User {} has exceeded per-user limit for discount: {}", userId, discountCode);
                return false;
            }

            // Verify user exists
            if (!userRepository.existsById(userId)) {
                log.warn("User not found with ID: {}", userId);
                return false;
            }

            // Verify course exists
            if (!courseRepository.existsById(courseId)) {
                log.warn("Course not found with ID: {}", courseId);
                return false;
            }

            log.info("Discount validation successful for code: {}", discountCode);
            return true;

        } catch (Exception e) {
            log.error("Error validating discount: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<Discount> getCurrentlyValidDiscounts() {
        return discountRepository.findCurrentlyValidDiscounts(LocalDateTime.now());
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<DiscountResponseDto>> createUserInductionDiscount(String userId) {
        try {
            log.info("Creating user induction discount for user ID: {}", userId);

            // Validate user exists
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("User not found with ID: {}", userId);
                return ApiResponseUtil.notFound("User not found");
            }

            // Check if user already has a REFERRAL discount
            if (discountRepository.existsByOwnerUserIdAndType(userId, DiscountType.REFERRAL)) {
                log.warn("User already has a REFERRAL discount: {}", userId);
                return ApiResponseUtil.conflict("User already has an induction discount");
            }

            // Generate unique discount code for user
            String discountCode = generateUserDiscountCode(user);

            // Ensure the generated code is unique
            while (discountRepository.existsByCodeIgnoreCase(discountCode)) {
                discountCode = generateUserDiscountCode(user);
            }

            // Create discount entity with REFERRAL type
            // For induction discounts, we don't set time limits (startDate and endDate =
            // null)
            Discount discount = Discount.builder()
                    .code(discountCode)
                    .discountPercent(BigDecimal.valueOf(2.0)) // Fixed 2% discount for induction
                    .description("Induction discount for " + user.getName())
                    .type(DiscountType.REFERRAL)
                    .ownerUser(user)
                    .startDate(null) // No time restriction - always valid
                    .endDate(null) // No time restriction - always valid
                    .usageLimit(null) // Unlimited usage by others
                    .perUserLimit(1) // Each user can only use once
                    .isActive(true)
                    .build();

            // Save discount
            Discount savedDiscount = discountRepository.save(discount);

            // Convert to DTO and return
            DiscountResponseDto responseDto = convertToDto(savedDiscount);

            log.info("User induction discount created successfully with ID: {} and code: {} for user: {}",
                    savedDiscount.getId(), savedDiscount.getCode(), userId);
            return ApiResponseUtil.created(responseDto, "Induction discount created successfully");

        } catch (Exception e) {
            log.error("Error creating user induction discount for user {}: {}", userId, e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to create induction discount. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<DiscountResponseDto>> getUserInductionDiscount(String userId) {
        try {
            log.info("Getting user induction discount for user ID: {}", userId);

            // Validate user exists
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("User not found with ID: {}", userId);
                return ApiResponseUtil.notFound("User not found");
            }

            // Find user's REFERRAL discount
            Discount userReferralDiscount = discountRepository.findByOwnerUserIdAndType(userId, DiscountType.REFERRAL)
                    .orElse(null);
            if (userReferralDiscount == null) {
                log.info("No induction discount found for user: {}", userId);
                return ApiResponseUtil.notFound("No induction discount found for this user");
            }

            // Convert to DTO and return
            DiscountResponseDto responseDto = convertToDto(userReferralDiscount);

            log.info("User induction discount found with ID: {} and code: {} for user: {}",
                    userReferralDiscount.getId(), userReferralDiscount.getCode(), userId);
            return ApiResponseUtil.success(responseDto, "User induction discount retrieved successfully");

        } catch (Exception e) {
            log.error("Error getting user induction discount for user {}: {}", userId, e.getMessage(), e);
            return ApiResponseUtil
                    .internalServerError("Failed to retrieve induction discount. Please try again later.");
        }
    }

    /**
     * Generates a unique discount code for a user based on their name and timestamp
     */
    private String generateUserDiscountCode(User user) {
        String userName = user.getName().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (userName.length() > 8) {
            userName = userName.substring(0, 8);
        }
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8); // Last 5 digits
        return "IND_" + userName + "_" + timestamp;
    }

    @Override
    public DiscountResponseDto convertToDto(Discount discount) {
        if (discount == null) {
            return null;
        }

        DiscountResponseDto.DiscountResponseDtoBuilder builder = DiscountResponseDto.builder()
                .id(discount.getId())
                .code(discount.getCode())
                .discountPercent(discount.getDiscountPercent())
                .description(discount.getDescription())
                .type(discount.getType())
                .startDate(discount.getStartDate())
                .endDate(discount.getEndDate())
                .usageLimit(discount.getUsageLimit())
                .perUserLimit(discount.getPerUserLimit())
                .isActive(discount.getIsActive())
                .currentUsageCount(discount.getDiscountUsages().size())
                .createdAt(discount.getCreatedAt())
                .updatedAt(discount.getUpdatedAt());

        // Add owner user info for REFERRAL type discounts
        if (discount.getType() == DiscountType.REFERRAL && discount.getOwnerUser() != null) {
            User ownerUser = discount.getOwnerUser();
            DiscountResponseDto.OwnerUserInfo ownerUserInfo = DiscountResponseDto.OwnerUserInfo.builder()
                    .id(ownerUser.getId())
                    .name(ownerUser.getName())
                    .email(ownerUser.getEmail())
                    .build();
            builder.ownerUser(ownerUserInfo);
        }

        return builder.build();
    }

    /**
     * Helper method to convert Spring Data Page to PaginatedResponse
     */
    private <T> PaginatedResponse<T> convertPageToPaginatedResponse(Page<T> page) {
        PaginatedResponse.PageInfo pageInfo = PaginatedResponse.PageInfo.builder()
                .number(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .first(page.isFirst())
                .last(page.isLast())
                .build();

        return PaginatedResponse.<T>builder()
                .content(page.getContent())
                .page(pageInfo)
                .build();
    }
}
