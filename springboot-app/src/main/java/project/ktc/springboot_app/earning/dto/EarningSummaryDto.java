package project.ktc.springboot_app.earning.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningSummaryDto {
	private BigDecimal totalEarnings;
	private BigDecimal paidAmount;
	private Long totalTransactions;
}
