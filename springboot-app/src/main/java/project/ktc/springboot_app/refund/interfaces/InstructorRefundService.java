package project.ktc.springboot_app.refund.interfaces;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.refund.dto.AdminRefundStatusUpdateResponseDto;
import project.ktc.springboot_app.refund.dto.InstructorRefundDetailsResponseDto;
import project.ktc.springboot_app.refund.dto.InstructorRefundResponseDto;
import project.ktc.springboot_app.refund.dto.UpdateRefundStatusDto;

public interface InstructorRefundService {

    /**
     * Get all payments with pagination for admin
     * 
     * @param pageable Pagination information
     * @return ResponseEntity containing paginated list of refunds
     */
    ResponseEntity<ApiResponse<PaginatedResponse<InstructorRefundResponseDto>>> getAllRefundsByInstructorId(
            Pageable pageable);

    /**
     * Get all refunds without pagination for admin
     * 
     * @return ResponseEntity containing list of all refunds
     */
    ResponseEntity<ApiResponse<List<InstructorRefundResponseDto>>> getAllRefundsByInstructorId();

    /**
     * Get refund details by ID for admin
     * 
     * @param refundId The refund ID to retrieve
     * @return ResponseEntity containing detailed refund information
     */
    ResponseEntity<ApiResponse<InstructorRefundDetailsResponseDto>> getRefundByIdAndInstructorIdWithDetails(
            String refundId);

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
