package project.ktc.springboot_app.refund.interfaces;

import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.refund.dto.AdminRefundStatusUpdateResponseDto;
import project.ktc.springboot_app.refund.dto.UpdateRefundStatusDto;

public interface AdminRefundService {

    /**
     * Updates the status of a refund by ID
     * 
     * @param refundId  the ID of the refund to update
     * @param updateDto the update request containing the new status
     * @return ResponseEntity with the update result
     */
    ResponseEntity<ApiResponse<AdminRefundStatusUpdateResponseDto>> updateRefundStatus(
            String refundId, UpdateRefundStatusDto updateDto);
}
