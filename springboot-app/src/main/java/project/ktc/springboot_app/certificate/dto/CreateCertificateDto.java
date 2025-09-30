package project.ktc.springboot_app.certificate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating a new certificate")
public class CreateCertificateDto {

  @NotBlank(message = "User ID is required")
  @Schema(
      description = "ID of the user who completed the course",
      example = "user-123",
      required = true)
  private String userId;

  @NotBlank(message = "Course ID is required")
  @Schema(description = "ID of the completed course", example = "course-456", required = true)
  private String courseId;
}
