package project.ktc.springboot_app.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseReviewStatusDto {

  @NotBlank(message = "Status is required")
  @Pattern(
      regexp = "^(APPROVED|REJECTED)$",
      message = "Status must be either 'APPROVED' or 'REJECTED'")
  private String status;

  private String reason; // Required when status is REJECTED
}
