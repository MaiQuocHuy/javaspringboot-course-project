package project.ktc.springboot_app.stripe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for validating discount codes */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateDiscountRequest {

  @NotBlank(message = "Discount code is required")
  private String discountCode;

  @NotBlank(message = "Course ID is required")
  private String courseId;
}
