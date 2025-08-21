package project.ktc.springboot_app.refund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRefundStatusUpdateResponseDto {

    private String id;
    private String paymentId;
    private BigDecimal amount;
    private String status;
    private String reason;
    private String rejectedReason;
}
