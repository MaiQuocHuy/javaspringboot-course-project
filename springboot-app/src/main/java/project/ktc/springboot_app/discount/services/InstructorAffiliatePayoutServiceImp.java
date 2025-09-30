package project.ktc.springboot_app.discount.services;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.discount.dto.InstructorAffiliatePayoutResponseDto;
import project.ktc.springboot_app.discount.entity.AffiliatePayout;
import project.ktc.springboot_app.discount.enums.PayoutStatus;
import project.ktc.springboot_app.discount.interfaces.InstructorAffiliatePayoutService;
import project.ktc.springboot_app.discount.repositories.InstructorAffiliatePayoutRepository;
import project.ktc.springboot_app.utils.SecurityUtil;

/** Service implementation for instructor affiliate payout operations */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstructorAffiliatePayoutServiceImp implements InstructorAffiliatePayoutService {

	private final InstructorAffiliatePayoutRepository instructorAffiliatePayoutRepository;

	@Override
	public ResponseEntity<ApiResponse<PaginatedResponse<InstructorAffiliatePayoutResponseDto>>> getAffiliatePayouts(
			Pageable pageable) {

		String instructorId = SecurityUtil.getCurrentUserId();
		log.info(
				"Getting affiliate payouts for instructor: {}, page: {}, size: {}",
				instructorId,
				pageable.getPageNumber(),
				pageable.getPageSize());

		try {
			Page<AffiliatePayout> affiliatePayoutPage = instructorAffiliatePayoutRepository
					.findByCourseInstructorId(instructorId, pageable);

			Page<InstructorAffiliatePayoutResponseDto> responsePage = affiliatePayoutPage
					.map(InstructorAffiliatePayoutResponseDto::fromEntity);

			PaginatedResponse<InstructorAffiliatePayoutResponseDto> paginatedResponse = PaginatedResponse
					.<InstructorAffiliatePayoutResponseDto>builder()
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
					"Successfully retrieved {} affiliate payouts for instructor: {}",
					responsePage.getTotalElements(),
					instructorId);

			return ApiResponseUtil.success(
					paginatedResponse, "Instructor affiliate payouts retrieved successfully");

		} catch (Exception e) {
			log.error("Error retrieving affiliate payouts for instructor: {}", instructorId, e);
			return ApiResponseUtil.internalServerError("Failed to retrieve affiliate payouts");
		}
	}

	@Override
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<InstructorAffiliatePayoutResponseDto>> getAffiliatePayoutById(
			String affiliatePayoutId) {

		String instructorId = SecurityUtil.getCurrentUserId();
		log.info("Getting affiliate payout {} for instructor: {}", affiliatePayoutId, instructorId);

		try {
			AffiliatePayout affiliatePayout = instructorAffiliatePayoutRepository.findByIdAndCourseInstructorId(
					affiliatePayoutId, instructorId);

			if (affiliatePayout == null) {
				log.warn(
						"Affiliate payout {} not found for instructor: {}", affiliatePayoutId, instructorId);
				return ApiResponseUtil.notFound(
						"Affiliate payout not found or you don't have permission to view it");
			}

			InstructorAffiliatePayoutResponseDto responseDto = InstructorAffiliatePayoutResponseDto
					.fromEntity(affiliatePayout);

			return ApiResponseUtil.success(responseDto, "Affiliate payout retrieved successfully");

		} catch (Exception e) {
			log.error(
					"Error retrieving affiliate payout {} for instructor: {}",
					affiliatePayoutId,
					instructorId,
					e);
			return ApiResponseUtil.internalServerError("Failed to retrieve affiliate payout");
		}
	}

	@Override
	public ResponseEntity<ApiResponse<PaginatedResponse<InstructorAffiliatePayoutResponseDto>>> getAffiliatePayouts(
			String search, PayoutStatus status, String fromDate, String toDate, Pageable pageable) {

		String instructorId = SecurityUtil.getCurrentUserId();
		log.info(
				"Getting affiliate payouts with filters for instructor: {} - search: {}, status: {}, fromDate: {}, toDate: {}, page: {}, size: {}",
				instructorId,
				search,
				status,
				fromDate,
				toDate,
				pageable.getPageNumber(),
				pageable.getPageSize());

		try {
			LocalDate fromLocalDate = null;
			LocalDate toLocalDate = null;

			// Parse and validate date parameters
			if (fromDate != null && !fromDate.isEmpty()) {
				try {
					fromLocalDate = LocalDate.parse(fromDate);
				} catch (DateTimeParseException e) {
					return ApiResponseUtil.badRequest("Invalid fromDate format. Use YYYY-MM-DD format");
				}
			}

			if (toDate != null && !toDate.isEmpty()) {
				try {
					toLocalDate = LocalDate.parse(toDate);
				} catch (DateTimeParseException e) {
					return ApiResponseUtil.badRequest("Invalid toDate format. Use YYYY-MM-DD format");
				}
			}

			// Validate date range if both dates provided
			if (fromLocalDate != null && toLocalDate != null && fromLocalDate.isAfter(toLocalDate)) {
				return ApiResponseUtil.badRequest("Start date cannot be after end date");
			}

			Page<AffiliatePayout> affiliatePayoutPage = instructorAffiliatePayoutRepository
					.findByCourseInstructorIdWithFilter(
							instructorId, search, status, fromLocalDate, toLocalDate, pageable);

			Page<InstructorAffiliatePayoutResponseDto> responsePage = affiliatePayoutPage
					.map(InstructorAffiliatePayoutResponseDto::fromEntity);

			PaginatedResponse<InstructorAffiliatePayoutResponseDto> paginatedResponse = PaginatedResponse
					.<InstructorAffiliatePayoutResponseDto>builder()
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
					"Successfully retrieved {} affiliate payouts for instructor: {} with filters",
					responsePage.getTotalElements(),
					instructorId);

			return ApiResponseUtil.success(
					paginatedResponse, "Instructor affiliate payouts with filters retrieved successfully");

		} catch (Exception e) {
			log.error(
					"Error retrieving affiliate payouts with filters for instructor: {}", instructorId, e);
			return ApiResponseUtil.internalServerError(
					"Failed to retrieve affiliate payouts with filters");
		}
	}
}
