package project.ktc.springboot_app.stripe.controllers;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

import io.swagger.v3.oas.annotations.tags.Tag;
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
import project.ktc.springboot_app.stripe.dto.CreateCheckoutSessionRequest;
import project.ktc.springboot_app.stripe.dto.CreateCheckoutSessionResponse;
import project.ktc.springboot_app.stripe.services.StripeCheckoutService;
import project.ktc.springboot_app.stripe.services.StripeWebhookService;
import project.ktc.springboot_app.utils.SecurityUtil;

import java.util.Optional;

/**
 * Controller for handling Stripe webhook events and checkout sessions
 */
@RestController
@RequestMapping("/api/stripe")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Stripe API", description = "Endpoints for managing Stripe payments and webhooks")
public class StripeController {

    private final StripeWebhookService stripeWebhookService;
    private final StripeCheckoutService stripeCheckoutService;
    private final CourseRepository courseRepository;

    /**
     * Creates a Stripe Checkout Session for course enrollment
     */
    @PostMapping("/create-checkout-session")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<CreateCheckoutSessionResponse>> createCheckoutSession(
            @RequestBody CreateCheckoutSessionRequest request) {

        try {
            // Get current user
            String currentUserId = SecurityUtil.getCurrentUserId();

            // Validate course
            Optional<Course> courseOpt = courseRepository.findById(request.getCourseId());
            if (courseOpt.isEmpty()) {
                return ApiResponseUtil.notFound("Course not found");
            }

            Course course = courseOpt.get();

            // Validate course is published and approved
            if (!course.getIsPublished() || !course.getIsApproved()) {
                return ApiResponseUtil.badRequest("Course is not available for purchase");
            }

            // Create checkout session
            Session session = stripeCheckoutService.createCheckoutSession(currentUserId, course);

            CreateCheckoutSessionResponse response = new CreateCheckoutSessionResponse();
            response.setSessionId(session.getId());
            response.setSessionUrl(session.getUrl());

            return ApiResponseUtil.success(response, "Checkout session created successfully");

        } catch (StripeException e) {
            log.error("Stripe error creating checkout session: {}", e.getMessage(), e);
            return ApiResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create checkout session: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error creating checkout session: {}", e.getMessage(), e);
            return ApiResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create checkout session");
        }
    }

    /**
     * Endpoint to receive Stripe webhook events
     * This endpoint should be registered in your Stripe Dashboard
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("Received Stripe webhook event");

        try {
            boolean processed = stripeWebhookService.processWebhookEvent(payload, sigHeader);

            if (processed) {
                log.info("Webhook event processed successfully");
                return ResponseEntity.ok("Webhook processed successfully");
            } else {
                log.error("Failed to process webhook event");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to process webhook");
            }

        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid signature");
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook processing error");
        }
    }

    /**
     * Health check endpoint for Stripe webhook
     */
    @GetMapping("/webhook/health")
    public ResponseEntity<String> webhookHealth() {
        return ResponseEntity.ok("Stripe webhook endpoint is healthy");
    }
}
