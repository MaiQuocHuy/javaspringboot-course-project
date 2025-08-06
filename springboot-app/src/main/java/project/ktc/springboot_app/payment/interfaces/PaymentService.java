package project.ktc.springboot_app.payment.interfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.payment.dto.PaymentResponseDto;
import project.ktc.springboot_app.payment.entity.Payment;

/**
 * Interface for payment-related operations
 */
public interface PaymentService {

    /**
     * Updates payment status from webhook events
     * 
     * @param paymentId       The payment ID to update
     * @param status          The new payment status
     * @param stripeSessionId The Stripe session ID for reference
     */
    void updatePaymentStatusFromWebhook(String paymentId, String status, String stripeSessionId);

    /**
     * Creates a new payment record
     * 
     * @param userId          The user making the payment
     * @param courseId        The course being purchased
     * @param amount          The payment amount
     * @param stripeSessionId The Stripe session ID
     * @return The created payment ID
     */
    String createPayment(String userId, String courseId, Double amount, String stripeSessionId);

    /**
     * Updates the Stripe session ID for an existing payment
     * 
     * @param paymentId       The payment ID to update
     * @param stripeSessionId The Stripe session ID to store
     */
    void updatePaymentSessionId(String paymentId, String stripeSessionId);

    /**
     * Finds payment by Stripe session ID and verifies the amount
     * 
     * @param stripeSessionId The Stripe session ID
     * @param paidAmount      The amount paid according to Stripe
     * @return The payment entity if found and amount matches
     */
    Optional<Payment> findPaymentBySessionIdAndVerifyAmount(String stripeSessionId, Double paidAmount);

    /**
<<<<<<< HEAD:springboot-app/src/main/java/project/ktc/springboot_app/payment/service/PaymentService.java
     * Retrieves all payment transactions for the currently authenticated student
     * 
     * @return ResponseEntity containing list of payment transactions
     */
    ResponseEntity<ApiResponse<List<PaymentResponseDto>>> getStudentPayments();

    /**
     * Retrieves detailed information about a specific payment for the currently
     * authenticated student
     * 
     * @param paymentId The ID of the payment to retrieve
     * @return ResponseEntity containing detailed payment information
     */
    ResponseEntity<ApiResponse<project.ktc.springboot_app.payment.dto.PaymentDetailResponseDto>> getStudentPaymentDetail(
            String paymentId);
=======
     * Finds payment by Stripe session ID
     * 
     * @param stripeSessionId The Stripe session ID
     * @return The payment entity if found
     */
    Optional<Payment> findPaymentBySessionId(String stripeSessionId);
>>>>>>> ebe3623b8c790417898331f330aea841c3682f4f:springboot-app/src/main/java/project/ktc/springboot_app/payment/interfaces/PaymentService.java
}
