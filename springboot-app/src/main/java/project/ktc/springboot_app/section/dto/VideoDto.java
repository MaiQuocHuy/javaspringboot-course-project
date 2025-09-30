package project.ktc.springboot_app.section.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto {
  private String id;
  private String url;
  private Integer duration;

  // Cloudinary metadata fields
  private String title;
  private String format;
  private Double metadataDuration; // Duration from Cloudinary metadata
  private String thumbnail;
  private Integer width;
  private Integer height;
  private Long sizeBytes;
}
