package project.ktc.springboot_app.discount.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending discount codes via email to all students
 * Only requires subject and discount ID - backend will fetch discount details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to send discount code via email to all students")
public class DiscountEmailRequest {

    @NotBlank(message = "Discount ID is required")
    @Schema(description = "Discount ID to fetch discount details", example = "discount-uuid-123", required = true)
    @JsonProperty("discount_id")
    private String discountId;

    @NotBlank(message = "Subject is required")
    @Size(min = 5, max = 200, message = "Subject must be between 5 and 200 characters")
    @Schema(description = "Email subject", example = "Special Discount Just for You!", required = true)
    private String subject;
}
