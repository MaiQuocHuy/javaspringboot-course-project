package project.ktc.springboot_app.stripe.controllers;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.discount.interfaces.DiscountPriceService;
import project.ktc.springboot_app.payment.entity.Payment;
import project.ktc.springboot_app.payment.interfaces.PaymentService;
import project.ktc.springboot_app.stripe.dto.CreateCheckoutSessionRequest;
import project.ktc.springboot_app.stripe.dto.CreateCheckoutSessionResponse;
import project.ktc.springboot_app.stripe.dto.PaymentStatusResponse;
import project.ktc.springboot_app.stripe.dto.PriceCalculationResponse;
import project.ktc.springboot_app.stripe.services.StripeCheckoutService;
import project.ktc.springboot_app.stripe.services.StripeWebhookService;
import project.ktc.springboot_app.utils.SecurityUtil;

/** Controller for handling Stripe webhook events and checkout sessions */
@RestController
@RequestMapping("/api/stripe")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Stripe API", description = "Endpoints for managing Stripe payments and webhooks")
public class StripeController {

  private final StripeWebhookService stripeWebhookService;
  private final StripeCheckoutService stripeCheckoutService;
  private final CourseRepository courseRepository;
  private final PaymentService paymentService;
  private final DiscountPriceService discountPriceService;

  /** Creates a Stripe Checkout Session for course enrollment */
  @PostMapping("/create-checkout-session")
  @PreAuthorize("hasRole('STUDENT')")
  public ResponseEntity<ApiResponse<CreateCheckoutSessionResponse>> createCheckoutSession(
      @RequestBody CreateCheckoutSessionRequest request) {

    try {
      // Get current user
      String currentUserId = SecurityUtil.getCurrentUserId();

      // Validate course and calculate price with discount
      PriceCalculationResponse priceCalculation;
      try {
        priceCalculation =
            discountPriceService.calculatePrice(
                request.getCourseId(), currentUserId, request.getDiscountCode());
      } catch (IllegalArgumentException e) {
        log.warn("Price calculation failed: {}", e.getMessage());
        return ApiResponseUtil.badRequest(e.getMessage());
      }

      // Get course for session creation
      Course course =
          courseRepository
              .findById(request.getCourseId())
              .orElseThrow(() -> new IllegalArgumentException("Course not found"));

      // Check if user has already completed payment for this course
      Optional<Payment> completedPaymentOpt =
          paymentService.findCompletedPaymentByCourseIdAndUserId(course.getId(), currentUserId);
      if (completedPaymentOpt.isPresent()) {
        return ApiResponseUtil.badRequest("You have already purchased this course");
      }

      // Check if payment with courseId and user has exists
      Optional<Payment> paymentOpt =
          paymentService.findPaymentByCourseIdAndUserId(course.getId(), currentUserId);
      if (paymentOpt.isPresent()
          && !(paymentOpt.get().getStatus() == Payment.PaymentStatus.FAILED)) {
        return ApiResponseUtil.badRequest("Payment with courseId and user has already exists");
      }

      // Create checkout session with final price
      Session session =
          stripeCheckoutService.createCheckoutSessionWithDiscount(
              currentUserId, course, priceCalculation);

      CreateCheckoutSessionResponse response =
          CreateCheckoutSessionResponse.builder()
              .sessionId(session.getId())
              .sessionUrl(session.getUrl())
              .priceCalculation(priceCalculation)
              .build();

      return ApiResponseUtil.success(response, "Checkout session created successfully");

    } catch (StripeException e) {
      log.error("Stripe error creating checkout session: {}", e.getMessage(), e);
      return ApiResponseUtil.error(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create checkout session: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      log.warn("Invalid request for checkout session: {}", e.getMessage());
      return ApiResponseUtil.badRequest(e.getMessage());
    } catch (Exception e) {
      log.error("Error creating checkout session: {}", e.getMessage(), e);
      return ApiResponseUtil.error(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create checkout session");
    }
  }

  /**
   * Endpoint to receive Stripe webhook events This endpoint should be registered in your Stripe
   * Dashboard
   */
  @PostMapping("/webhook")
  public ResponseEntity<String> handleWebhook(
      @RequestBody String payload,
      @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

    log.info("========== STRIPE WEBHOOK RECEIVED ==========");
    log.info("Payload length: {}", payload != null ? payload.length() : 0);
    log.info("Signature header present: {}", sigHeader != null);
    log.info(
        "Payload preview: {}",
        payload != null && payload.length() > 100 ? payload.substring(0, 100) + "..." : payload);

    try {
      boolean processed = stripeWebhookService.processWebhookEvent(payload, sigHeader);

      if (processed) {
        log.info("✅ Webhook event processed successfully");
        return ResponseEntity.ok("Webhook processed successfully");
      } else {
        log.error("❌ Failed to process webhook event");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Failed to process webhook");
      }

    } catch (SignatureVerificationException e) {
      log.error("❌ Webhook signature verification failed: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
    } catch (Exception e) {
      log.error("❌ Error processing webhook: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Webhook processing error");
    }
  }

  /** Health check endpoint for Stripe webhook */
  @GetMapping("/webhook/health")
  public ResponseEntity<String> webhookHealth() {
    return ResponseEntity.ok("Stripe webhook endpoint is healthy");
  }

  /** Calculate price with discount validation (for frontend preview) */
  @PostMapping("/calculate-price")
  @Operation(
      summary = "Calculate price with discount",
      description = "Calculate final price with discount validation for preview purposes")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Price calculated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid course ID or discount code"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Course not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error")
      })
  public ResponseEntity<ApiResponse<PriceCalculationResponse>> calculatePrice(
      @Valid @RequestBody CreateCheckoutSessionRequest request) {
    try {
      String currentUserId = SecurityUtil.getCurrentUserId();
      log.info(
          "User {} calculating price for course: {} with discount: {}",
          currentUserId,
          request.getCourseId(),
          request.getDiscountCode());

      // Calculate price with discount
      PriceCalculationResponse priceResponse =
          discountPriceService.calculatePrice(
              request.getCourseId(), currentUserId, request.getDiscountCode());

      return ApiResponseUtil.success(priceResponse, "Price calculated successfully");

    } catch (IllegalArgumentException e) {
      log.warn("Price calculation validation error: {}", e.getMessage());
      return ApiResponseUtil.badRequest(e.getMessage());
    } catch (Exception e) {
      log.error("Error calculating price: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError(
          "Failed to calculate price. Please try again later.");
    }
  }

