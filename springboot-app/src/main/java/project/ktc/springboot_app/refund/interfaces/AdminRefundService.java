package project.ktc.springboot_app.refund.interfaces;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.refund.dto.AdminRefundDetailsResponseDto;
import project.ktc.springboot_app.refund.dto.AdminRefundResponseDto;
import project.ktc.springboot_app.refund.dto.AdminRefundStatisticsResponseDto;

public interface AdminRefundService {

  /**
   * Get all payments with pagination for admin
   *
   * @param pageable Pagination information
   * @return ResponseEntity containing paginated list of refunds
   */
  ResponseEntity<ApiResponse<PaginatedResponse<AdminRefundResponseDto>>> getAllRefunds(
      Pageable pageable);

  /**
   * Get all refunds with pagination, search, and filtering for admin
   *
   * @param search Search term for refund ID, user name, or course title
   * @param status Filter by refund status
   * @param fromDate Filter by creation date from (ISO date string)
   * @param toDate Filter by creation date to (ISO date string)
   * @param pageable Pagination information
   * @return ResponseEntity containing paginated list of filtered refunds
   */
  ResponseEntity<ApiResponse<PaginatedResponse<AdminRefundResponseDto>>> getAllRefunds(
      String search,
      project.ktc.springboot_app.refund.entity.Refund.RefundStatus status,
      String fromDate,
      String toDate,
      Pageable pageable);

  /**
   * Get all refunds without pagination for admin
   *
   * @return ResponseEntity containing list of all refunds
   */
  ResponseEntity<ApiResponse<List<AdminRefundResponseDto>>> getAllRefunds();

  /**
   * Get all refunds without pagination with search and filtering for admin
   *
   * @param search Search term for refund ID, user name, or course title
   * @param status Filter by refund status
   * @param fromDate Filter by creation date from (ISO date string)
   * @param toDate Filter by creation date to (ISO date string)
   * @return ResponseEntity containing list of filtered refunds
   */
  ResponseEntity<ApiResponse<List<AdminRefundResponseDto>>> getAllRefunds(
      String search,
      project.ktc.springboot_app.refund.entity.Refund.RefundStatus status,
      String fromDate,
      String toDate);

  /**
   * Get refund details by ID for admin
   *
   * @param refundId The refund ID to retrieve
   * @return ResponseEntity containing detailed refund information
   */
  ResponseEntity<ApiResponse<AdminRefundDetailsResponseDto>> getRefundDetail(String refundId);

  /**
   * Get refund statistics for admin dashboard
   *
   * @return ResponseEntity containing refund counts by status
   */
  ResponseEntity<ApiResponse<AdminRefundStatisticsResponseDto>> getRefundStatistics();
}
