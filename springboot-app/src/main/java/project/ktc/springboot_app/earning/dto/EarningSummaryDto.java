package project.ktc.springboot_app.earning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningSummaryDto {
    private BigDecimal totalEarnings;
    private BigDecimal paidAmount;
    private Long totalTransactions;
}
