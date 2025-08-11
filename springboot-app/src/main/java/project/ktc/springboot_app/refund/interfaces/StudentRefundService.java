package project.ktc.springboot_app.refund.interfaces;

import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.refund.dto.RefundRequestDto;
import project.ktc.springboot_app.refund.dto.RefundResponseDto;

public interface StudentRefundService {
    /**
     * Request a refund for a purchased course
     *
     * @param courseId         the course ID
     * @param refundRequestDto the refund request details
     * @return refund response
     */
    ResponseEntity<ApiResponse<RefundResponseDto>> requestRefund(
            String courseId,
            RefundRequestDto refundRequestDto);
}
