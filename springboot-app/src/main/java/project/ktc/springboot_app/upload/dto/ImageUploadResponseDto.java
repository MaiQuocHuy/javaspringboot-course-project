package project.ktc.springboot_app.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for image upload */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponseDto {

  /** Secure URL of the uploaded image */
  private String url;

  /** Public ID of the uploaded image in Cloudinary */
  private String publicId;

  /** Original filename */
  private String originalFilename;

  /** File size in bytes */
  private Long size;

  /** Image format (jpg, png, etc.) */
  private String format;

  /** Image width in pixels */
  private Integer width;

  /** Image height in pixels */
  private Integer height;
}
