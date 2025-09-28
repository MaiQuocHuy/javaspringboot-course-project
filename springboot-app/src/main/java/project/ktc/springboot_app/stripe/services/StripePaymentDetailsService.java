package project.ktc.springboot_app.stripe.services;

import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.payment.dto.PaymentDetailResponseDto.StripePaymentData;

/**
 * Service for fetching Stripe payment details
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StripePaymentDetailsService {

    /**
     * Fetches detailed payment information from Stripe using the session ID
     *
     * @param sessionId The Stripe checkout session ID
     * @return StripePaymentData containing payment details, or null if not found
     */
    public StripePaymentData fetchPaymentDetails(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.debug("Session ID is null or empty, skipping Stripe data fetch");
            return null;
        }

        try {
            log.info("Fetching Stripe payment details for session: {}", sessionId);

            // Retrieve the checkout session
            Session session;
            try {
                session = Session.retrieve(sessionId);
            } catch (InvalidRequestException e) {
                // Handle case where session doesn't exist (expired, deleted, or invalid)
                if (e.getMessage() != null && e.getMessage().contains("No such checkout.session")) {
                    log.warn("Stripe session no longer exists for ID: {}. This is normal for expired test sessions.",
                            sessionId);
                    return null;
                } else {
                    // Re-throw other invalid request exceptions
                    throw e;
                }
            }

            if (session == null) {
                log.warn("Stripe session not found for ID: {}", sessionId);
                return null;
            }

            log.debug("Retrieved Stripe session: {}", session.getId());

            // Get the payment intent ID from the session
            String paymentIntentId = session.getPaymentIntent();

            if (paymentIntentId == null) {
                log.warn("No payment intent found for session: {}", sessionId);
                return createBasicStripeData(session);
            }

            // Retrieve the payment intent to get transaction details
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if (paymentIntent == null) {
                log.warn("Payment intent not found for ID: {}", paymentIntentId);
                return createBasicStripeData(session);
            }

            log.debug("Retrieved payment intent: {}", paymentIntent.getId());

            // Get the latest charge ID from payment intent
            String chargeId = paymentIntent.getLatestCharge();

            StripePaymentData.StripePaymentDataBuilder builder = StripePaymentData.builder()
                    .transactionId(paymentIntent.getId());

            // If we have a charge, get the card and receipt details
            if (chargeId != null) {
                try {
                    Charge charge = Charge.retrieve(chargeId);

                    if (charge != null) {
                        log.debug("Retrieved charge: {}", charge.getId());

                        // Set receipt URL
                        builder.receiptUrl(charge.getReceiptUrl());

                        // Get card details if available
                        if (charge.getPaymentMethodDetails() != null &&
                                charge.getPaymentMethodDetails().getCard() != null) {

                            var card = charge.getPaymentMethodDetails().getCard();
                            builder.cardBrand(card.getBrand())
                                    .cardLast4(card.getLast4())
                                    .cardExpMonth(Math.toIntExact(card.getExpMonth()))
                                    .cardExpYear(Math.toIntExact(card.getExpYear()));
                        }
                    }
                } catch (StripeException e) {
                    log.warn("Error retrieving charge details for ID {}: {}", chargeId, e.getMessage());
                }
            }

            StripePaymentData stripeData = builder.build();
            log.info("Successfully fetched Stripe payment details for session: {}", sessionId);

            return stripeData;

        } catch (StripeException e) {
            log.error("Error fetching Stripe payment details for session {}: {}", sessionId, e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching Stripe payment details for session {}: {}", sessionId, e.getMessage(),
                    e);
            return null;
        }
    }

    /**
     * Creates basic Stripe data from session information only
     */
    private StripePaymentData createBasicStripeData(Session session) {
        return StripePaymentData.builder()
                .transactionId(session.getId()) // Use session ID as fallback transaction ID
                .build();
    }

    /**
     * Checks if a payment was processed through Stripe
     *
     * @param paymentMethod The payment method string
     * @return true if the payment was processed through Stripe
     */
    public boolean isStripePayment(String paymentMethod) {
        return paymentMethod != null &&
                (paymentMethod.equalsIgnoreCase("stripe") ||
                        paymentMethod.equalsIgnoreCase("STRIPE"));
    }
}
