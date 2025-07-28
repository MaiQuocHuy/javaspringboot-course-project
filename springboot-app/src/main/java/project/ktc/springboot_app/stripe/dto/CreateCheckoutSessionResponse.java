package project.ktc.springboot_app.stripe.dto;

import lombok.Data;

/**
 * Response DTO for Stripe Checkout Session creation
 */
@Data
public class CreateCheckoutSessionResponse {

    private String sessionId;
    private String sessionUrl;
}
