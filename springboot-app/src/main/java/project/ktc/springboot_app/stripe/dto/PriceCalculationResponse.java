package project.ktc.springboot_app.stripe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for price calculation with discount */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Price calculation result with discount information")
public class PriceCalculationResponse {

  @Schema(description = "Original course price", example = "99.99")
  private BigDecimal originalPrice;

  @Schema(description = "Discount amount applied", example = "9.99")
  private BigDecimal discountAmount;

  @Schema(description = "Final price after discount", example = "90.00")
  private BigDecimal finalPrice;

  @Schema(description = "Applied discount code", example = "WELCOME10")
  private String appliedDiscountCode;

  @Schema(description = "Discount percentage applied", example = "10.00")
  private BigDecimal discountPercent;

  @Schema(description = "Whether a discount was applied", example = "true")
  private boolean discountApplied;

  @Schema(description = "Currency code", example = "USD")
  @Builder.Default
  private String currency = "USD";
}
