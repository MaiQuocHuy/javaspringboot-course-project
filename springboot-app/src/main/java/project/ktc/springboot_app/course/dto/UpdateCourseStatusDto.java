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
public class UpdateCourseStatusDto {

  @NotBlank(message = "Status is required")
  @Pattern(
      regexp = "^(PUBLISHED|UNPUBLISHED)$",
      message = "Status must be either 'PUBLISHED' or 'UNPUBLISHED'")
  private String status;
}
