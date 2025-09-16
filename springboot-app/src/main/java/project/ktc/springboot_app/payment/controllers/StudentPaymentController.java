package project.ktc.springboot_app.payment.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.payment.dto.PaymentResponseDto;
import project.ktc.springboot_app.payment.dto.StudentPaymentStatsDto;
import project.ktc.springboot_app.payment.interfaces.PaymentService;
import project.ktc.springboot_app.payment.dto.PaymentDetailResponseDto;

import java.util.List;

/**
 * REST Controller for student payment operations
 */
@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
@Tag(name = "Student Payment API", description = "Endpoints for students to view their payment transactions")
public class StudentPaymentController {

        private final PaymentService paymentService;

        /**
         * Retrieve all payment transactions for the currently authenticated student
         * 
         * @return ResponseEntity containing list of payment transactions
         */
        @GetMapping("/payments")
        @Operation(summary = "Get student payments", description = """
                        Retrieves a list of all payment transactions made by the currently authenticated student.

                        **Features:**
                        - Returns all payment transactions for the current student
                        - Includes course information (title, thumbnail) for each payment
                        - Payments are ordered by creation date (most recent first)
                        - Supports multiple payment statuses and methods
                        - Default currency format is USD

                        **Response includes:**
                        - Payment ID and amount
                        - Payment status (PENDING, COMPLETED, FAILED, REFUNDED)
                        - Payment method (STRIPE, BANK_TRANSFER, etc.)
                        - Creation timestamp
                        - Associated course details (ID, title, thumbnail URL)
                        """)
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payments retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - STUDENT role required", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Server error during payment retrieval", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<ApiResponse<List<PaymentResponseDto>>> getPayments() {
                return paymentService.getStudentPayments();
        }

        /**
         * Retrieve detailed information about a specific payment made by the student
         * 
         * @param id The payment ID to retrieve details for
         * @return ResponseEntity containing detailed payment information
         */
        @GetMapping("/payments/{id}")
        @Operation(summary = "Get payment details", description = """
                        Retrieves detailed information about a specific payment made by the currently authenticated student,
                        including external gateway (e.g., Stripe) data if available.

                        **Features:**
                        - Validates payment ownership (students can only access their own payments)
                        - Includes basic payment information (ID, amount, status, method, creation date)
                        - Fetches external gateway data for Stripe payments:
                          - Transaction ID from PaymentIntent
                          - Receipt URL for downloading payment receipt
                          - Card information (brand, last 4 digits, expiration)
                        - Returns course information associated with the payment
                        - Secure access with ownership verification

                        **Payment Security:**
                        - Only the student who made the payment can access its details
                        - Returns 404 for non-existent payments or payments belonging to other users
                        - No sensitive payment data is exposed beyond what's necessary

                        **Stripe Integration:**
                        - Automatically fetches additional details for Stripe payments
                        - Includes card brand, last 4 digits, and expiration date
                        - Provides receipt URL for payment confirmation
                        - Gracefully handles Stripe API errors without breaking the response

                        **Response Format:**
                        - All monetary amounts are in VND currency
                        - Timestamps are in ISO 8601 format
                        - Card information is only included for successful Stripe payments
                        - Course details include ID, title, and thumbnail URL
                        """)
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment details retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - STUDENT role required", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found or not owned by the current user", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Server error during payment detail retrieval", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<ApiResponse<PaymentDetailResponseDto>> getPaymentDetail(@PathVariable String id) {
                return paymentService.getStudentPaymentDetail(id);
        }

        /**
         * Retrieve payment statistics for the currently authenticated student
         * 
         * @return ResponseEntity containing student payment statistics
         */
        @GetMapping("/payment-stats")
        @Operation(summary = "Get student payment statistics", description = """
                        Retrieves comprehensive payment statistics for the currently authenticated student.

                        **Statistics Included:**
                        - Total number of payment transactions
                        - Total amount spent (sum of completed payments only)
                        - Number of successful/completed payments
                        - Number of failed payments

                        **Features:**
                        - Only includes data for the authenticated student
                        - Amount calculations consider only COMPLETED payments
                        - All monetary amounts are in the configured currency
                        - Real-time data reflecting current payment status
                        """)
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment statistics retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - STUDENT role required", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Server error during statistics retrieval", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<ApiResponse<StudentPaymentStatsDto>> getPaymentStats() {
                return paymentService.getStudentPaymentStats();
        }
}
