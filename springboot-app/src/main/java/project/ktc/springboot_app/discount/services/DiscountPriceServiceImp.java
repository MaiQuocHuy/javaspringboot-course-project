package project.ktc.springboot_app.discount.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.discount.entity.Discount;
import project.ktc.springboot_app.discount.entity.DiscountUsage;
import project.ktc.springboot_app.discount.enums.DiscountType;
import project.ktc.springboot_app.discount.interfaces.DiscountPriceService;
import project.ktc.springboot_app.discount.repositories.DiscountRepository;
import project.ktc.springboot_app.discount.repositories.DiscountUsageRepository;
import project.ktc.springboot_app.stripe.dto.PriceCalculationResponse;
import project.ktc.springboot_app.user.repositories.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Implementation of DiscountPriceService for handling discount validation and
 * price calculation
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscountPriceServiceImp implements DiscountPriceService {

    private final CourseRepository courseRepository;
    private final DiscountRepository discountRepository;
    private final DiscountUsageRepository discountUsageRepository;
    private final UserRepository userRepository;

    @Override
    public void validateCourseAvailability(Course course) {
        log.info("Validating course availability for course: {}", course.getId());

        // Check if course is published
        if (!Boolean.TRUE.equals(course.getIsPublished())) {
            throw new IllegalArgumentException("Course is not published and not available for purchase");
        }

        // Check if course is approved
        if (!Boolean.TRUE.equals(course.getIsApproved())) {
            throw new IllegalArgumentException("Course is not approved and not available for purchase");
        }

        // Check if course is deleted
        if (Boolean.TRUE.equals(course.getIsDeleted())) {
            throw new IllegalArgumentException("Course is deleted and not available for purchase");
        }

        // Note: is_active and is_open_for_sale fields are not in current Course entity
        // These would need to be added if required in the future

        log.info("Course {} passed all availability validations", course.getId());
    }

    @Override
    public PriceCalculationResponse calculatePrice(String courseId, String userId, String discountCode) {
        log.info("Calculating price for course: {}, user: {}, discount: {}",
                courseId, userId, discountCode);

        // Find and validate course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        validateCourseAvailability(course);

        BigDecimal originalPrice = course.getPrice();
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal finalPrice = originalPrice;
        String appliedDiscountCode = null;
        BigDecimal discountPercent = BigDecimal.ZERO;
        boolean discountApplied = false;

        // Apply discount if provided
        if (discountCode != null && !discountCode.trim().isEmpty()) {
            try {
                Discount discount = validateAndGetDiscount(discountCode, userId, courseId, originalPrice);

                // Calculate discount amount
                discountAmount = calculateDiscountAmount(originalPrice, discount);
                finalPrice = originalPrice.subtract(discountAmount);

                // Ensure final price is not negative
                if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                    finalPrice = BigDecimal.ZERO;
                }

                appliedDiscountCode = discount.getCode();
                discountPercent = discount.getDiscountPercent();
                discountApplied = true;

                log.info("Discount applied successfully: {} -> Original: {}, Discount: {}, Final: {}",
                        discountCode, originalPrice, discountAmount, finalPrice);

            } catch (Exception e) {
                log.warn("Failed to apply discount {}: {}", discountCode, e.getMessage());
                throw e; // Re-throw to let controller handle the error response
            }
        }

        // Round final price to 2 decimal places
        finalPrice = finalPrice.setScale(2, RoundingMode.HALF_UP);
        discountAmount = discountAmount.setScale(2, RoundingMode.HALF_UP);

        return PriceCalculationResponse.builder()
                .originalPrice(originalPrice)
                .discountAmount(discountAmount)
                .finalPrice(finalPrice)
                .appliedDiscountCode(appliedDiscountCode)
                .discountPercent(discountPercent)
                .discountApplied(discountApplied)
                .currency("USD")
                .build();
    }

    @Override
    @Transactional
    public void recordDiscountUsage(String discountCode, String userId, String courseId,
            BigDecimal discountAmount, String paymentId) {
        log.info("Recording discount usage: code={}, user={}, course={}, amount={}",
                discountCode, userId, courseId, discountAmount);

        try {
            // Find discount
            Discount discount = discountRepository.findByCodeIgnoreCase(discountCode)
                    .orElseThrow(() -> new IllegalArgumentException("Discount not found: " + discountCode));

            // Find user and course
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

            // Determine referred by user for REFERRAL type discounts
            User referredByUser = null;
            if (discount.getType() == DiscountType.REFERRAL && discount.getOwnerUser() != null) {
                referredByUser = discount.getOwnerUser();
            }

            // Create discount usage record
            DiscountUsage discountUsage = DiscountUsage.builder()
                    .discount(discount)
                    .user(user)
                    .course(course)
                    .referredByUser(referredByUser)
                    .usedAt(LocalDateTime.now())
                    .discountPercent(discount.getDiscountPercent())
                    .discountAmount(discountAmount)
                    .build();

            discountUsageRepository.save(discountUsage);

            log.info("Discount usage recorded successfully: {}", discountUsage.getId());

        } catch (Exception e) {
            log.error("Failed to record discount usage: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to record discount usage", e);
        }
    }

    /**
     * Validate discount and return if valid
     */
    private Discount validateAndGetDiscount(String discountCode, String userId, String courseId,
            BigDecimal originalPrice) {
        log.info("Validating discount: {}", discountCode);

        // Find discount by code
        Discount discount = discountRepository.findByCodeIgnoreCase(discountCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid discount code: " + discountCode));

        // Check if discount is active
        if (!Boolean.TRUE.equals(discount.getIsActive())) {
            throw new IllegalArgumentException("Discount code is not active: " + discountCode);
        }

        // Check owner restriction for REFERRAL type discounts
        if (discount.getType() == DiscountType.REFERRAL && discount.getOwnerUser() != null
                && discount.getOwnerUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You cannot use your own referral discount code: " + discountCode);
        }

        // Check date validity only if dates are set (not null)
        LocalDateTime now = LocalDateTime.now();

        // Check start date only if it's not null
        if (discount.getStartDate() != null && now.isBefore(discount.getStartDate())) {
            throw new IllegalArgumentException("Discount code is not yet valid: " + discountCode);
        }

        // Check end date only if it's not null
        if (discount.getEndDate() != null && now.isAfter(discount.getEndDate())) {
            throw new IllegalArgumentException("Discount code has expired: " + discountCode);
        }

        // Check usage limit
        if (discount.getUsageLimit() != null) {
            long currentUsage = discountUsageRepository.countByDiscountId(discount.getId());
            if (currentUsage >= discount.getUsageLimit()) {
                throw new IllegalArgumentException("Discount usage limit reached: " + discountCode);
            }
        }

        // Check per-user limit
        if (discount.getPerUserLimit() != null) {
            long userUsage = discountUsageRepository.countByUserIdAndDiscountId(userId, discount.getId());
            if (userUsage >= discount.getPerUserLimit()) {
                throw new IllegalArgumentException(
                        "You have reached the usage limit for this discount: " + discountCode);
            }
        }

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        log.info("Discount validation successful: {}", discountCode);
        return discount;
    }

    /**
     * Calculate discount amount based on discount type
     */
    private BigDecimal calculateDiscountAmount(BigDecimal originalPrice, Discount discount) {
        BigDecimal discountAmount;

        // Currently only supporting percentage discounts
        // In the future, could add support for flat amount discounts
        discountAmount = originalPrice
                .multiply(discount.getDiscountPercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        log.info("Calculated discount amount: {} ({}% of {})",
                discountAmount, discount.getDiscountPercent(), originalPrice);

        return discountAmount;
    }
}
