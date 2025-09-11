package project.ktc.springboot_app.discount.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.ktc.springboot_app.discount.dto.request.BulkPayoutActionRequestDto;
import project.ktc.springboot_app.discount.dto.response.AffiliatePayoutResponseDto;
import project.ktc.springboot_app.discount.dto.response.AffiliateStatisticsResponseDto;
import project.ktc.springboot_app.discount.enums.PayoutStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface AdminAffiliatePayoutService {

    Page<AffiliatePayoutResponseDto> getAffiliatePayouts(
            Pageable pageable,
            Long userId,
            PayoutStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount);

    AffiliateStatisticsResponseDto getAffiliateStatistics();

    AffiliatePayoutResponseDto getPayoutById(Long id);

    AffiliatePayoutResponseDto markPayoutAsPaid(Long id);

    AffiliatePayoutResponseDto cancelPayout(Long id, String reason);

    String bulkActionPayouts(BulkPayoutActionRequestDto bulkRequest);

    byte[] exportPayouts(
            Long userId,
            PayoutStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount);
}
