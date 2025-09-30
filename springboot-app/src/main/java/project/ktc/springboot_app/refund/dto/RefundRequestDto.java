package project.ktc.springboot_app.refund.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundRequestDto {
  @NotBlank(message = "Reason is required")
  @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
  private String reason;
}
