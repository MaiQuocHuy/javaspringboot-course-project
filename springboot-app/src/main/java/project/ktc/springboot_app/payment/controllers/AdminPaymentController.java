package project.ktc.springboot_app.payment.controllers;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.payment.dto.AdminPaidOutResponseDto;
import project.ktc.springboot_app.payment.dto.AdminPaymentResponseDto;
import project.ktc.springboot_app.payment.dto.AdminPaymentStatisticsResponseDto;
import project.ktc.springboot_app.payment.dto.AdminUpdatePaymentStatusDto;
import project.ktc.springboot_app.payment.dto.AdminUpdatePaymentStatusResponseDto;
import project.ktc.springboot_app.payment.dto.AdminPaymentDetailResponseDto;
import project.ktc.springboot_app.payment.interfaces.AdminPaymentService;

/**
 * REST Controller for admin payment operations
 * Requires ADMIN role for all endpoints
 */
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Payment API", description = "API for managing payments (Admin only)")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class AdminPaymentController {

        private final AdminPaymentService adminPaymentService;

        /**
         * Get all payments with pagination
         * 
         * @param pageable Pagination parameters
         * @return ResponseEntity containing paginated list of payments
         */
        @GetMapping
        @PreAuthorize("hasPermission('Payment', 'payment:READ')")
        @Operation(summary = "Get all payments", description = """
                        Retrieves all payments in the system with pagination support and advanced filtering for admin view.

                        **Features:**
                        - Returns all payments across all users
                        - Includes user and course information for each payment
                        - Payments are ordered by creation date (most recent first)
                        - Supports pagination for better performance
                        - Shows payment status, amount, and method
                        - Advanced search and filtering capabilities

                        **Search & Filter Options:**
                        - Search by payment ID, user name, or course title
                        - Filter by payment status (PENDING, COMPLETED, FAILED, REFUNDED)
                        - Filter by creation date range

                        **Admin Only:**
                        - This endpoint requires ADMIN role
                        - Provides complete payment overview for administrative purposes
                        - Includes sensitive payment information for audit purposes

                        **Response includes:**
                        - Payment ID, amount, status, and method
                        - User information (ID, name, email, thumbnail)
                        - Course information (ID, title, thumbnail)
                        - Payment timestamps (created, paid)
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<AdminPaymentResponseDto>>> getAllPayments(
                        @Parameter(description = "Search by payment ID, user name, or course title") @RequestParam(required = false) String search,

                        @Parameter(description = "Filter by payment status", example = "COMPLETED") @RequestParam(required = false) project.ktc.springboot_app.payment.entity.Payment.PaymentStatus status,

                        @Parameter(description = "Filter by creation date from (ISO format: yyyy-MM-dd)", example = "2024-01-01") @RequestParam(required = false) String fromDate,

                        @Parameter(description = "Filter by creation date to (ISO format: yyyy-MM-dd)", example = "2024-12-31") @RequestParam(required = false) String toDate,

                        @Parameter(description = "Filter by payment method", example = "STRIPE") @RequestParam(required = false) String paymentMethod,

                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) Integer page,

                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size) {

                Pageable pageable = PageRequest.of(page, size);
                log.info("Admin requesting all payments with pagination: page={}, size={}, search={}, status={}, fromDate={}, toDate={}, paymentMethod={}",
                                page, size, search, status, fromDate, toDate, paymentMethod);
                return adminPaymentService.getAllPayments(search, status, fromDate, toDate, paymentMethod, pageable);
        }

        /**
         * Get all payments without pagination
         * 
         * @return ResponseEntity containing list of all payments
         */
        @GetMapping("/all")
        @PreAuthorize("hasPermission('Payment', 'payment:READ')")
        @Operation(summary = "Get all payments (no pagination)", description = """
                        Retrieves all payments in the system without pagination for admin view with search and filtering.

                        **Warning:** This endpoint returns all payments at once and should be used carefully
                        for systems with large numbers of payments as it may impact performance.

                        **Search & Filter Options:**
                        - Search by payment ID, user name, or course title
                        - Filter by payment status (PENDING, COMPLETED, FAILED, REFUNDED)
                        - Filter by creation date range

                        **Use cases:**
                        - Generating reports that need complete data
                        - Exporting payment data
                        - Small systems with limited payment records

                        **Admin Only:**
                        - This endpoint requires ADMIN role
                        - Returns complete payment data for all users
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "All payments retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<AdminPaymentResponseDto>>> getAllPaymentsAll(
                        @Parameter(description = "Search by payment ID, user name, or course title") @RequestParam(required = false) String search,

                        @Parameter(description = "Filter by payment status", example = "COMPLETED") @RequestParam(required = false) project.ktc.springboot_app.payment.entity.Payment.PaymentStatus status,

                        @Parameter(description = "Filter by creation date from (ISO format: yyyy-MM-dd)", example = "2024-01-01") @RequestParam(required = false) String fromDate,

                        @Parameter(description = "Filter by creation date to (ISO format: yyyy-MM-dd)", example = "2024-12-31") @RequestParam(required = false) String toDate,

                        @Parameter(description = "Filter by payment method", example = "STRIPE") @RequestParam(required = false) String paymentMethod) {
                log.info("Admin requesting all payments without pagination with filters: search={}, status={}, fromDate={}, toDate={}, paymentMethod={}",
                                search, status, fromDate, toDate, paymentMethod);
                return adminPaymentService.getAllPayments(search, status, fromDate, toDate, paymentMethod);
        }

        /**
         * Get payment details by ID
         * 
         * @param paymentId The payment ID to retrieve
         * @return ResponseEntity containing detailed payment information
         */
        @GetMapping("/{paymentId}")
        @PreAuthorize("hasPermission('Payment', 'payment:READ')")
        @Operation(summary = "Get payment details", description = """
                        Retrieves detailed information about a specific payment by ID for admin view.

                        **Features:**
                        - Returns comprehensive payment information
                        - Includes user and course details
                        - Shows Stripe payment data if applicable (transaction ID, receipt URL, card info)
                        - Provides complete audit trail information

                        **Admin Only:**
                        - This endpoint requires ADMIN role
                        - Access to any payment regardless of user ownership
                        - Includes sensitive payment gateway information

                        **Stripe Integration:**
                        - Automatically fetches additional details for Stripe payments
                        - Includes transaction ID, receipt URL, and card information
                        - Gracefully handles Stripe API errors
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment details retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @ApiResponse(responseCode = "404", description = "Payment not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<AdminPaymentDetailResponseDto>> getPaymentDetail(
                        @Parameter(description = "Payment ID", required = true) @PathVariable String paymentId) {
                log.info("Admin requesting payment details for payment: {}", paymentId);
                return adminPaymentService.getPaymentDetail(paymentId);
        }

        /**
         * Update payment status
         * 
         * @param paymentId The payment ID to update
         * @param newStatus The new payment status (COMPLETED or FAILED)
         * @return ResponseEntity containing updated payment information
         */
        @PatchMapping("/{paymentId}/status")
        @PreAuthorize("hasPermission('Payment', 'payment:UPDATE')")
        @Operation(summary = "Update payment status", description = """
                        Updates the status of a payment from PENDING to either COMPLETED or FAILED.

                        **Allowed Status Transitions:**
                        - PENDING → COMPLETED
                        - PENDING → FAILED

                        **Business Rules:**
                        - Only payments with PENDING status can be updated
                        - When status is updated to COMPLETED, paidAt timestamp is automatically set
                        - System logs are created for audit trail
                        - Cannot update payments that are already COMPLETED, FAILED, or REFUNDED

                        **Admin Only:**
                        - This endpoint requires ADMIN role
                        - Used for manual payment processing or corrections
                        - Creates audit logs for compliance

                        **Parameters:**
                        - newStatus: Must be either "COMPLETED" or "FAILED"

                        **Side Effects:**
                        - Updates payment status in database
                        - Sets paidAt timestamp for COMPLETED status
                        - Creates system log entry for audit trail
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment status updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid status or payment cannot be updated"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @ApiResponse(responseCode = "404", description = "Payment not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<AdminUpdatePaymentStatusResponseDto>> updatePaymentStatus(
                        @Parameter(description = "Payment ID", required = true) @PathVariable String paymentId,
                        @Valid @RequestBody AdminUpdatePaymentStatusDto newStatusDto) {
                log.info("Admin updating payment status for payment: {} to status: {}", paymentId,
                                newStatusDto.getStatus());
                return adminPaymentService.updatePaymentStatus(paymentId, newStatusDto.getStatus().name());
        }

        /**
         * Paid out payment to instructor
         * 
         * @param paymentId The payment ID to paid out
         * @return ResponseEntity containing paid out operation result
         */
        @PostMapping("/{paymentId}/paid-out")
        @PreAuthorize("hasPermission('Payment', 'payment:PAID_OUT')")
        @Operation(summary = "Paid out payment to instructor", description = """
                        **Purpose:**
                        Pays out the payment to the course instructor by creating an instructor earning record.

                        **Business Rules:**
                        - Payment must be in COMPLETED status
                        - Must wait 3 days after payment completion
                        - No pending refunds allowed
                        - Cannot paid out twice

                        **Actions:**
                        - Creates instructor earning record (70% of payment amount)
                        - Sets payment.paidOutAt timestamp
                        - Creates audit logs for compliance

                        **Admin Only:**
                        - This endpoint requires ADMIN role
                        - Used for instructor payment processing
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment paid out successfully"),
                        @ApiResponse(responseCode = "400", description = "Payment cannot be paid out (validation failed)"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @ApiResponse(responseCode = "404", description = "Payment not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<AdminPaidOutResponseDto>> paidOutPayment(
                        @Parameter(description = "Payment ID", required = true) @PathVariable String paymentId) {
                log.info("Admin attempting to paid out payment: {}", paymentId);
                return adminPaymentService.paidOutPayment(paymentId);
        }

        /**
         * Get payment statistics for admin dashboard
         * 
         * @return ResponseEntity containing payment counts by status
         */
        @GetMapping("/statistics")
        @PreAuthorize("hasPermission('Payment', 'payment:READ')")
        @Operation(summary = "Get payment statistics", description = """
                        **Purpose:**
                        Retrieves comprehensive statistics about payments for admin dashboard.

                        **Response Includes:**
                        - Payment counts by status (total, pending, completed, failed, refunded)

                        **Use Cases:**
                        - Admin dashboard overview
                        - Financial reporting
                        - System health monitoring

                        **Admin Only:**
                        - This endpoint requires ADMIN role
                        - Provides system-wide statistics
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<AdminPaymentStatisticsResponseDto>> getPaymentStatistics() {
                log.info("Admin retrieving payment statistics");
                return adminPaymentService.getPaymentStatistics();
        }

}
