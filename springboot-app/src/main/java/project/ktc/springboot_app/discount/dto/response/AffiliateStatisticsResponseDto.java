package project.ktc.springboot_app.discount.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for affiliate statistics */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateStatisticsResponseDto {

	private long totalPayouts;

	private long pendingPayouts;

	private long paidPayouts;

	private long cancelledPayouts;

	private BigDecimal totalCommissionAmount;

	private BigDecimal pendingCommissionAmount;

	private BigDecimal paidCommissionAmount;

	private BigDecimal cancelledCommissionAmount;
}
