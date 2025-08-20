package project.ktc.springboot_app.refund.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.payment.dto.AdminPaymentDetailResponseDto;
import project.ktc.springboot_app.refund.entity.Refund;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRefundDetailsResponseDto {
    private String id;
    private AdminPaymentDetailResponseDto payment;
    private String reason;
    private BigDecimal amount;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    public static AdminRefundDetailsResponseDto fromEntity(Refund refund) {
        return AdminRefundDetailsResponseDto.builder()
                .id(refund.getId())
                .payment(AdminPaymentDetailResponseDto.fromEntity(refund.getPayment()))
                .reason(refund.getReason())
                .amount(refund.getAmount())
                .status(refund.getStatus().name())
                .requestedAt(refund.getRequestedAt())
                .processedAt(refund.getProcessedAt())
                .build();
    }

}
