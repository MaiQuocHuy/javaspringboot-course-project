package project.ktc.springboot_app.payment.controllers;

import java.util.List;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Admin;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.payment.dto.AdminPaymentResponseDto;
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
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Payment API", description = "API for managing payments (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AdminPaymentController {

        private final AdminPaymentService adminPaymentService;

        /**
         * Get all payments with pagination
         * 
         * @param pageable Pagination parameters
         * @return ResponseEntity containing paginated list of payments
         */
        @GetMapping
        @Operation(summary = "Get all payments", description = """
                        Retrieves all payments in the system with pagination support for admin view.

                        **Features:**
                        - Returns all payments across all users
                        - Includes user and course information for each payment
                        - Payments are ordered by creation date (most recent first)
                        - Supports pagination for better performance
                        - Shows payment status, amount, and method

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
                        @PageableDefault(size = 10) Pageable pageable) {
                log.info("Admin requesting all payments with pagination: page={}, size={}",
                                pageable.getPageNumber(), pageable.getPageSize());
                return adminPaymentService.getAllPayments(pageable);
        }

        /**
         * Get all payments without pagination
         * 
         * @return ResponseEntity containing list of all payments
         */
        @GetMapping("/all")
        @Operation(summary = "Get all payments (no pagination)", description = """
                        Retrieves all payments in the system without pagination for admin view.

                        **Warning:** This endpoint returns all payments at once and should be used carefully
                        for systems with large numbers of payments as it may impact performance.

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
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<AdminPaymentResponseDto>>> getAllPayments() {
                log.info("Admin requesting all payments without pagination");
                return adminPaymentService.getAllPayments();
        }

        /**
         * Get payment details by ID
         * 
         * @param paymentId The payment ID to retrieve
         * @return ResponseEntity containing detailed payment information
         */
        @GetMapping("/{paymentId}")
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

}
