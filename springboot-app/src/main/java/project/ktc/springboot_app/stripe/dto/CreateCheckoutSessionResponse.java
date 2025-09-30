package project.ktc.springboot_app.stripe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for Stripe Checkout Session creation */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for checkout session creation")
public class CreateCheckoutSessionResponse {

	@Schema(description = "Stripe session ID", example = "cs_test_1234567890")
	private String sessionId;

	@Schema(description = "Stripe checkout URL", example = "https://checkout.stripe.com/pay/cs_test_1234567890")
	private String sessionUrl;

	@Schema(description = "Price calculation details")
	private PriceCalculationResponse priceCalculation;
}
