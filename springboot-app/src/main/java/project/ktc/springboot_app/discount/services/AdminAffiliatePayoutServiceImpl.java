package project.ktc.springboot_app.discount.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.discount.dto.request.BulkPayoutActionRequestDto;
import project.ktc.springboot_app.discount.dto.response.*;
import project.ktc.springboot_app.discount.entity.AffiliatePayout;
import project.ktc.springboot_app.discount.enums.PayoutStatus;
import project.ktc.springboot_app.discount.interfaces.AdminAffiliatePayoutService;
import project.ktc.springboot_app.discount.repositories.AffiliatePayoutRepository;
import project.ktc.springboot_app.common.exception.ResourceNotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAffiliatePayoutServiceImpl implements AdminAffiliatePayoutService {

    private final AffiliatePayoutRepository affiliatePayoutRepository;

    @Override
    public Page<AffiliatePayoutResponseDto> getAffiliatePayouts(
            Pageable pageable,
            Long userId,
            PayoutStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount) {

        try {
            // Get all payouts first (since repository doesn't support specifications)
            List<AffiliatePayout> allPayouts = affiliatePayoutRepository.findAll();

            // Apply filters
            List<AffiliatePayout> filteredPayouts = allPayouts.stream()
                    .filter(payout -> userId == null ||
                            (payout.getReferredByUser() != null &&
                                    payout.getReferredByUser().getId().equals(userId.toString())))
                    .filter(payout -> status == null || payout.getPayoutStatus() == status)
                    .filter(payout -> startDate == null ||
                            (payout.getCreatedAt() != null &&
                                    !payout.getCreatedAt().isBefore(startDate)))
                    .filter(payout -> endDate == null ||
                            (payout.getCreatedAt() != null &&
                                    !payout.getCreatedAt().isAfter(endDate)))
                    .filter(payout -> minAmount == null ||
                            payout.getCommissionAmount().compareTo(minAmount) >= 0)
                    .filter(payout -> maxAmount == null ||
                            payout.getCommissionAmount().compareTo(maxAmount) <= 0)
                    .collect(Collectors.toList());

            // Convert to DTOs
            List<AffiliatePayoutResponseDto> payoutDtos = filteredPayouts.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());

            // Apply pagination manually
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), payoutDtos.size());

            List<AffiliatePayoutResponseDto> pageContent = payoutDtos.subList(start, end);

            return new PageImpl<>(pageContent, pageable, payoutDtos.size());

        } catch (Exception e) {
            log.error("Error fetching affiliate payouts", e);
            throw new RuntimeException("Failed to fetch affiliate payouts: " + e.getMessage());
        }
    }

    @Override
    public AffiliateStatisticsResponseDto getAffiliateStatistics() {
        try {
            List<AffiliatePayout> allPayouts = affiliatePayoutRepository.findAll();

            BigDecimal totalCommissionAmount = BigDecimal.ZERO;
            BigDecimal paidCommissionAmount = BigDecimal.ZERO;
            BigDecimal pendingCommissionAmount = BigDecimal.ZERO;
            BigDecimal cancelledCommissionAmount = BigDecimal.ZERO;

            long totalPayouts = allPayouts.size();
            long paidPayouts = 0;
            long pendingPayouts = 0;
            long cancelledPayouts = 0;

            for (AffiliatePayout payout : allPayouts) {
                totalCommissionAmount = totalCommissionAmount.add(payout.getCommissionAmount());

                switch (payout.getPayoutStatus()) {
                    case PAID:
                        paidCommissionAmount = paidCommissionAmount.add(payout.getCommissionAmount());
                        paidPayouts++;
                        break;
                    case PENDING:
                        pendingCommissionAmount = pendingCommissionAmount.add(payout.getCommissionAmount());
                        pendingPayouts++;
                        break;
                    case CANCELLED:
                        cancelledCommissionAmount = cancelledCommissionAmount.add(payout.getCommissionAmount());
                        cancelledPayouts++;
                        break;
                }
            }

            return AffiliateStatisticsResponseDto.builder()
                    .totalPayouts(totalPayouts)
                    .paidPayouts(paidPayouts)
                    .pendingPayouts(pendingPayouts)
                    .cancelledPayouts(cancelledPayouts)
                    .totalCommissionAmount(totalCommissionAmount)
                    .paidCommissionAmount(paidCommissionAmount)
                    .pendingCommissionAmount(pendingCommissionAmount)
                    .cancelledCommissionAmount(cancelledCommissionAmount)
                    .build();
        } catch (Exception e) {
            log.error("Error calculating affiliate statistics", e);
            throw new RuntimeException("Failed to calculate affiliate statistics: " + e.getMessage());
        }
    }

    @Override
    public AffiliatePayoutResponseDto getPayoutById(Long id) {
        AffiliatePayout payout = affiliatePayoutRepository.findById(id.toString())
                .orElseThrow(() -> new ResourceNotFoundException("Affiliate payout not found with id: " + id));

        return convertToResponseDto(payout);
    }

    @Override
    @Transactional
    public AffiliatePayoutResponseDto markPayoutAsPaid(Long id) {
        AffiliatePayout payout = affiliatePayoutRepository.findById(id.toString())
                .orElseThrow(() -> new ResourceNotFoundException("Affiliate payout not found with id: " + id));

        if (payout.getPayoutStatus() != PayoutStatus.PENDING) {
            throw new IllegalStateException("Only pending payouts can be marked as paid");
        }

        payout.markAsPaid(); // Use the entity method

        AffiliatePayout savedPayout = affiliatePayoutRepository.save(payout);

        log.info("Marked payout {} as paid", id);
        return convertToResponseDto(savedPayout);
    }

    @Override
    @Transactional
    public AffiliatePayoutResponseDto cancelPayout(Long id, String reason) {
        AffiliatePayout payout = affiliatePayoutRepository.findById(id.toString())
                .orElseThrow(() -> new ResourceNotFoundException("Affiliate payout not found with id: " + id));

        if (payout.getPayoutStatus() == PayoutStatus.PAID) {
            throw new IllegalStateException("Cannot cancel a payout that has already been paid");
        }

        payout.markAsCancelled(); // Use the entity method

        AffiliatePayout savedPayout = affiliatePayoutRepository.save(payout);

        log.info("Cancelled payout {} with reason: {}", id, reason);
        return convertToResponseDto(savedPayout);
    }

    @Override
    @Transactional
    public String bulkActionPayouts(BulkPayoutActionRequestDto bulkRequest) {
        try {
            List<String> payoutIds = bulkRequest.getPayoutIds(); // Use String as per DTO
            BulkPayoutActionRequestDto.BulkPayoutAction action = bulkRequest.getAction();

            int totalRequested = payoutIds.size();
            int totalProcessed = 0;
            int totalFailed = 0;
            List<String> failedReasons = new ArrayList<>();

            for (String payoutId : payoutIds) {
                try {
                    AffiliatePayout payout = affiliatePayoutRepository.findById(payoutId)
                            .orElseThrow(() -> new ResourceNotFoundException("Payout not found: " + payoutId));

                    switch (action) {
                        case MARK_PAID:
                            if (payout.getPayoutStatus() == PayoutStatus.PENDING) {
                                payout.markAsPaid();
                                affiliatePayoutRepository.save(payout);
                                totalProcessed++;
                            } else {
                                failedReasons.add("Payout " + payoutId + ": Not in pending status");
                                totalFailed++;
                            }
                            break;

                        case CANCEL:
                            if (payout.getPayoutStatus() != PayoutStatus.PAID) {
                                payout.markAsCancelled();
                                affiliatePayoutRepository.save(payout);
                                totalProcessed++;
                            } else {
                                failedReasons.add("Payout " + payoutId + ": Already paid, cannot cancel");
                                totalFailed++;
                            }
                            break;

                        default:
                            failedReasons.add("Payout " + payoutId + ": Unknown action " + action);
                            totalFailed++;
                    }
                } catch (Exception e) {
                    failedReasons.add("Payout " + payoutId + ": " + e.getMessage());
                    totalFailed++;
                }
            }

            BulkPayoutActionResultDto result = BulkPayoutActionResultDto.builder()
                    .action(action.toString())
                    .totalRequested(totalRequested)
                    .totalProcessed(totalProcessed)
                    .totalFailed(totalFailed)
                    .failedReasons(failedReasons)
                    .summary(String.format("Bulk %s completed: %d processed, %d failed out of %d requested",
                            action, totalProcessed, totalFailed, totalRequested))
                    .build();

            log.info("Bulk action {} completed: {}", action, result.getSummary());
            return result.getSummary();

        } catch (Exception e) {
            log.error("Error performing bulk action on payouts", e);
            throw new RuntimeException("Failed to perform bulk action: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportPayouts(
            Long userId,
            PayoutStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount) {

        try {
            // Get filtered payouts using the same logic as getAffiliatePayouts
            List<AffiliatePayout> allPayouts = affiliatePayoutRepository.findAll();

            List<AffiliatePayout> filteredPayouts = allPayouts.stream()
                    .filter(payout -> userId == null ||
                            (payout.getReferredByUser() != null &&
                                    payout.getReferredByUser().getId().equals(userId.toString())))
                    .filter(payout -> status == null || payout.getPayoutStatus() == status)
                    .filter(payout -> startDate == null ||
                            (payout.getCreatedAt() != null &&
                                    !payout.getCreatedAt().isBefore(startDate)))
                    .filter(payout -> endDate == null ||
                            (payout.getCreatedAt() != null &&
                                    !payout.getCreatedAt().isAfter(endDate)))
                    .filter(payout -> minAmount == null ||
                            payout.getCommissionAmount().compareTo(minAmount) >= 0)
                    .filter(payout -> maxAmount == null ||
                            payout.getCommissionAmount().compareTo(maxAmount) <= 0)
                    .collect(Collectors.toList());

            return generateCsvBytes(filteredPayouts);

        } catch (Exception e) {
            log.error("Error exporting payouts", e);
            throw new RuntimeException("Failed to export payouts: " + e.getMessage());
        }
    }

    private AffiliatePayoutResponseDto convertToResponseDto(AffiliatePayout payout) {
        return AffiliatePayoutResponseDto.builder()
                .id(payout.getId())
                .referredByUser(
                        payout.getReferredByUser() != null ? AffiliatePayoutResponseDto.ReferredByUserDto.builder()
                                .id(payout.getReferredByUser().getId())
                                .name(payout.getReferredByUser().getName())
                                .email(payout.getReferredByUser().getEmail())
                                .build() : null)
                .course(payout.getCourse() != null ? AffiliatePayoutResponseDto.CourseDto.builder()
                        .id(payout.getCourse().getId())
                        .name(payout.getCourse().getTitle())
                        .build() : null)
                .discountUsage(payout.getDiscountUsage() != null ? AffiliatePayoutResponseDto.DiscountUsageDto.builder()
                        .id(payout.getDiscountUsage().getId())
                        .discount(AffiliatePayoutResponseDto.DiscountUsageDto.DiscountDto.builder()
                                .code(payout.getDiscountUsage().getDiscount() != null
                                        ? payout.getDiscountUsage().getDiscount().getCode()
                                        : null)
                                .type(payout.getDiscountUsage().getDiscount() != null
                                        ? payout.getDiscountUsage().getDiscount().getType().toString()
                                        : null)
                                .build())
                        .build() : null)
                .commissionPercent(payout.getCommissionPercent())
                .commissionAmount(payout.getCommissionAmount())
                .payoutStatus(payout.getPayoutStatus())
                .paidAt(payout.getPaidAt())
                .cancelledAt(payout.getCancelledAt())
                .createdAt(payout.getCreatedAt())
                .updatedAt(payout.getUpdatedAt())
                .build();
    }

    private String getCourseTitle(Course course) {
        if (course == null) {
            return "Unknown Course";
        }
        return course.getTitle();
    }

    private byte[] generateCsvBytes(List<AffiliatePayout> payouts) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

        // Write BOM for proper UTF-8 encoding in Excel
        writer.write('\ufeff');

        // Write CSV header
        writer.write("ID,User ID,Course ID,Course Title,Commission %,Commission Amount,Status,Paid At,Created At\n");

        // Write data rows
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (AffiliatePayout payout : payouts) {
            writer.write(String.format("%s,%s,%s,\"%s\",%.2f,%.2f,%s,%s,%s\n",
                    payout.getId(),
                    payout.getReferredByUser() != null ? payout.getReferredByUser().getId() : "",
                    payout.getCourse() != null ? payout.getCourse().getId() : "",
                    getCourseTitle(payout.getCourse()).replace("\"", "\"\""), // Escape quotes
                    payout.getCommissionPercent(),
                    payout.getCommissionAmount(),
                    payout.getPayoutStatus(),
                    payout.getPaidAt() != null ? payout.getPaidAt().format(formatter) : "",
                    payout.getCreatedAt() != null ? payout.getCreatedAt().format(formatter) : ""));
        }

        writer.flush();
        writer.close();

        return outputStream.toByteArray();
    }
}
