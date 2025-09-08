package project.ktc.springboot_app.discount.interfaces;

import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.stripe.dto.PriceCalculationResponse;

/**
 * Service interface for discount validation and price calculation
 */
public interface DiscountPriceService {

    /**
     * Validate course availability for purchase
     * 
     * @param course The course to validate
     * @throws IllegalArgumentException if course is not available
     */
    void validateCourseAvailability(Course course);

    /**
     * Calculate final price with discount validation
     * 
     * @param courseId     The course ID
     * @param userId       The user ID attempting to purchase
     * @param discountCode Optional discount code to apply
     * @return PriceCalculationResponse with final pricing details
     * @throws IllegalArgumentException if course or discount is invalid
     */
    PriceCalculationResponse calculatePrice(String courseId, String userId, String discountCode);

    /**
     * Record discount usage after successful payment
     * 
     * @param discountCode   The discount code that was used
     * @param userId         The user who used the discount
     * @param courseId       The course purchased
     * @param discountAmount The actual discount amount applied
     * @param paymentId      Optional payment ID for tracking
     */
    void recordDiscountUsage(String discountCode, String userId, String courseId,
            java.math.BigDecimal discountAmount, String paymentId);
}
