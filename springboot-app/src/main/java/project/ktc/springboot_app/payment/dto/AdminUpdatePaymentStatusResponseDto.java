package project.ktc.springboot_app.payment.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdatePaymentStatusResponseDto {
	private String id;
	private String paymentMethod;
	private String status;
	private LocalDateTime updatedAt;
}
