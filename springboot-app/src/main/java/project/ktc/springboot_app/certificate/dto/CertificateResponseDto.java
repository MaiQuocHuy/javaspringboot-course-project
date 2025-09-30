package project.ktc.springboot_app.certificate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for certificate response")
public class CertificateResponseDto {

  @Schema(description = "Certificate ID", example = "cert-789")
  private String id;

  @Schema(description = "Unique certificate code", example = "RC-2025-001-ABCD1234")
  private String certificateCode;

  @Schema(description = "Certificate issuance timestamp", example = "2025-01-30T10:30:00")
  private LocalDateTime issuedAt;

  @Schema(
      description = "URL to the certificate PDF file",
      example = "https://certificates.ktc.edu/downloads/cert-789.pdf")
  private String fileUrl;

  @Schema(description = "User information")
  private UserInfo user;

  @Schema(description = "Course information")
  private CourseInfo course;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "User information for certificate")
  public static class UserInfo {
    @Schema(description = "User ID", example = "user-123")
    private String id;

    @Schema(description = "User name", example = "John Doe")
    private String name;

    @Schema(description = "User email", example = "john.doe@example.com")
    private String email;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Course information for certificate")
  public static class CourseInfo {
    @Schema(description = "Course ID", example = "course-456")
    private String id;

    @Schema(description = "Course title", example = "Complete React Development Course")
    private String title;

    @Schema(description = "Course instructor name", example = "Jane Smith")
    private String instructorName;
  }
}
