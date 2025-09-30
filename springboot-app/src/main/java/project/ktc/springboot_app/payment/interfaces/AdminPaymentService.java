package project.ktc.springboot_app.payment.interfaces;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.payment.dto.AdminPaidOutResponseDto;
import project.ktc.springboot_app.payment.dto.AdminPaymentDetailResponseDto;
import project.ktc.springboot_app.payment.dto.AdminPaymentResponseDto;
import project.ktc.springboot_app.payment.dto.AdminPaymentStatisticsResponseDto;
import project.ktc.springboot_app.payment.dto.AdminUpdatePaymentStatusResponseDto;

/** Interface for admin payment operations */
public interface AdminPaymentService {

  /**
   * Get all payments with pagination for admin
   *
   * @param pageable Pagination information
   * @return ResponseEntity containing paginated list of payments
   */
  ResponseEntity<ApiResponse<PaginatedResponse<AdminPaymentResponseDto>>> getAllPayments(
      Pageable pageable);

  /**
   * Get all payments with pagination, search, and filtering for admin
   *
   * @param search Search term for payment ID, user name, or course title
   * @param status Filter by payment status
   * @param fromDate Filter by creation date from (ISO date string)
   * @param toDate Filter by creation date to (ISO date string)
   * @param paymentMethod Filter by payment method
   * @param pageable Pagination information
   * @return ResponseEntity containing paginated list of filtered payments
   */
  ResponseEntity<ApiResponse<PaginatedResponse<AdminPaymentResponseDto>>> getAllPayments(
      String search,
      project.ktc.springboot_app.payment.entity.Payment.PaymentStatus status,
      String fromDate,
      String toDate,
      String paymentMethod,
      Pageable pageable);

  /**
   * Get all payments without pagination for admin
   *
   * @return ResponseEntity containing list of all payments
   */
  ResponseEntity<ApiResponse<List<AdminPaymentResponseDto>>> getAllPayments();

  /**
   * Get all payments without pagination with search and filtering for admin
   *
   * @param search Search term for payment ID, user name, or course title
   * @param status Filter by payment status
   * @param fromDate Filter by creation date from (ISO date string)
   * @param toDate Filter by creation date to (ISO date string)
   * @param paymentMethod Filter by payment method
   * @return ResponseEntity containing list of filtered payments
   */
  ResponseEntity<ApiResponse<List<AdminPaymentResponseDto>>> getAllPayments(
      String search,
      project.ktc.springboot_app.payment.entity.Payment.PaymentStatus status,
      String fromDate,
      String toDate,
      String paymentMethod);

  /**
   * Get payment details by ID for admin
   *
   * @param paymentId The payment ID to retrieve
   * @return ResponseEntity containing detailed payment information
   */
  ResponseEntity<ApiResponse<AdminPaymentDetailResponseDto>> getPaymentDetail(String paymentId);

  /**
   * Update payment status from PENDING to COMPLETED or FAILED
   *
   * @param paymentId The payment ID to update
   * @param newStatus The new status (COMPLETED or FAILED)
   * @return ResponseEntity containing the updated payment information
   */
  ResponseEntity<ApiResponse<AdminUpdatePaymentStatusResponseDto>> updatePaymentStatus(
      String paymentId, String newStatus);

  /**
   * Get payments for a specific user (admin view)
   *
   * @param userId The user ID
   * @param pageable Pagination information
   * @return ResponseEntity containing user's payments
   */
  ResponseEntity<ApiResponse<Page<AdminPaymentResponseDto>>> getPaymentsByUserId(
      String userId, Pageable pageable);

  /**
   * Get payments for a specific course (admin view)
   *
   * @param courseId The course ID
   * @param pageable Pagination information
   * @return ResponseEntity containing course's payments
   */
  ResponseEntity<ApiResponse<Page<AdminPaymentResponseDto>>> getPaymentsByCourseId(
      String courseId, Pageable pageable);

  /**
   * Search payments by user email or course title
   *
   * @param searchTerm The search term
   * @param pageable Pagination information
   * @return ResponseEntity containing search results
   */
  ResponseEntity<ApiResponse<Page<AdminPaymentResponseDto>>> searchPayments(
      String searchTerm, Pageable pageable);

  /**
   * Paid out payment to instructor This action creates an instructor earning record and marks
   * payment as paid out
   *
   * @param paymentId The payment ID to paid out
   * @return ResponseEntity containing the paid out operation result
   */
  ResponseEntity<ApiResponse<AdminPaidOutResponseDto>> paidOutPayment(String paymentId);

  /**
   * Get payment statistics for admin dashboard
   *
   * @return ResponseEntity containing payment counts by status
   */
  ResponseEntity<ApiResponse<AdminPaymentStatisticsResponseDto>> getPaymentStatistics();
}
