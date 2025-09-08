package project.ktc.springboot_app.stripe.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import project.ktc.springboot_app.email.interfaces.EmailService;
import project.ktc.springboot_app.enrollment.services.EnrollmentServiceImp;
import project.ktc.springboot_app.payment.service.PaymentServiceImp;
import project.ktc.springboot_app.stripe.config.StripeConfig;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.discount.interfaces.DiscountPriceService;
import project.ktc.springboot_app.discount.interfaces.AffiliatePayoutService;
import project.ktc.springboot_app.discount.entity.DiscountUsage;
import project.ktc.springboot_app.discount.repositories.DiscountUsageRepository;
import project.ktc.springboot_app.discount.enums.DiscountType;

import java.math.BigDecimal;

/**
 * Service to handle Stripe webhook events
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StripeWebhookService {

    private final StripeConfig stripeConfig;
    private final PaymentServiceImp paymentService;
    private final EnrollmentServiceImp enrollmentService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final DiscountPriceService discountPriceService;
    private final AffiliatePayoutService affiliatePayoutService;
    private final DiscountUsageRepository discountUsageRepository;

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

        log.info("🔄 Processing Stripe webhook event...");
        log.info("📝 Payload length: {}", payload != null ? payload.length() : 0);
        log.info("🔐 Signature header: {}", sigHeader != null ? "Present" : "Missing");

        try {
            // Verify webhook signature if webhook secret is configured
            if (stripeConfig.getWebhookSecret() != null && !stripeConfig.getWebhookSecret().isEmpty()
                    && sigHeader != null) {
                event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
                log.info("✅ Webhook signature verified successfully");
            } else {
                // In development, parse without verification
                event = Event.GSON.fromJson(payload, Event.class);
                log.warn("⚠️ Webhook processed without signature verification - development mode");
            }

            // Debug logging for the event
            log.info("📧 Parsed event - Type: {}, ID: {}, Created: {}", event.getType(), event.getId(),
                    event.getCreated());
            log.debug("📊 Event has data: {}", event.getData() != null);
            log.debug("🔍 Event dataObjectDeserializer: {}", event.getDataObjectDeserializer() != null);

        } catch (SignatureVerificationException e) {
            log.error("❌ Webhook signature verification failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Error parsing webhook payload: {}", e.getMessage());
            return false;
        }

        // Handle the event
        return handleEvent(event);
    }

    /**
     * Routes events to appropriate handlers based on event type
     */
    private boolean handleEvent(Event event) {
        log.info("🎯 Processing webhook event: {} with ID: {}", event.getType(), event.getId());

        try {
            switch (event.getType()) {
                case "checkout.session.completed":
                    log.info("💳 Handling checkout session completed event: {}", event.getId());
                    handleCheckoutSessionCompleted(event);
                    break;
                case "checkout.session.expired":
                    log.info("⏰ Handling checkout session expired event: {}", event.getId());
                    handleCheckoutSessionExpired(event);
                    break;
                case "payment_intent.succeeded":
                    log.info("✅ Handling payment intent succeeded event: {}", event.getId());
                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    log.info("❌ Handling payment intent failed event: {}", event.getId());
                    handlePaymentIntentFailed(event);
                    break;
                case "invoice.payment_succeeded":
                    log.info("🧾 Handling invoice payment succeeded event: {}", event.getId());
                    handleInvoicePaymentSucceeded(event);
                    break;
                case "customer.subscription.created":
                    log.info("📅 Handling customer subscription created event: {}", event.getId());
                    handleSubscriptionCreated(event);
                    break;
                case "customer.subscription.updated":
                    log.info("🔄 Handling customer subscription updated event: {}", event.getId());
                    handleSubscriptionUpdated(event);
                    break;
                case "customer.subscription.deleted":
                    log.info("🗑️ Handling customer subscription deleted event: {}", event.getId());
                    handleSubscriptionDeleted(event);
                    break;
                default:
                    log.info("❓ Unhandled event type: {}", event.getType());
                    return true; // Still return success for unhandled events
            }

            log.info("✅ Successfully processed webhook event: {}", event.getType());
            return true;

        } catch (Exception e) {
            log.error("❌ Error processing webhook event {}: {}", event.getType(), e.getMessage(), e);
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

        log.info("🎯 Processing completed checkout session: {}", session.getId());
        log.info("📧 Customer email: {}, 💰 Amount total: {} cents", session.getCustomerEmail(),
                session.getAmountTotal());

        try {
            // Extract metadata (courseId and userId should be stored in session metadata)
            String courseId = session.getMetadata() != null ? session.getMetadata().get("courseId") : null;
            String userId = session.getMetadata() != null ? session.getMetadata().get("userId") : null;
            String discountCode = session.getMetadata() != null ? session.getMetadata().get("discountCode") : null;
            String discountAmountStr = session.getMetadata() != null ? session.getMetadata().get("discountAmount")
                    : null;

            log.info("📊 Session metadata - CourseId: {}, UserId: {}, DiscountCode: {}", courseId, userId,
                    discountCode);

            if (courseId == null || userId == null) {
                log.error("❌ Missing required metadata in session. CourseId: {}, UserId: {}", courseId, userId);
                log.error("📋 Available metadata keys: {}",
                        session.getMetadata() != null ? session.getMetadata().keySet() : "No metadata");
                return;
            }

            // Verify payment amount matches what was recorded in database
            // session.getAmountTotal() is in cents, convert to dollars for comparison
            double stripeAmountInDollars = session.getAmountTotal() / 100.0;
            log.info("💵 Converting Stripe amount: {} cents = ${}", session.getAmountTotal(), stripeAmountInDollars);

            var paymentOpt = paymentService.findPaymentBySessionIdAndVerifyAmount(session.getId(),
                    stripeAmountInDollars);
            if (paymentOpt.isEmpty()) {
                log.error(
                        "❌ Payment verification failed for session {}. Either payment not found or amount mismatch. Stripe amount: ${}",
                        session.getId(), stripeAmountInDollars);
                return;
            }

            var payment = paymentOpt.get();
            log.info("✅ Payment amount verified successfully. Database: ${}, Stripe: ${}",
                    payment.getAmount(), stripeAmountInDollars);

            // Update payment status to COMPLETED
            paymentService.updatePaymentStatusFromWebhook(payment.getId(), "COMPLETED", session.getId());
            log.info("✅ Payment {} marked as COMPLETED", payment.getId());

            // Create enrollment for the user
            enrollmentService.createEnrollmentFromWebhook(userId, courseId, session.getId());
            log.info("✅ Enrollment created for user {} in course {}", userId, courseId);

            // Record discount usage if discount was applied
            if (discountCode != null && !discountCode.trim().isEmpty() && discountAmountStr != null) {
                try {
                    BigDecimal discountAmount = new BigDecimal(discountAmountStr);
                    discountPriceService.recordDiscountUsage(discountCode, userId, courseId, discountAmount,
                            payment.getId());
                    log.info("✅ Discount usage recorded for code: {} with amount: {}", discountCode, discountAmount);

                    // Note: Affiliate payout will be created when payment is actually paid out to
                    // instructor
                    // This ensures commission is only paid when payment is confirmed and processed

                } catch (Exception discountException) {
                    log.error("❌ Failed to record discount usage for code: {} - {}", discountCode,
                            discountException.getMessage());
                    // Don't fail the whole webhook processing for discount recording failure
                }
            }

            // Send payment confirmation email asynchronously
            sendPaymentConfirmationEmail(session, courseId, userId);

        } catch (Exception e) {
            log.error("Error processing checkout session completion: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process checkout session completion", e);
        }
    }

    /**
     * Sends payment confirmation email with course details
     */
    public void sendPaymentConfirmationEmail(Session session, String courseId, String userId) {
        try {
            log.info("📧 Sending payment confirmation email for session: {}", session.getId());

            // Get user details
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.error("User not found with ID: {}", userId);
                return;
            }

            // Get course details with instructor
            Course course = courseRepository.findPublishedCourseByIdWithDetails(courseId).orElse(null);
            if (course == null) {
                log.error("Course not found with ID: {}", courseId);
                return;
            }

            // Format amount (Stripe amounts are in cents)
            String formattedAmount = String.format("$%.2f", session.getAmountTotal() / 100.0);

            // Get payment method info
            String paymentMethod = getPaymentMethodFromSession(session);

            // Build course URL
            String courseUrl = String.format("https://ktc-learning.com/courses/%s", course.getId());

            // Calculate course duration and lesson count
            String courseDuration = calculateCourseDuration(course);
            String lessonCount = String.valueOf(countCourseLessons(course));

            // Send async email
            emailService.sendPaymentConfirmationEmailAsync(
                    user.getEmail(),
                    user.getName(),
                    course.getTitle(),
                    courseUrl,
                    course.getInstructor() != null ? course.getInstructor().getName() : "KTC Learning",
                    course.getLevel() != null ? course.getLevel().toString() : "Beginner",
                    courseDuration,
                    lessonCount,
                    formattedAmount,
                    session.getPaymentIntent(),
                    paymentMethod,
                    java.time.LocalDateTime.now());

            log.info("✅ Payment confirmation email sent successfully to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to send payment confirmation email: {}", e.getMessage(), e);
            // Don't throw exception to avoid disrupting the main payment flow
        }
    }

    /**
     * Extracts payment method from Stripe session
     */
    private String getPaymentMethodFromSession(Session session) {
        try {
            if (session.getPaymentMethodTypes() != null && !session.getPaymentMethodTypes().isEmpty()) {
                String paymentType = session.getPaymentMethodTypes().get(0);
                return formatPaymentMethodName(paymentType);
            }
            return "Card";
        } catch (Exception e) {
            log.warn("Could not extract payment method from session: {}", e.getMessage());
            return "Card";
        }
    }

    /**
     * Formats payment method name for display
     */
    private String formatPaymentMethodName(String paymentType) {
        switch (paymentType.toLowerCase()) {
            case "card":
                return "Credit/Debit Card";
            case "paypal":
                return "PayPal";
            case "bank_transfer":
                return "Bank Transfer";
            case "ideal":
                return "iDEAL";
            default:
                return paymentType.substring(0, 1).toUpperCase() + paymentType.substring(1);
        }
    }

    /**
     * Calculates total course duration from all video lessons
     */
    private String calculateCourseDuration(Course course) {
        try {
            if (course.getSections() == null || course.getSections().isEmpty()) {
                return "0 minutes";
            }

            int totalDurationMinutes = course.getSections().stream()
                    .flatMap(section -> section.getLessons().stream())
                    .filter(lesson -> lesson.getContent() != null)
                    .mapToInt(
                            lesson -> lesson.getContent().getDuration() != null ? lesson.getContent().getDuration() / 60
                                    : 0)
                    .sum();

            if (totalDurationMinutes < 60) {
                return totalDurationMinutes + " minutes";
            } else {
                int hours = totalDurationMinutes / 60;
                int minutes = totalDurationMinutes % 60;
                return String.format("%d hours %d minutes", hours, minutes);
            }
        } catch (Exception e) {
            log.warn("Could not calculate course duration: {}", e.getMessage());
            return "Not specified";
        }
    }

    /**
     * Counts total number of lessons in a course
     */
    private int countCourseLessons(Course course) {
        try {
            if (course.getSections() == null || course.getSections().isEmpty()) {
                return 0;
            }

            return course.getSections().stream()
                    .mapToInt(section -> section.getLessons() != null ? section.getLessons().size() : 0)
                    .sum();
        } catch (Exception e) {
            log.warn("Could not count course lessons: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Handles expired checkout sessions
     */
    private void handleCheckoutSessionExpired(Event event) {
        log.info("⏰ Processing expired checkout session event");
        Session session = null;

        try {
            // Primary: Try to deserialize session from event
            session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

            // Fallback: If session is null, try to extract session ID and retrieve it
            if (session == null) {
                log.warn("⚠️ Session deserialization failed, attempting to retrieve session manually");
                String sessionId = extractSessionIdFromEventData(event);
                if (sessionId != null) {
                    session = Session.retrieve(sessionId);
                    log.info("✅ Successfully retrieved session {} using fallback method", sessionId);
                }
            }

            if (session == null) {
                log.error("❌ Could not retrieve session from checkout.session.expired event after all attempts");
                return;
            }

            log.info("🕒 Processing expired checkout session: {}", session.getId());

            // Find payment by session ID and update status to FAILED
            var paymentOpt = paymentService.findPaymentBySessionIdAndVerifyAmount(session.getId(), 0.0);
            if (paymentOpt.isPresent()) {
                var payment = paymentOpt.get();
                paymentService.updatePaymentStatusFromWebhook(payment.getId(), "FAILED", session.getId());
                log.info("❌ Payment {} marked as FAILED due to session expiration", payment.getId());
            } else {
                log.warn("⚠️ No payment found for expired session: {}", session.getId());
            }

        } catch (Exception e) {
            log.error("❌ Error processing checkout session expiration: {}", e.getMessage(), e);
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

    /**
     * Retrieves a Stripe checkout session by session ID
     * 
     * @param sessionId The Stripe session ID
     * @return The Stripe Session object, or null if not found or error occurs
     */
    public Session getSessionById(String sessionId) {
        try {
            log.info("🔍 Retrieving Stripe session with ID: {}", sessionId);

            if (sessionId == null || sessionId.trim().isEmpty()) {
                log.error("❌ Session ID cannot be null or empty");
                return null;
            }

            Session session = Session.retrieve(sessionId);

            if (session != null) {
                log.info("✅ Successfully retrieved session: {}", sessionId);
                log.debug("📋 Session details - Status: {}, Payment Status: {}, Amount: {} cents",
                        session.getStatus(), session.getPaymentStatus(), session.getAmountTotal());
            } else {
                log.warn("⚠️ Session not found: {}", sessionId);
            }

            return session;

        } catch (Exception e) {
            log.error("❌ Error retrieving session {}: {}", sessionId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Creates affiliate payout asynchronously for referral discounts
     */
    private void createAffiliatePayoutAsync(String discountCode, String userId, String courseId,
            BigDecimal finalPrice) {
        log.info("💰 Checking for affiliate payout eligibility - discount: {}", discountCode);

        try {
            // Find the discount usage record that was just created
            var discountUsages = discountUsageRepository.findByUserIdAndCourseId(userId, courseId);
            if (discountUsages.isEmpty()) {
                log.warn("No discount usage found for user: {} and course: {}", userId, courseId);
                return;
            }

            // Find the most recent usage for this discount code
            DiscountUsage relevantUsage = discountUsages.stream()
                    .filter(usage -> usage.getDiscount().getCode().equalsIgnoreCase(discountCode))
                    .findFirst()
                    .orElse(null);

            if (relevantUsage == null) {
                log.warn("No discount usage found for code: {}", discountCode);
                return;
            }

            // Only create payout for REFERRAL type discounts
            if (relevantUsage.getDiscount().getType() == DiscountType.REFERRAL) {
                log.info("🎉 Creating affiliate payout for referral discount: {}", discountCode);

                // Create payout asynchronously (final price is already after discount)
                affiliatePayoutService.createPayoutAsync(relevantUsage, finalPrice, relevantUsage.getDiscount().getId())
                        .thenAccept(payout -> {
                            log.info("✅ Affiliate payout created successfully: {} for amount: ${}",
                                    payout.getId(), payout.getCommissionAmount());
                        })
                        .exceptionally(throwable -> {
                            log.error("❌ Failed to create affiliate payout: {}", throwable.getMessage());
                            return null;
                        });
            } else {
                log.debug("📄 Discount is not REFERRAL type, skipping payout creation");
            }

        } catch (Exception e) {
            log.error("❌ Error in affiliate payout creation: {}", e.getMessage(), e);
            // Don't fail webhook processing for payout errors
        }
    }
}
