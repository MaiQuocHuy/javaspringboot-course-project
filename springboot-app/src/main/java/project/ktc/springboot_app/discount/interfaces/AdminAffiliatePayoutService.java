package project.ktc.springboot_app.discount.interfaces;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.ktc.springboot_app.discount.dto.response.AffiliatePayoutDetailResponseDto;
import project.ktc.springboot_app.discount.dto.response.AffiliatePayoutResponseDto;
import project.ktc.springboot_app.discount.dto.response.AffiliateStatisticsResponseDto;
import project.ktc.springboot_app.discount.enums.PayoutStatus;

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

	AffiliatePayoutDetailResponseDto getPayoutDetailById(String id);

	AffiliatePayoutResponseDto markPayoutAsPaid(Long id);

	AffiliatePayoutResponseDto cancelPayout(Long id, String reason);

	byte[] exportPayouts(
			Long userId,
			PayoutStatus status,
			LocalDateTime startDate,
			LocalDateTime endDate,
			BigDecimal minAmount,
			BigDecimal maxAmount);
}
