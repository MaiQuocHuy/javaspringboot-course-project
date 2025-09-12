package project.ktc.springboot_app.discount.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for discount email sending operation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response for discount email sending operation")
public class DiscountEmailResponse {

    @Schema(description = "Estimated number of student recipients", example = "250")
    private Long estimatedRecipients;

    @Schema(description = "Discount code that was sent", example = "DISCOUNT10")
    private String discountCode;

    @Schema(description = "Email subject that was used", example = "Special Discount Just for You!")
    private String subject;
}
