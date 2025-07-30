package project.ktc.springboot_app.earning.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.earning.dto.EarningDetailResponseDto;
import project.ktc.springboot_app.earning.dto.EarningResponseDto;
import project.ktc.springboot_app.earning.dto.EarningSummaryDto;
import project.ktc.springboot_app.earning.dto.EarningsWithSummaryDto;
import project.ktc.springboot_app.earning.entity.InstructorEarning;
import project.ktc.springboot_app.earning.interfaces.InstructorEarningService;
import project.ktc.springboot_app.earning.repositories.InstructorEarningRepository;
import project.ktc.springboot_app.utils.SecurityUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorEarningServiceImp implements InstructorEarningService {

    private final InstructorEarningRepository instructorEarningRepository;

    @Override
    public ResponseEntity<ApiResponse<EarningsWithSummaryDto>> getEarnings(
            String courseId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable) {

        log.info(
                "Fetching earnings for instructor with filters - courseId: {}, dateFrom: {}, dateTo: {}, page: {}",
                courseId, dateFrom, dateTo, pageable);

        try {
            String currentUserId = SecurityUtil.getCurrentUserId();

            // Fetch earnings with pagination and filters
            Page<InstructorEarning> earningsPage = instructorEarningRepository.findEarningsWithFilters(
                    currentUserId, courseId, dateFrom, dateTo, pageable);

            // Convert to DTOs
            List<EarningResponseDto> earningDtos = earningsPage.getContent().stream()
                    .map(this::mapToEarningResponseDto)
                    .collect(Collectors.toList());

            // Calculate summary
            EarningSummaryDto summary = calculateSummary(currentUserId, courseId, dateFrom, dateTo);

            // Create paginated earnings using PaginatedResponse
            PaginatedResponse<EarningResponseDto> paginatedEarnings = PaginatedResponse.<EarningResponseDto>builder()
                    .content(earningDtos)
                    .page(PaginatedResponse.PageInfo.builder()
                            .number(earningsPage.getNumber())
                            .size(earningsPage.getSize())
                            .totalElements(earningsPage.getTotalElements())
                            .totalPages(earningsPage.getTotalPages())
                            .first(earningsPage.isFirst())
                            .last(earningsPage.isLast())
                            .build())
                    .build();

            // Build final response with earnings and summary
            EarningsWithSummaryDto responseData = EarningsWithSummaryDto.builder()
                    .earnings(paginatedEarnings)
                    .summary(summary)
                    .build();

            log.info("Successfully retrieved {} earnings for instructor {}", earningDtos.size(), currentUserId);

            return ApiResponseUtil.success(responseData, "Earnings retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving earnings for instructor: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve earnings. Please try again later.");
        }
    }

    private EarningResponseDto mapToEarningResponseDto(InstructorEarning earning) {
        // Calculate platform cut (assuming 20% platform fee)
        BigDecimal platformCutPercentage = new BigDecimal("0.20");
        BigDecimal totalAmount = earning.getPayment().getAmount();
        BigDecimal platformCut = totalAmount.multiply(platformCutPercentage);
        BigDecimal instructorShare = totalAmount.subtract(platformCut);

        return EarningResponseDto.builder()
                .id(earning.getId())
                .courseId(earning.getCourse().getId())
                .courseTitle(earning.getCourse().getTitle())
                .courseThumbnailUrl(earning.getCourse().getThumbnailUrl())
                .paymentId(earning.getPayment().getId())
                .amount(totalAmount)
                .platformCut(platformCut)
                .instructorShare(instructorShare)
                .status(earning.getStatus().name())
                .paidAt(earning.getPaidAt())
                .build();
    }

    private EarningSummaryDto calculateSummary(String instructorId, String courseId,
            LocalDateTime dateFrom, LocalDateTime dateTo) {

        // Get filtered totals if filters are applied, otherwise get overall totals
        BigDecimal totalEarnings;
        Long totalTransactions;

        if (hasFilters(courseId, dateFrom, dateTo)) {
            totalEarnings = instructorEarningRepository.getTotalEarningsWithFilters(
                    instructorId, courseId, dateFrom, dateTo);
            totalTransactions = instructorEarningRepository.getTotalTransactionsWithFilters(
                    instructorId, courseId, dateFrom, dateTo);
        } else {
            totalEarnings = instructorEarningRepository.getTotalEarningsByInstructor(instructorId);
            totalTransactions = instructorEarningRepository.getTotalTransactionsByInstructor(instructorId);
        }

        // Get status-specific amounts (overall, not filtered)
        BigDecimal paidAmount = instructorEarningRepository.getPaidAmountByInstructor(instructorId);

        return EarningSummaryDto.builder()
                .totalEarnings(totalEarnings != null ? totalEarnings : BigDecimal.ZERO)
                .paidAmount(paidAmount != null ? paidAmount : BigDecimal.ZERO)
                .totalTransactions(totalTransactions != null ? totalTransactions : 0L)
                .build();
    }

    private boolean hasFilters(String courseId, LocalDateTime dateFrom, LocalDateTime dateTo) {
        return courseId != null || dateFrom != null || dateTo != null;
    }

    @Override
    public ResponseEntity<ApiResponse<EarningDetailResponseDto>> getEarningDetails(String earningId) {
        log.info("Fetching earning details for earningId: {}", earningId);

        try {
            String currentUserId = SecurityUtil.getCurrentUserId();

            // Find earning by ID and instructor ID to ensure ownership
            InstructorEarning earning = instructorEarningRepository.findByIdAndInstructorId(earningId, currentUserId)
                    .orElse(null);

            if (earning == null) {
                log.warn("Earning not found or access denied for earningId: {} and instructorId: {}",
                        earningId, currentUserId);
                return ApiResponseUtil.notFound("Earning record not found or access denied");
            }

            // Map to detailed DTO
            EarningDetailResponseDto responseData = mapToEarningDetailResponseDto(earning);

            log.info("Successfully retrieved earning details for earningId: {}", earningId);
            return ApiResponseUtil.success(responseData, "Earning details retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving earning details for earningId: {}: {}", earningId, e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve earning details. Please try again later.");
        }
    }

    private EarningDetailResponseDto mapToEarningDetailResponseDto(InstructorEarning earning) {
        // Calculate platform cut (assuming 20% platform fee)
        BigDecimal platformCutPercentage = new BigDecimal("0.20");
        BigDecimal totalAmount = earning.getPayment().getAmount();
        BigDecimal platformCut = totalAmount.multiply(platformCutPercentage);
        BigDecimal instructorShare = totalAmount.subtract(platformCut);

        return EarningDetailResponseDto.builder()
                .id(earning.getId())
                .courseId(earning.getCourse().getId())
                .courseTitle(earning.getCourse().getTitle())
                .courseDescription(earning.getCourse().getDescription())
                .courseThumbnailUrl(earning.getCourse().getThumbnailUrl())
                .paymentId(earning.getPayment().getId())
                .amount(totalAmount)
                .platformCut(platformCut)
                .platformCutPercentage(20) // 20% platform fee
                .instructorShare(instructorShare)
                .status(earning.getStatus().name())
                .paidAt(earning.getPaidAt())
                .build();
    }
}
