package project.ktc.springboot_app.payment.service;

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
}
