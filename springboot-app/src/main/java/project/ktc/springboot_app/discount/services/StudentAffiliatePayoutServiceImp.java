package project.ktc.springboot_app.discount.services;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.discount.dto.StudentAffiliatePayoutResponseDto;
import project.ktc.springboot_app.discount.dto.StudentAffiliatePayoutStatisticsDto;
import project.ktc.springboot_app.discount.entity.AffiliatePayout;
import project.ktc.springboot_app.discount.enums.PayoutStatus;
import project.ktc.springboot_app.discount.interfaces.StudentAffiliatePayoutService;
import project.ktc.springboot_app.discount.repositories.StudentAffiliatePayoutRepository;
import project.ktc.springboot_app.utils.SecurityUtil;

/** Service implementation for student affiliate payout operations */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentAffiliatePayoutServiceImp implements StudentAffiliatePayoutService {

	private final StudentAffiliatePayoutRepository studentAffiliatePayoutRepository;

	@Override
	public ResponseEntity<ApiResponse<PaginatedResponse<StudentAffiliatePayoutResponseDto>>> getAffiliatePayouts(
			Pageable pageable) {

		String studentId = SecurityUtil.getCurrentUserId();
		log.info(
				"Getting affiliate payouts for student: {}, page: {}, size: {}",
				studentId,
				pageable.getPageNumber(),
				pageable.getPageSize());

		try {
			Page<AffiliatePayout> affiliatePayoutPage = studentAffiliatePayoutRepository
					.findByReferredByUserId(studentId, pageable);

			Page<StudentAffiliatePayoutResponseDto> responsePage = affiliatePayoutPage
					.map(StudentAffiliatePayoutResponseDto::fromEntity);

			PaginatedResponse<StudentAffiliatePayoutResponseDto> paginatedResponse = PaginatedResponse
					.<StudentAffiliatePayoutResponseDto>builder()
					.content(responsePage.getContent())
					.page(
							PaginatedResponse.PageInfo.builder()
									.number(responsePage.getNumber())
									.size(responsePage.getSize())
									.totalElements(responsePage.getTotalElements())
									.totalPages(responsePage.getTotalPages())
									.first(responsePage.isFirst())
									.last(responsePage.isLast())
									.build())
					.build();

			log.info(
					"Successfully retrieved {} affiliate payouts for student: {}",
					responsePage.getTotalElements(),
					studentId);

			return ApiResponseUtil.success(
					paginatedResponse, "Student affiliate payouts retrieved successfully");

		} catch (Exception e) {
			log.error("Error retrieving affiliate payouts for student: {}", studentId, e);
			return ApiResponseUtil.internalServerError("Failed to retrieve affiliate payouts");
		}
	}

	@Override
	public ResponseEntity<ApiResponse<StudentAffiliatePayoutStatisticsDto>> getAffiliatePayoutStatistics() {

		String studentId = SecurityUtil.getCurrentUserId();
		log.info("Getting affiliate payout statistics for student: {}", studentId);

		try {
			// Get counts by status
			Long totalPayouts = studentAffiliatePayoutRepository.countByReferredByUserId(studentId);
			Long pendingPayouts = studentAffiliatePayoutRepository.countByReferredByUserIdAndPayoutStatus(
					studentId, PayoutStatus.PENDING);
			Long paidPayouts = studentAffiliatePayoutRepository.countByReferredByUserIdAndPayoutStatus(
					studentId, PayoutStatus.PAID);
			Long cancelledPayouts = studentAffiliatePayoutRepository.countByReferredByUserIdAndPayoutStatus(
					studentId, PayoutStatus.CANCELLED);

			// Get commission amounts by status
			BigDecimal totalCommissionAmount = studentAffiliatePayoutRepository
					.sumCommissionAmountByReferredByUserId(studentId);
			BigDecimal pendingCommissionAmount = studentAffiliatePayoutRepository
					.sumCommissionAmountByReferredByUserIdAndPayoutStatus(
							studentId, PayoutStatus.PENDING);
			BigDecimal paidCommissionAmount = studentAffiliatePayoutRepository
					.sumCommissionAmountByReferredByUserIdAndPayoutStatus(
							studentId, PayoutStatus.PAID);
			BigDecimal cancelledCommissionAmount = studentAffiliatePayoutRepository
					.sumCommissionAmountByReferredByUserIdAndPayoutStatus(
							studentId, PayoutStatus.CANCELLED);

			StudentAffiliatePayoutStatisticsDto statistics = StudentAffiliatePayoutStatisticsDto.builder()
					.totalPayouts(totalPayouts != null ? totalPayouts : 0L)
					.pendingPayouts(pendingPayouts != null ? pendingPayouts : 0L)
					.paidPayouts(paidPayouts != null ? paidPayouts : 0L)
					.cancelledPayouts(cancelledPayouts != null ? cancelledPayouts : 0L)
					.totalCommissionAmount(
							totalCommissionAmount != null ? totalCommissionAmount : BigDecimal.ZERO)
					.pendingCommissionAmount(
							pendingCommissionAmount != null ? pendingCommissionAmount : BigDecimal.ZERO)
					.paidCommissionAmount(
							paidCommissionAmount != null ? paidCommissionAmount : BigDecimal.ZERO)
					.cancelledCommissionAmount(
							cancelledCommissionAmount != null ? cancelledCommissionAmount : BigDecimal.ZERO)
					.build();

			log.info("Successfully retrieved affiliate payout statistics for student: {}", studentId);

			return ApiResponseUtil.success(
					statistics, "Student affiliate payout statistics retrieved successfully");

		} catch (Exception e) {
			log.error("Error retrieving affiliate payout statistics for student: {}", studentId, e);
			return ApiResponseUtil.internalServerError("Failed to retrieve affiliate payout statistics");
		}
	}
}
