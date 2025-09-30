package project.ktc.springboot_app.discount.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending discount codes via email Can send to all students, specific users, or a
 * single user based on userId/userIds parameters Only requires subject and discount ID - backend
 * will fetch discount details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description =
        "Request to send discount code via email to all students, specific users, or single user")
public class DiscountEmailRequest {

  @NotBlank(message = "Discount ID is required")
  @Schema(
      description = "Discount ID to fetch discount details",
      example = "discount-uuid-123",
      required = true)
  @JsonProperty("discount_id")
  private String discountId;

  @NotBlank(message = "Subject is required")
  @Size(min = 5, max = 200, message = "Subject must be between 5 and 200 characters")
  @Schema(
      description = "Email subject",
      example = "Special Discount Just for You!",
      required = true)
  private String subject;

  @Schema(
      description =
          "Optional user ID to send email to specific user. If not provided and userIds is also empty, sends to all students",
      example = "user-uuid-456",
      required = false)
  @JsonProperty("user_id")
  private String userId;

  @Schema(
      description =
          "Optional array of user IDs to send email to multiple specific users. Takes precedence over userId if both are provided",
      example = "[\"user-uuid-123\", \"user-uuid-456\", \"user-uuid-789\"]",
      required = false)
  @JsonProperty("user_ids")
  private List<String> userIds;
}
