package project.ktc.springboot_app.stripe.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a Stripe Checkout Session
 */
@Data
public class CreateCheckoutSessionRequest {

    @NotBlank(message = "Course ID is required")
    private String courseId;
}
