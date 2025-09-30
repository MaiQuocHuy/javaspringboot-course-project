package project.ktc.springboot_app.instructor_application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for instructor application document upload request */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequestDto {

  @NotBlank(message = "Portfolio URL is required")
  @Pattern(
      regexp =
          "^(https?://)?(www\\.)?(github\\.com|linkedin\\.com|gitlab\\.com|bitbucket\\.org|[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})(/.*)?",
      message =
          "Portfolio must be a valid URL (GitHub, LinkedIn, GitLab, Bitbucket, or personal website)")
  private String portfolio;
}
