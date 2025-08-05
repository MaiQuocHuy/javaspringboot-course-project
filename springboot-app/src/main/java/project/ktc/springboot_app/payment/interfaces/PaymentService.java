package project.ktc.springboot_app.payment.interfaces;

import java.util.Optional;
import project.ktc.springboot_app.entity.Payment;

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
     * Finds payment by Stripe session ID
     * 
     * @param stripeSessionId The Stripe session ID
     * @return The payment entity if found
     */
    Optional<Payment> findPaymentBySessionId(String stripeSessionId);
}
