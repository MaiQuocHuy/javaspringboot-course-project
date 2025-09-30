package project.ktc.springboot_app.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for document upload response from Cloudinary */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResponseDto {
  private String url;
  private String publicId;
  private String originalFilename;
  private Long size;
  private String resourceType;
  private String format;
}
