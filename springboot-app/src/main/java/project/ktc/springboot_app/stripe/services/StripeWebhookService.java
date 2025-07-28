package project.ktc.springboot_app.stripe.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.enrollment.interfaces.EnrollmentService;
import project.ktc.springboot_app.payment.service.PaymentService;
import project.ktc.springboot_app.stripe.config.StripeConfig;

/**
 * Service to handle Stripe webhook events
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StripeWebhookService {

    private final StripeConfig stripeConfig;
    private final PaymentService paymentService;
    private final EnrollmentService enrollmentService;

    /**
     * Processes incoming webhook events from Stripe
     *
     * @param payload   The raw webhook payload
     * @param sigHeader The Stripe signature header
     * @return true if event was processed successfully
     * @throws SignatureVerificationException if signature verification fails
     */
    public boolean processWebhookEvent(String payload, String sigHeader) throws SignatureVerificationException {
        Event event;

        try {
            // Verify webhook signature if webhook secret is configured
            if (stripeConfig.getWebhookSecret() != null && !stripeConfig.getWebhookSecret().isEmpty()) {
                event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
                log.info("Webhook signature verified successfully");
            } else {
                // In development, parse without verification
                event = Event.GSON.fromJson(payload, Event.class);
                log.warn("Webhook processed without signature verification - development mode");
            }

            // Debug logging for the event
            log.info("Parsed event - Type: {}, ID: {}, Created: {}", event.getType(), event.getId(),
                    event.getCreated());
            log.debug("Event has data: {}", event.getData() != null);
            log.debug("Event dataObjectDeserializer: {}", event.getDataObjectDeserializer() != null);

        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error parsing webhook payload: {}", e.getMessage());
            return false;
        }

        // Handle the event
        return handleEvent(event);
    }

    /**
     * Routes events to appropriate handlers based on event type
     */
    private boolean handleEvent(Event event) {
        log.info("Processing webhook event: {} with ID: {}", event.getType(), event.getId());

        try {
            switch (event.getType()) {
                case "checkout.session.completed":
                    log.info("Handling checkout session completed event: {}", event.getId());
                    handleCheckoutSessionCompleted(event);
                    break;
                case "checkout.session.expired":
                    log.info("Handling checkout session expired event: {}", event.getId());
                    handleCheckoutSessionExpired(event);
                    break;
                case "payment_intent.succeeded":
                    log.info("Handling payment intent succeeded event: {}", event.getId());
                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    log.info("Handling payment intent failed event: {}", event.getId());
                    handlePaymentIntentFailed(event);
                    break;
                case "invoice.payment_succeeded":
                    log.info("Handling invoice payment succeeded event: {}", event.getId());
                    handleInvoicePaymentSucceeded(event);
                    break;
                case "customer.subscription.created":
                    log.info("Handling customer subscription created event: {}", event.getId());
                    handleSubscriptionCreated(event);
                    break;
                case "customer.subscription.updated":
                    log.info("Handling customer subscription updated event: {}", event.getId());
                    handleSubscriptionUpdated(event);
                    break;
                case "customer.subscription.deleted":
                    log.info("Handling customer subscription deleted event: {}", event.getId());
                    handleSubscriptionDeleted(event);
                    break;
                default:
                    log.info("Unhandled event type: {}", event.getType());
                    return true; // Still return success for unhandled events
            }

            log.info("Successfully processed webhook event: {}", event.getType());
            return true;

        } catch (Exception e) {
            log.error("Error processing webhook event {}: {}", event.getType(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Handles successful checkout sessions
     * This is the main event for course purchases
     */
    private void handleCheckoutSessionCompleted(Event event) {
        log.info("Handling checkout session completed event: {}", event.getId());

        Session session = null;

        // First attempt: Try to deserialize session from event data
        try {
            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                Object eventObject = event.getDataObjectDeserializer().getObject().get();
                if (eventObject instanceof Session) {
                    session = (Session) eventObject;
                    log.info("Successfully deserialized session from event data: {}", session.getId());
                } else {
                    log.warn("Event data object is not a Session instance. Got: {}", eventObject.getClass().getName());
                }
            } else {
                log.warn("Event data object is not present in the event, trying fallback method");
            }
        } catch (Exception e) {
            log.warn("Error deserializing session object from event: {}", e.getMessage());
        }

        // Fallback: If session is still null, extract session ID and retrieve manually
        if (session == null) {
            try {
                String sessionId = extractSessionIdFromEventData(event);
                if (sessionId != null) {
                    session = Session.retrieve(sessionId);
                    log.info("Successfully retrieved session {} using fallback method", sessionId);
                } else {
                    log.error("Could not extract session ID from event data");
                    return;
                }
            } catch (Exception fallbackException) {
                log.error("Fallback session retrieval failed: {}", fallbackException.getMessage(), fallbackException);
                return;
            }
        }

        if (session == null) {
            log.error("Could not retrieve session from checkout.session.completed event");
            return;
        }

        log.info("Processing completed checkout session: {}", session.getId());
        log.info("Customer email: {}, Amount total: {}", session.getCustomerEmail(), session.getAmountTotal());

        try {
            // Extract metadata (courseId and userId should be stored in session metadata)
            String courseId = session.getMetadata().get("courseId");
            String userId = session.getMetadata().get("userId");

            if (courseId == null || userId == null) {
                log.error("Missing required metadata in session. CourseId: {}, UserId: {}", courseId, userId);
                return;
            }

            // Update payment status to COMPLETED
            String paymentId = session.getMetadata().get("paymentId");
            if (paymentId != null) {
                paymentService.updatePaymentStatusFromWebhook(paymentId, "COMPLETED", session.getId());
                log.info("Payment {} marked as COMPLETED", paymentId);
            }

            // Create enrollment for the user
            enrollmentService.createEnrollmentFromWebhook(userId, courseId, session.getId());
            log.info("Enrollment created for user {} in course {}", userId, courseId);

            // Send confirmation email (if you have email service)
            // emailService.sendCourseEnrollmentConfirmation(session.getCustomerEmail(),
            // courseId);

        } catch (Exception e) {
            log.error("Error processing checkout session completion: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process checkout session completion", e);
        }
    }

    /**
     * Handles expired checkout sessions
     */
    private void handleCheckoutSessionExpired(Event event) {
        Session session = null;

        try {
            // Primary: Try to deserialize session from event
            session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

            // Fallback: If session is null, try to extract session ID and retrieve it
            if (session == null) {
                log.warn("Session deserialization failed, attempting to retrieve session manually");
                String sessionId = extractSessionIdFromEventData(event);
                if (sessionId != null) {
                    session = Session.retrieve(sessionId);
                    log.info("Successfully retrieved session {} using fallback method", sessionId);
                }
            }

            if (session == null) {
                log.error("Could not retrieve session from checkout.session.expired event after all attempts");
                return;
            }

            log.info("Processing expired checkout session: {}", session.getId());

            // Update payment status to FAILED if payment exists
            String paymentId = session.getMetadata().get("paymentId");
            if (paymentId != null) {
                paymentService.updatePaymentStatusFromWebhook(paymentId, "FAILED", session.getId());
                log.info("Payment {} marked as FAILED due to session expiration", paymentId);
            }

        } catch (Exception e) {
            log.error("Error processing checkout session expiration: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles successful payment intents
     */
    private void handlePaymentIntentSucceeded(Event event) {
        log.info("Payment intent succeeded: {}", event.getId());
        // Additional payment processing logic if needed
    }

    /**
     * Handles failed payment intents
     */
    private void handlePaymentIntentFailed(Event event) {
        log.info("Payment intent failed: {}", event.getId());
        // Handle payment failures, potentially retry logic
    }

    /**
     * Handles successful invoice payments (for subscriptions)
     */
    private void handleInvoicePaymentSucceeded(Event event) {
        log.info("Invoice payment succeeded: {}", event.getId());
        // Handle subscription payments
    }

    /**
     * Handles subscription creation
     */
    private void handleSubscriptionCreated(Event event) {
        log.info("Subscription created: {}", event.getId());
        // Handle subscription creation logic
    }

    /**
     * Handles subscription updates
     */
    private void handleSubscriptionUpdated(Event event) {
        log.info("Subscription updated: {}", event.getId());
        // Handle subscription updates (plan changes, etc.)
    }

    /**
     * Handles subscription cancellation
     */
    private void handleSubscriptionDeleted(Event event) {
        log.info("Subscription deleted: {}", event.getId());
        // Handle subscription cancellation logic
    }

    /**
     * Helper method to extract session ID from event data when deserialization
     * fails
     */
    private String extractSessionIdFromEventData(Event event) {
        try {
            log.debug("Attempting to extract session ID from event data");
            log.debug("Event type: {}, Event ID: {}", event.getType(), event.getId());

            // Parse the raw JSON to extract session ID
            String rawEventJson = event.toJson();
            log.debug("Raw event JSON available for parsing");

            JsonObject eventJson = JsonParser.parseString(rawEventJson).getAsJsonObject();

            if (eventJson.has("data") && eventJson.getAsJsonObject("data").has("object")) {
                JsonObject dataObject = eventJson.getAsJsonObject("data").getAsJsonObject("object");

                // Log the structure for debugging
                log.debug("Event data object keys: {}", dataObject.keySet());

                if (dataObject.has("id")) {
                    String sessionId = dataObject.get("id").getAsString();
                    log.info("Extracted session ID from event JSON: {}", sessionId);
                    return sessionId;
                } else {
                    log.warn("No 'id' field found in event data object. Available keys: {}", dataObject.keySet());
                }
            } else {
                log.warn("Event JSON structure is missing data.object path");
                log.debug("Event JSON keys: {}", eventJson.keySet());
            }

        } catch (Exception e) {
            log.error("Error extracting session ID from event: {}", e.getMessage(), e);
        }

        log.error("Could not extract session ID from event using any method");
        return null;
    }
}