  /**
   * TEST ENDPOINT: Simulate a successful payment completion This is for development/testing
   * purposes only
   */
  @PostMapping("/test-payment-success/{sessionId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<String>> testPaymentSuccess(@PathVariable String sessionId) {
    log.info("🧪 TEST: Simulating payment success for session: {}", sessionId);

    try {
      // Create a mock webhook payload for checkout.session.completed
      String mockPayload =
          String.format(
              """
                    {
                      "id": "evt_test_%s",
                      "object": "event",
                      "api_version": "2020-08-27",
                      "created": %d,
                      "type": "checkout.session.completed",
                      "data": {
                        "object": {
                          "id": "%s",
                          "object": "checkout_session",
                          "amount_total": 9999,
                          "currency": "usd",
                          "customer_email": "test@example.com",
                          "metadata": {
                            "courseId": "course-001",
                            "userId": "user-002"
                          },
                          "payment_status": "paid",
                          "status": "complete"
                        }
                      }
                    }
                    """,
              System.currentTimeMillis(), System.currentTimeMillis() / 1000, sessionId);

      // Process the mock webhook (without signature verification)
      boolean processed = stripeWebhookService.processWebhookEvent(mockPayload, null);

      if (processed) {
        return ApiResponseUtil.success(
            "Payment simulation completed successfully",
            "Test payment processed for session: " + sessionId);
      } else {
        return ApiResponseUtil.error(
            HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process test payment");
      }

    } catch (Exception e) {
      log.error("Error in test payment simulation: {}", e.getMessage(), e);
      return ApiResponseUtil.error(
          HttpStatus.INTERNAL_SERVER_ERROR, "Test payment simulation failed: " + e.getMessage());
    }
  }

  @GetMapping("/payment-status/{sessionId}")
  @PreAuthorize("hasRole('STUDENT')")
  public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatus(
      @PathVariable String sessionId) {
    try {
      log.info("Getting payment status for session: {}", sessionId);

      // Get current user
      String currentUserId = SecurityUtil.getCurrentUserId();

      // Find payment by session ID
      Optional<Payment> paymentOpt = paymentService.findPaymentBySessionId(sessionId);

      if (paymentOpt.isEmpty()) {
        log.warn("Payment not found for session: {}", sessionId);
        return ApiResponseUtil.notFound("Payment not found for session: " + sessionId);
      }

      Payment payment = paymentOpt.get();

      // Verify that the payment belongs to the current user
      if (!payment.getUser().getId().equals(currentUserId)) {
        log.warn("Payment {} does not belong to current user {}", payment.getId(), currentUserId);
        return ApiResponseUtil.forbidden("You don't have permission to access this payment");
      }

      // Get course information
      Course course = payment.getCourse();
      PaymentStatusResponse.CourseInfo courseInfo = null;

      if (course != null) {
        courseInfo =
            PaymentStatusResponse.CourseInfo.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .price(course.getPrice().doubleValue())
                .build();
      }

      // Build response
      PaymentStatusResponse response =
          PaymentStatusResponse.builder()
              .id(payment.getId())
              .sessionId(payment.getSessionId())
              .courseId(payment.getCourse() != null ? payment.getCourse().getId() : null)
              .userId(payment.getUser() != null ? payment.getUser().getId() : null)
              .amount(payment.getAmount().doubleValue())
              .currency("USD") // Assuming USD for now
              .status(payment.getStatus().name())
              .createdAt(payment.getCreatedAt())
              .updatedAt(payment.getUpdatedAt())
              .course(courseInfo)
              .build();

      log.info("Payment status retrieved successfully for session: {}", sessionId);
      return ApiResponseUtil.success(response, "Payment status retrieved successfully");

    } catch (Exception e) {
      log.error("Error getting payment status for session {}: {}", sessionId, e.getMessage(), e);
      return ApiResponseUtil.error(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve payment status: " + e.getMessage());
    }
  }
}
