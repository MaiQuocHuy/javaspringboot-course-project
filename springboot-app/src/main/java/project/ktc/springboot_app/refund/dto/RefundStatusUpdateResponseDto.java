package project.ktc.springboot_app.refund.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundStatusUpdateResponseDto {

	private String id;
	private String paymentId;
	private BigDecimal amount;
	private String status;
	private String reason;
	private String rejectedReason;
}
