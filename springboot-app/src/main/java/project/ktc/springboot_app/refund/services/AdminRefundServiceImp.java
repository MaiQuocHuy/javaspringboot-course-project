package project.ktc.springboot_app.refund.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.earning.entity.InstructorEarning;
import project.ktc.springboot_app.earning.repositories.InstructorEarningRepository;
import project.ktc.springboot_app.payment.entity.Payment;
import project.ktc.springboot_app.refund.dto.AdminRefundStatusUpdateResponseDto;
import project.ktc.springboot_app.refund.dto.UpdateRefundStatusDto;
import project.ktc.springboot_app.refund.entity.Refund;
import project.ktc.springboot_app.refund.interfaces.AdminRefundService;
import project.ktc.springboot_app.refund.repositories.RefundRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminRefundServiceImp implements AdminRefundService {

    private final RefundRepository refundRepository;
    private final InstructorEarningRepository instructorEarningRepository;

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
            refund.setStatus(newStatus);
            refund.setProcessedAt(LocalDateTime.now());

            // 5. If status is COMPLETED, block instructor payout by updating earning status
            if (Refund.RefundStatus.COMPLETED.equals(newStatus) && earningOpt.isPresent()) {
                InstructorEarning earning = earningOpt.get();
                // Block the earning by setting it to a blocked status (we can use PENDING to
                // prevent payout)
                earning.setStatus(InstructorEarning.EarningStatus.PENDING);
                instructorEarningRepository.save(earning);
                log.info("Blocked instructor earning {} due to refund completion", earning.getId());
            }

            // 6. Save the updated refund
            Refund savedRefund = refundRepository.save(refund);

            // 7. Build response
            AdminRefundStatusUpdateResponseDto response = AdminRefundStatusUpdateResponseDto.builder()
                    .id(savedRefund.getId())
                    .paymentId(savedRefund.getPayment().getId())
                    .amount(savedRefund.getAmount())
                    .status(savedRefund.getStatus().name())
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
