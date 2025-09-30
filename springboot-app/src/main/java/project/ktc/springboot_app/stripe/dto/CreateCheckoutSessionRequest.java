package project.ktc.springboot_app.stripe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Request DTO for creating a Stripe Checkout Session */
@Data
@Schema(description = "Request object for creating a checkout session")
public class CreateCheckoutSessionRequest {

	@NotBlank(message = "Course ID is required")
	@Schema(description = "Course ID to purchase", example = "course-001", required = true)
	private String courseId;

	@Size(max = 50, message = "Discount code cannot exceed 50 characters")
	@Pattern(regexp = "^[A-Z0-9\\-_]*$", message = "Discount code can only contain uppercase letters, numbers, hyphens, and underscores")
	@Schema(description = "Optional discount code to apply", example = "WELCOME10")
	private String discountCode;
}
