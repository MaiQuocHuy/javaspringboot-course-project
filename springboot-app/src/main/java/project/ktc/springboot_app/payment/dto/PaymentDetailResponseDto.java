package project.ktc.springboot_app.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.payment.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for detailed Payment response containing payment information with
 * external gateway data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailResponseDto {

    private String id;

    private BigDecimal amount;

    private String currency;

    private String status;

    private String paymentMethod;

    // @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    private String transactionId;

    private String stripeSessionId;

    private String receiptUrl;

    private CardInfoDto card;

    private CourseInfoDto course;

    /**
     * Nested DTO for card information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardInfoDto {
        private String brand;
        private String last4;
        private Integer expMonth;
        private Integer expYear;
    }

    /**
     * Nested DTO for course information in payment response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseInfoDto {
        private String id;
        private String title;
        private String thumbnailUrl;
    }

    /**
     * Factory method to create PaymentDetailResponseDto from Payment entity
     */
    public static PaymentDetailResponseDto fromEntity(Payment payment) {
        CourseInfoDto courseInfo = CourseInfoDto.builder()
                .id(payment.getCourse().getId())
                .title(payment.getCourse().getTitle())
                .thumbnailUrl(payment.getCourse().getThumbnailUrl())
                .build();

        return PaymentDetailResponseDto.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .currency("VND") // Default currency as specified in requirements
                .status(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod())
                .createdAt(payment.getCreatedAt())
                .stripeSessionId(payment.getSessionId())
                .course(courseInfo)
                .build();
    }

    /**
     * Factory method to create PaymentDetailResponseDto with Stripe data
     */
    public static PaymentDetailResponseDto fromEntityWithStripeData(Payment payment, StripePaymentData stripeData) {
        PaymentDetailResponseDto dto = fromEntity(payment);

        if (stripeData != null) {
            dto.setTransactionId(stripeData.getTransactionId());
            dto.setReceiptUrl(stripeData.getReceiptUrl());

            if (stripeData.getCardBrand() != null) {
                dto.setCard(CardInfoDto.builder()
                        .brand(stripeData.getCardBrand())
                        .last4(stripeData.getCardLast4())
                        .expMonth(stripeData.getCardExpMonth())
                        .expYear(stripeData.getCardExpYear())
                        .build());
            }
        }

        return dto;
    }

    /**
     * Data class for Stripe payment information
     */
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
