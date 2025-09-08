package project.ktc.springboot_app.discount.interfaces;

import project.ktc.springboot_app.discount.entity.AffiliatePayout;
import project.ktc.springboot_app.discount.entity.DiscountUsage;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for handling affiliate payouts
 */
public interface AffiliatePayoutService {

    /**
     * Asynchronously create affiliate payout for referral discount usage
     * 
     * @param discountUsage The discount usage record
     * @param finalPrice    The final price paid (after discount)
     * @param paymentId     The payment ID for tracking
     * @return CompletableFuture containing the created AffiliatePayout
     */
    CompletableFuture<AffiliatePayout> createPayoutAsync(DiscountUsage discountUsage, BigDecimal finalPrice,
            String paymentId);

    /**
     * Create affiliate payout synchronously (for testing or specific use cases)
     * 
     * @param discountUsage The discount usage record
     * @param finalPrice    The final price paid (after discount)
     * @param paymentId     The payment ID for tracking
     * @return The created AffiliatePayout
     */
    AffiliatePayout createPayout(DiscountUsage discountUsage, BigDecimal finalPrice, String paymentId);

    /**
     * Calculate commission amount based on final price
     * 
     * @param finalPrice The final price after discount
     * @return The commission amount
     */
    BigDecimal calculateCommissionAmount(BigDecimal finalPrice);

    /**
     * Check if affiliate commission is enabled
     * 
     * @return true if enabled, false otherwise
     */
    boolean isCommissionEnabled();
}
