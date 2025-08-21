package project.ktc.springboot_app.refund.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.refund.entity.Refund;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRefundResponseDto {
    private String id;
    private PaymentInfo payment;
    private String reason;
    private BigDecimal amount;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private String id;
        private BigDecimal amount;
        private String status;
        private LocalDateTime createdAt;
    }

    public static AdminRefundResponseDto fromEntity(Refund refund) {
        return AdminRefundResponseDto.builder()
                .id(refund.getId())
                .payment(PaymentInfo.builder()
                        .id(refund.getPayment().getId())
                        .amount(refund.getPayment().getAmount())
                        .status(refund.getPayment().getStatus().name())
                        .createdAt(refund.getPayment().getCreatedAt())
                        .build())
                .reason(refund.getReason())
                .amount(refund.getAmount())
                .status(refund.getStatus().name())
                .requestedAt(refund.getRequestedAt())
                .processedAt(refund.getProcessedAt())
                .build();
    }
}
