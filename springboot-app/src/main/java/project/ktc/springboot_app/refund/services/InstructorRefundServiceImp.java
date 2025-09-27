package project.ktc.springboot_app.refund.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.earning.entity.InstructorEarning;
import project.ktc.springboot_app.earning.repositories.InstructorEarningRepository;
import project.ktc.springboot_app.enrollment.entity.Enrollment;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;
import project.ktc.springboot_app.payment.entity.Payment;
import project.ktc.springboot_app.refund.dto.RefundStatusUpdateResponseDto;
import project.ktc.springboot_app.refund.dto.InstructorRefundDetailsResponseDto;
import project.ktc.springboot_app.refund.dto.InstructorRefundResponseDto;
import project.ktc.springboot_app.refund.dto.UpdateRefundStatusDto;
import project.ktc.springboot_app.refund.entity.Refund;
import project.ktc.springboot_app.refund.interfaces.InstructorRefundService;
import project.ktc.springboot_app.refund.repositories.InstructorRefundRepository;
import project.ktc.springboot_app.refund.repositories.RefundRepository;
import project.ktc.springboot_app.stripe.services.StripePaymentDetailsService;
import project.ktc.springboot_app.utils.SecurityUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstructorRefundServiceImp implements InstructorRefundService {
    private final InstructorRefundRepository instructorRefundRepository;
    private final StripePaymentDetailsService stripePaymentDetailsService;
    private final RefundRepository refundRepository;
    private final InstructorEarningRepository instructorEarningRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<InstructorRefundResponseDto>>> getAllRefundsByInstructorId(
            Pageable pageable) {
        try {
            String userId = SecurityUtil.getCurrentUserId();
            log.info("Fetching paginated refunds for instructor: {} by user: {}", userId);
            Page<Refund> refunds = instructorRefundRepository.findAllRefundsByInstructorId(userId, pageable);
            List<InstructorRefundResponseDto> refundDtos = refunds.getContent().stream()
                    .map(InstructorRefundResponseDto::fromEntity)
                    .collect(Collectors.toList());
            PaginatedResponse<InstructorRefundResponseDto> paginatedResponse = PaginatedResponse
                    .<InstructorRefundResponseDto>builder()
                    .content(refundDtos)
                    .page(PaginatedResponse.PageInfo.builder()
                            .number(refunds.getNumber())
                            .size(refunds.getSize())
                            .totalElements(refunds.getTotalElements())
                            .totalPages(refunds.getTotalPages())
                            .first(refunds.isFirst())
                            .last(refunds.isLast())
                            .build())
                    .build();
            // log.info("User ID: {}, Refund DTOs: {}, Paginated Response: {}", userId,
            // refundDtos, paginatedResponse);
            return ApiResponseUtil.success(paginatedResponse, "Fetched all refunds for instructor");
        } catch (Exception e) {
            log.error("Error fetching refunds for instructor: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("An error occurred while fetching refunds");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<List<InstructorRefundResponseDto>>> getAllRefundsByInstructorId() {
        try {
            String userId = SecurityUtil.getCurrentUserId();
            log.info("Fetching all refunds for instructor: {} by user: {}", userId);

            List<Refund> refunds = instructorRefundRepository.findAllRefundsByInstructorId(userId);

            List<InstructorRefundResponseDto> refundDtos = refunds.stream().map(InstructorRefundResponseDto::fromEntity)
                    .collect(Collectors.toList());
            log.info("User ID: {}, Refunds: {}, Refund DTOs: {}", userId, refunds, refundDtos);
            return ApiResponseUtil.success(refundDtos, "Fetched all refunds for instructor");
        } catch (Exception e) {
            log.error("Error fetching all refunds for instructor: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("An error occurred while fetching refunds");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<InstructorRefundDetailsResponseDto>> getRefundByIdAndInstructorIdWithDetails(
            String refundId) {
        try {
            String userId = SecurityUtil.getCurrentUserId();
            log.info("Fetching refund details for refund ID: {} and instructor ID: {} by user: {}", refundId,
                    userId);
            Optional<Refund> refundOpt = instructorRefundRepository.findRefundByIdAndInstructorIdWithDetails(refundId,
                    userId);
            if (refundOpt.isEmpty()) {
                log.warn("Refund not found with ID: {} for instructor ID: {}", refundId, userId);
                return ApiResponseUtil.notFound("Refund not found");
            }
            Refund refund = refundOpt.get();
            InstructorRefundDetailsResponseDto refundDetail;
            if (stripePaymentDetailsService.isStripePayment(refund.getPayment().getPaymentMethod())
                    && refund.getPayment().getSessionId() != null) {
                try {
                    var stripeData = stripePaymentDetailsService
                            .fetchPaymentDetails(refund.getPayment().getSessionId());
                    InstructorRefundDetailsResponseDto.StripePaymentData instructorStripeData = null;
                    if (stripeData != null) {
                        instructorStripeData = InstructorRefundDetailsResponseDto.StripePaymentData.builder()
                                .transactionId(stripeData.getTransactionId())
                                .receiptUrl(stripeData.getReceiptUrl())
                                .build();
                    }
                    refundDetail = InstructorRefundDetailsResponseDto.fromEntityWithStripeData(refund,
                            instructorStripeData);
                } catch (Exception e) {
                    log.error("Error fetching Stripe payment details for session ID: {}",
                            refund.getPayment().getSessionId(), e);
                    refundDetail = InstructorRefundDetailsResponseDto.fromEntity(refund);
                }
            } else {
                refundDetail = InstructorRefundDetailsResponseDto.fromEntity(refund);
                log.info("Non-Stripe payment method or missing session ID for refund ID: {}", refundId);
            }
            log.info("Refund details fetched successfully for refund ID: {}", refundId);
            return ApiResponseUtil.success(refundDetail, "Fetched refund details successfully");
        } catch (Exception e) {
            log.error("Error fetching refund details for refund ID: {}", refundId, e);
            return ApiResponseUtil.internalServerError("An error occurred while fetching refund details");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<RefundStatusUpdateResponseDto>> updateRefundStatus(
            String refundId, UpdateRefundStatusDto updateDto) {

        log.info("Processing refund status update for refund ID: {} to status: {}", refundId, updateDto.getStatus());

        try {
            // 1. Find the refund
            Optional<Refund> refundOpt = refundRepository.findById(refundId);
            if (refundOpt.isEmpty()) {
                log.warn("Refund not found with ID: {}", refundId);
                return ApiResponseUtil.notFound("Refund not found");
            }
            Refund refund = refundOpt.get();

            // 2. Check if refund is in PENDING state
            if (!Refund.RefundStatus.PENDING.equals(refund.getStatus())) {
                log.warn("Refund {} is not in PENDING state. Current status: {}", refundId, refund.getStatus());
                return ApiResponseUtil.badRequest("Refund status can only be updated from PENDING state");
            }

            // 3. Check if related payment has been paid out to instructor
            Payment payment = refund.getPayment();
            Optional<InstructorEarning> earningOpt = instructorEarningRepository.findByPaymentId(payment.getId());
            if (earningOpt.isPresent()) {
                InstructorEarning earning = earningOpt.get();
                if (InstructorEarning.EarningStatus.PAID.equals(earning.getStatus())) {
                    log.warn(
                            "Cannot update refund status - payment has already been paid out to instructor. Earning ID: {}",
                            earning.getId());
                    return ApiResponseUtil
                            .badRequest("Cannot process refund - payment has already been paid out to instructor");
                }
            }

            // 4. Update refund status
            Refund.RefundStatus newStatus = Refund.RefundStatus.valueOf(updateDto.getStatus());
            if (Refund.RefundStatus.FAILED.equals(newStatus)) {
                if (updateDto.getRejectedReason() == null || updateDto.getRejectedReason().isBlank()) {
                    log.warn("Failed! Rejected refund requires a reason");
                    return ApiResponseUtil.badRequest("Failed! Rejected refund must have a reason");
                }
                refund.setRejectedReason(updateDto.getRejectedReason());
            }
            refund.setStatus(newStatus);
            refund.setProcessedAt(LocalDateTime.now());

            // 5. If status is COMPLETED, block instructor payout and remove enrollment
            if (Refund.RefundStatus.COMPLETED.equals(newStatus)) {
                // Block instructor earning if exists
                if (earningOpt.isPresent()) {
                    InstructorEarning earning = earningOpt.get();
                    // Block the earning by setting it to a blocked status (we can use PENDING to
                    // prevent payout)
                    earning.setStatus(InstructorEarning.EarningStatus.PENDING);
                    instructorEarningRepository.save(earning);
                    log.info("Blocked instructor earning {} due to refund completion", earning.getId());
                }

                // Remove enrollment for the refunded course
                try {
                    String userId = payment.getUser().getId();
                    String courseId = payment.getCourse().getId();

                    Optional<Enrollment> enrollmentOpt = enrollmentRepository
                            .findByUserIdAndCourseId(userId, courseId);

                    if (enrollmentOpt.isPresent()) {
                        enrollmentRepository.delete(enrollmentOpt.get());
                        log.info("Removed enrollment for user {} from course {} due to refund completion",
                                userId, courseId);
                    } else {
                        log.warn("No enrollment found for user {} in course {} to remove",
                                userId, courseId);
                    }
                } catch (Exception e) {
                    log.error("Error removing enrollment for refund {}: {}", refundId, e.getMessage(), e);
                    // Continue with refund processing even if enrollment removal fails
                }

                // Update payment status to REFUNDED
                try {
                    payment.setStatus(Payment.PaymentStatus.REFUNDED);
                    // Note: payment will be saved through the refund relationship, but we can save
                    // explicitly if needed
                    log.info("Updated payment {} status to REFUNDED due to refund completion", payment.getId());
                } catch (Exception e) {
                    log.error("Error updating payment status to REFUNDED for payment {}: {}",
                            payment.getId(), e.getMessage(), e);
                    // Continue with refund processing even if payment status update fails
                }
            }

            // 6. Save the updated refund
            Refund savedRefund = refundRepository.save(refund);

            // 7. Build response
            RefundStatusUpdateResponseDto response = RefundStatusUpdateResponseDto.builder()
                    .id(savedRefund.getId())
                    .paymentId(savedRefund.getPayment().getId())
                    .amount(savedRefund.getAmount())
                    .status(savedRefund.getStatus().name())
                    .reason(savedRefund.getReason())
                    .rejectedReason(savedRefund.getRejectedReason())
                    .build();

            log.info("Refund status updated successfully for refund ID: {} to status: {}", refundId, newStatus);
            return ApiResponseUtil.success(response, "Refund status updated successfully");

        } catch (IllegalArgumentException e) {
            log.error("Invalid status value: {}", updateDto.getStatus());
            return ApiResponseUtil.badRequest("Invalid status value");
        } catch (Exception e) {
            log.error("Error updating refund status for refund ID {}: {}", refundId, e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to update refund status. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<InstructorRefundResponseDto>>> getAllRefundsByInstructorId(
            String search,
            Refund.RefundStatus status,
            String fromDate,
            String toDate,
            Pageable pageable) {
        try {
            String userId = SecurityUtil.getCurrentUserId();
            log.info(
                    "Instructor retrieving refunds with filters - search: {}, status: {}, fromDate: {}, toDate: {}, page: {}, size: {} for instructor: {}",
                    search, status, fromDate, toDate, pageable.getPageNumber(), pageable.getPageSize(), userId);

            // Validate and parse dates
            LocalDate fromLocalDate = null;
            LocalDate toLocalDate = null;

            if (fromDate != null && !fromDate.trim().isEmpty()) {
                try {
                    fromLocalDate = LocalDate.parse(fromDate);
                } catch (DateTimeParseException e) {
                    log.warn("Invalid fromDate format: {}", fromDate);
                    return ApiResponseUtil.badRequest("Invalid fromDate format. Use YYYY-MM-DD");
                }
            }

            if (toDate != null && !toDate.trim().isEmpty()) {
                try {
                    toLocalDate = LocalDate.parse(toDate);
                } catch (DateTimeParseException e) {
                    log.warn("Invalid toDate format: {}", toDate);
                    return ApiResponseUtil.badRequest("Invalid toDate format. Use YYYY-MM-DD");
                }
            }

            // Validate date range
            if (fromLocalDate != null && toLocalDate != null && fromLocalDate.isAfter(toLocalDate)) {
                log.warn("Invalid date range: fromDate {} is after toDate {}", fromDate, toDate);
                return ApiResponseUtil.badRequest("fromDate cannot be after toDate");
            }

            Page<Refund> refunds = instructorRefundRepository.findAllRefundsByInstructorIdWithFilter(
                    userId, search, status, fromLocalDate, toLocalDate, pageable);

            List<InstructorRefundResponseDto> refundDtos = refunds.getContent().stream()
                    .map(InstructorRefundResponseDto::fromEntity)
                    .collect(Collectors.toList());

            PaginatedResponse<InstructorRefundResponseDto> paginatedResponse = PaginatedResponse
                    .<InstructorRefundResponseDto>builder()
                    .content(refundDtos)
                    .page(PaginatedResponse.PageInfo.builder()
                            .number(refunds.getNumber())
                            .size(refunds.getSize())
                            .totalElements(refunds.getTotalElements())
                            .totalPages(refunds.getTotalPages())
                            .first(refunds.isFirst())
                            .last(refunds.isLast())
                            .build())
                    .build();

            log.info("Retrieved {} filtered refunds for instructor {} (page {} of {})",
                    refundDtos.size(), userId, refunds.getNumber(), refunds.getTotalPages());

            return ApiResponseUtil.success(paginatedResponse, "Refunds retrieved successfully!");

        } catch (Exception e) {
            log.error("Error retrieving filtered refunds for instructor: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Error retrieving filtered refunds for instructor");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<List<InstructorRefundResponseDto>>> getAllRefundsByInstructorId(
            String search,
            Refund.RefundStatus status,
            String fromDate,
            String toDate) {
        try {
            String userId = SecurityUtil.getCurrentUserId();
            log.info(
                    "Instructor retrieving all refunds with filters without pagination - search: {}, status: {}, fromDate: {}, toDate: {} for instructor: {}",
                    search, status, fromDate, toDate, userId);

            // Validate and parse dates
            LocalDate fromLocalDate = null;
            LocalDate toLocalDate = null;

            if (fromDate != null && !fromDate.trim().isEmpty()) {
                try {
                    fromLocalDate = LocalDate.parse(fromDate);
                } catch (DateTimeParseException e) {
                    log.warn("Invalid fromDate format: {}", fromDate);
                    return ApiResponseUtil.badRequest("Invalid fromDate format. Use YYYY-MM-DD");
                }
            }

            if (toDate != null && !toDate.trim().isEmpty()) {
                try {
                    toLocalDate = LocalDate.parse(toDate);
                } catch (DateTimeParseException e) {
                    log.warn("Invalid toDate format: {}", toDate);
                    return ApiResponseUtil.badRequest("Invalid toDate format. Use YYYY-MM-DD");
                }
            }

            // Validate date range
            if (fromLocalDate != null && toLocalDate != null && fromLocalDate.isAfter(toLocalDate)) {
                log.warn("Invalid date range: fromDate {} is after toDate {}", fromDate, toDate);
                return ApiResponseUtil.badRequest("fromDate cannot be after toDate");
            }

            List<Refund> refunds = instructorRefundRepository.findAllRefundsByInstructorIdWithFilter(
                    userId, search, status, fromLocalDate, toLocalDate);

            List<InstructorRefundResponseDto> refundDtos = refunds.stream()
                    .map(InstructorRefundResponseDto::fromEntity)
                    .collect(Collectors.toList());

            log.info("Retrieved {} filtered refunds for instructor {}", refundDtos.size(), userId);

            return ApiResponseUtil.success(refundDtos, "Refunds retrieved successfully!");

        } catch (Exception e) {
            log.error("Error retrieving filtered refunds for instructor: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Error retrieving filtered refunds for instructor");
        }
    }
}