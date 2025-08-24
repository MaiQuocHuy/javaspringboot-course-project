package project.ktc.springboot_app.refund.interfaces;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.refund.dto.AdminRefundDetailsResponseDto;
import project.ktc.springboot_app.refund.dto.AdminRefundResponseDto;
import project.ktc.springboot_app.refund.dto.AdminRefundStatisticsResponseDto;
import project.ktc.springboot_app.refund.dto.AdminRefundStatusUpdateResponseDto;
import project.ktc.springboot_app.refund.dto.UpdateRefundStatusDto;

public interface AdminRefundService {

    /**
     * Get all payments with pagination for admin
     * 
     * @param pageable Pagination information
     * @return ResponseEntity containing paginated list of refunds
     */
    ResponseEntity<ApiResponse<PaginatedResponse<AdminRefundResponseDto>>> getAllRefunds(Pageable pageable);

    /**
     * Get all refunds without pagination for admin
     * 
     * @return ResponseEntity containing list of all refunds
     */
    ResponseEntity<ApiResponse<List<AdminRefundResponseDto>>> getAllRefunds();

    /**
     * Get refund details by ID for admin
     * 
     * @param refundId The refund ID to retrieve
     * @return ResponseEntity containing detailed refund information
     */
    ResponseEntity<ApiResponse<AdminRefundDetailsResponseDto>> getRefundDetail(String refundId);

    /**
     * Updates the status of a refund by ID
     * 
     * @param refundId  the ID of the refund to update
     * @param updateDto the update request containing the new status
     * @return ResponseEntity with the update result
     */
    ResponseEntity<ApiResponse<AdminRefundStatusUpdateResponseDto>> updateRefundStatus(
            String refundId, UpdateRefundStatusDto updateDto);

    /**
     * Get refund statistics for admin dashboard
     * 
     * @return ResponseEntity containing refund counts by status
     */
    ResponseEntity<ApiResponse<AdminRefundStatisticsResponseDto>> getRefundStatistics();
}
