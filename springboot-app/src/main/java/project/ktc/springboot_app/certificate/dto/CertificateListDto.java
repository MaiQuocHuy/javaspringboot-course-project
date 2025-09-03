package project.ktc.springboot_app.certificate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for certificate list item")
public class CertificateListDto {

    @Schema(description = "Certificate ID", example = "cert-789")
    private String id;

    @Schema(description = "Unique certificate code", example = "RC-2025-001-ABCD1234")
    private String certificateCode;

    @Schema(description = "Certificate issuance timestamp", example = "2025-01-30T10:30:00")
    private LocalDateTime issuedAt;

    @Schema(description = "User name", example = "John Doe")
    private String userName;

    @Schema(description = "User email", example = "john.doe@example.com")
    private String userEmail;

    @Schema(description = "Course title", example = "Complete React Development Course")
    private String courseTitle;

    @Schema(description = "Course instructor name", example = "Jane Smith")
    private String instructorName;

    @Schema(description = "Certificate file status", example = "GENERATED")
    private String fileStatus;
}
