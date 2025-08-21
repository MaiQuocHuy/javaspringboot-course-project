package project.ktc.springboot_app.refund.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudinary.Api;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.earning.entity.InstructorEarning;
import project.ktc.springboot_app.earning.repositories.InstructorEarningRepository;
import project.ktc.springboot_app.enrollment.entity.Enrollment;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;
import project.ktc.springboot_app.payment.dto.AdminPaymentDetailResponseDto;
import project.ktc.springboot_app.payment.entity.Payment;
import project.ktc.springboot_app.refund.dto.AdminRefundDetailsResponseDto;
import project.ktc.springboot_app.refund.dto.AdminRefundResponseDto;
import project.ktc.springboot_app.refund.dto.AdminRefundStatusUpdateResponseDto;
import project.ktc.springboot_app.refund.dto.UpdateRefundStatusDto;
import project.ktc.springboot_app.refund.entity.Refund;
import project.ktc.springboot_app.refund.interfaces.AdminRefundService;
import project.ktc.springboot_app.refund.repositories.AdminRefundRepository;
import project.ktc.springboot_app.refund.repositories.RefundRepository;
import project.ktc.springboot_app.stripe.services.StripePaymentDetailsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminRefundServiceImp implements AdminRefundService {

    private final AdminRefundRepository adminRefundRepository;
    private final RefundRepository refundRepository;
    private final InstructorEarningRepository instructorEarningRepository;
    private final StripePaymentDetailsService stripePaymentDetailsService;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<AdminRefundResponseDto>>> getAllRefunds(Pageable pageable) {
        try {
            log.info("Admin retrieving all refunds with pagination: page={}, size={}", pageable.getPageNumber(),
                    pageable.getPageSize());
            Page<Refund> refunds = adminRefundRepository.findAllRefunds(pageable);

            List<AdminRefundResponseDto> refundDtos = refunds.getContent().stream()
                    .map(AdminRefundResponseDto::fromEntity)
                    .collect(Collectors.toList());

            PaginatedResponse<AdminRefundResponseDto> paginatedResponse = PaginatedResponse
                    .<AdminRefundResponseDto>builder()
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

            log.info("Retrieved {} refunds for admin (page {} of {})", refundDtos.size(), refunds.getNumber(),
                    refunds.getTotalPages());

            return ApiResponseUtil.success(paginatedResponse, "Refunds retrieved successfully!");

        } catch (Exception e) {
            log.error("Error retrieving all refunds for admin: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Error retrieving all refunds for admin");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<List<AdminRefundResponseDto>>> getAllRefunds() {
        try {
            log.info("Admin retrieving all refunds without pagination");

            List<Refund> refunds = adminRefundRepository.findAllRefunds();

            List<AdminRefundResponseDto> refundDtos = refunds.stream()
                    .map(AdminRefundResponseDto::fromEntity)
                    .collect(Collectors.toList());

            log.info("Retrieved {} refunds for admin", refundDtos.size());

            return ApiResponseUtil.success(refundDtos, "Refunds retrieved successfully!");

        } catch (Exception e) {
            log.error("Error retrieving all refunds for admin: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Error retrieving all refunds for admin");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<AdminRefundDetailsResponseDto>> getRefundDetail(String refundId) {
        try {
            log.info("Admin retrieving refund details for refund: {}", refundId);
            Optional<Refund> refundOpt = adminRefundRepository.findRefundByIdWithDetails(refundId);
            if (refundOpt.isEmpty()) {
                log.warn("Refund not found with ID: {}", refundId);
                return ApiResponseUtil.notFound("Refund not found");
            }
            Refund refund = refundOpt.get();
            AdminRefundDetailsResponseDto refundDetail;
            // Check if this is a Stripe payment and fetch additional details
            if (stripePaymentDetailsService.isStripePayment(refund.getPayment().getPaymentMethod())
                    && refund.getPayment().getSessionId() != null) {
                try {
                    var stripeData = stripePaymentDetailsService
                            .fetchPaymentDetails(refund.getPayment().getSessionId());
                    AdminRefundDetailsResponseDto.StripePaymentData adminStripeData = null;
                    if (stripeData != null) {
                        adminStripeData = AdminRefundDetailsResponseDto.StripePaymentData.builder()
                                .transactionId(stripeData.getTransactionId())
                                .receiptUrl(stripeData.getReceiptUrl())
                                .cardBrand(stripeData.getCardBrand())
                                .cardLast4(stripeData.getCardLast4())
                                .cardExpMonth(stripeData.getCardExpMonth())
                                .cardExpYear(stripeData.getCardExpYear())
                                .build();
                    }
                    refundDetail = AdminRefundDetailsResponseDto.fromEntityWithStripeData(refund, adminStripeData);

                } catch (Exception e) {
                    log.error("Error fetching Stripe payment details for refund: {}", refundId);
                    refundDetail = AdminRefundDetailsResponseDto.fromEntity(refund);
                }
            } else {
                refundDetail = AdminRefundDetailsResponseDto.fromEntity(refund);
                log.info("Refund {} is not a Stripe payment or has no session ID, using basic details", refundId);
            }
            log.info("Refund details retrieved successfully for refund ID: {}", refundId);
            return ApiResponseUtil.success(refundDetail, "Refund details retrieved successfully!");
        } catch (Exception e) {
            log.error("Error retrieving refund details for admin: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Error retrieving refund details for admin");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<AdminRefundStatusUpdateResponseDto>> updateRefundStatus(
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
            }

            // 6. Save the updated refund
            Refund savedRefund = refundRepository.save(refund);

            // 7. Build response
            AdminRefundStatusUpdateResponseDto response = AdminRefundStatusUpdateResponseDto.builder()
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
}
