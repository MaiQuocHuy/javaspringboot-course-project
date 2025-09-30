package project.ktc.springboot_app.discount.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.discount.config.AffiliateConfig;
import project.ktc.springboot_app.discount.entity.AffiliatePayout;
import project.ktc.springboot_app.discount.entity.DiscountUsage;
import project.ktc.springboot_app.discount.enums.DiscountType;
import project.ktc.springboot_app.discount.enums.PayoutStatus;
import project.ktc.springboot_app.discount.interfaces.AffiliatePayoutService;
import project.ktc.springboot_app.discount.repositories.AffiliatePayoutRepository;

/**
 * Implementation of AffiliatePayoutService for handling referral commissions Processes payouts
 * asynchronously to avoid blocking payment processing
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AffiliatePayoutServiceImp implements AffiliatePayoutService {

  private final AffiliatePayoutRepository affiliatePayoutRepository;
  private final AffiliateConfig affiliateConfig;

  @Override
  @Async("taskExecutor")
  @Transactional
  public CompletableFuture<AffiliatePayout> createPayoutAsync(
      DiscountUsage discountUsage, BigDecimal finalPrice, String paymentId) {
    log.info(
        "Starting async affiliate payout creation for discount usage: {}", discountUsage.getId());

    try {
      AffiliatePayout payout = createPayout(discountUsage, finalPrice, paymentId);
      log.info(
          "✅ Async affiliate payout created successfully: {} for amount: {}",
          payout.getId(),
          payout.getCommissionAmount());
      return CompletableFuture.completedFuture(payout);
    } catch (Exception e) {
      log.error(
          "❌ Failed to create async affiliate payout for discount usage: {} - {}",
          discountUsage.getId(),
          e.getMessage(),
          e);
      return CompletableFuture.failedFuture(e);
    }
  }

  @Override
  @Transactional
  public AffiliatePayout createPayout(
      DiscountUsage discountUsage, BigDecimal finalPrice, String paymentId) {
    log.info(
        "Creating affiliate payout for discount usage: {} with final price: {}",
        discountUsage.getId(),
        finalPrice);

    // Validate that this is a referral discount
    if (discountUsage.getDiscount().getType() != DiscountType.REFERRAL) {
      throw new IllegalArgumentException(
          "Affiliate payout can only be created for REFERRAL type discounts");
    }

    // Validate referrer exists
    if (discountUsage.getReferredByUser() == null) {
      throw new IllegalArgumentException("No referrer found for this discount usage");
    }

    // Check if commission is enabled
    if (!isCommissionEnabled()) {
      log.warn("Affiliate commission is disabled - skipping payout creation");
      throw new IllegalStateException("Affiliate commission system is disabled");
    }

    // Calculate commission amount
    BigDecimal commissionAmount = calculateCommissionAmount(finalPrice);
    BigDecimal commissionPercent = affiliateConfig.getCommissionPercent();

    // Check if payout already exists for this discount usage
    var existingPayouts = affiliatePayoutRepository.findByDiscountUsageId(discountUsage.getId());
    if (!existingPayouts.isEmpty()) {
      log.warn("Affiliate payout already exists for discount usage: {}", discountUsage.getId());
      return existingPayouts.get(0); // Return existing payout
    }

    // Create affiliate payout record
    AffiliatePayout affiliatePayout =
        AffiliatePayout.builder()
            .referredByUser(discountUsage.getReferredByUser())
            .course(discountUsage.getCourse())
            .discountUsage(discountUsage)
            .commissionPercent(commissionPercent)
            .commissionAmount(commissionAmount)
            .payoutStatus(PayoutStatus.PAID)
            .build();

    // Save payout
    AffiliatePayout savedPayout = affiliatePayoutRepository.save(affiliatePayout);

    log.info(
        "🎉 Affiliate payout created successfully: {} for referrer: {} with commission: ${}",
        savedPayout.getId(),
        discountUsage.getReferredByUser().getId(),
        savedPayout.getCommissionAmount());

    return savedPayout;
  }

  @Override
  public BigDecimal calculateCommissionAmount(BigDecimal finalPrice) {
    if (finalPrice == null || finalPrice.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    BigDecimal commissionPercent = affiliateConfig.getCommissionPercent();
    BigDecimal commissionAmount =
        finalPrice
            .multiply(commissionPercent)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

    log.debug(
        "Commission calculation: finalPrice={}, percent={}%, amount={}",
        finalPrice, commissionPercent, commissionAmount);

    return commissionAmount;
  }

  @Override
  public boolean isCommissionEnabled() {
    boolean enabled = affiliateConfig.isCommissionEnabled();
    log.debug("Affiliate commission enabled: {}", enabled);
    return enabled;
  }
}
