package project.ktc.springboot_app.discount.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for student affiliate payout statistics
 * Provides overview of student's affiliate earning statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAffiliatePayoutStatisticsDto {
    /**
     * Total number of affiliate payouts
     */
    private Long totalPayouts;

    /**
     * Total pending payouts count
     */
    private Long pendingPayouts;

    /**
     * Total paid payouts count
     */
    private Long paidPayouts;

    /**
     * Total cancelled payouts count
     */
    private Long cancelledPayouts;

    /**
     * Total commission amount earned (all statuses)
     */
    private BigDecimal totalCommissionAmount;

    /**
     * Total pending commission amount
     */
    private BigDecimal pendingCommissionAmount;

    /**
     * Total paid commission amount
     */
    private BigDecimal paidCommissionAmount;

    /**
     * Total cancelled commission amount
     */
    private BigDecimal cancelledCommissionAmount;
}
