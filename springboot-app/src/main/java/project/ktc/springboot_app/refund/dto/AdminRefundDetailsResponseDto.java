package project.ktc.springboot_app.refund.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.payment.dto.AdminPaymentDetailResponseDto;
import project.ktc.springboot_app.payment.dto.AdminPaymentDetailResponseDto.CardInfoDto;
import project.ktc.springboot_app.refund.entity.Refund;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRefundDetailsResponseDto {
  private String id;
  private AdminPaymentDetailResponseDto payment;
  private String reason;
  private String rejectedReason;
  private BigDecimal amount;
  private String status;
  private LocalDateTime requestedAt;
  private LocalDateTime processedAt;

  public static AdminRefundDetailsResponseDto fromEntity(Refund refund) {
    return AdminRefundDetailsResponseDto.builder()
        .id(refund.getId())
        .payment(AdminPaymentDetailResponseDto.fromEntity(refund.getPayment()))
        .reason(refund.getReason())
        .rejectedReason(refund.getRejectedReason())
        .amount(refund.getAmount())
        .status(refund.getStatus().name())
        .requestedAt(refund.getRequestedAt())
        .processedAt(refund.getProcessedAt())
        .build();
  }

  /** Factory method to create PaymentDetailAdminResponseDto with Stripe data */
  public static AdminRefundDetailsResponseDto fromEntityWithStripeData(
      Refund refund, StripePaymentData stripeData) {
    AdminRefundDetailsResponseDto dto = fromEntity(refund);

    if (stripeData != null) {
      dto.getPayment().setTransactionId(stripeData.getTransactionId());
      dto.getPayment().setReceiptUrl(stripeData.getReceiptUrl());

      if (stripeData.getCardBrand() != null) {
        dto.getPayment()
            .setCard(
                CardInfoDto.builder()
                    .brand(stripeData.getCardBrand())
                    .last4(stripeData.getCardLast4())
                    .expMonth(stripeData.getCardExpMonth())
                    .expYear(stripeData.getCardExpYear())
                    .build());
      }
    }

    return dto;
  }

  /** Data class for Stripe payment information */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class StripePaymentData {
    private String transactionId;
    private String receiptUrl;
    private String cardBrand;
    private String cardLast4;
    private Integer cardExpMonth;
    private Integer cardExpYear;
  }
}
