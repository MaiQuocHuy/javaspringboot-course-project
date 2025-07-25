package project.ktc.springboot_app.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for video upload operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoUploadResponseDto {

    /**
     * Cloudinary public ID for the uploaded video
     */
    private String publicId;

    /**
     * Secure URL to access the video
     */
    private String secureUrl;

    /**
     * Original filename
     */
    private String originalFilename;

    /**
     * File size in bytes
     */
    private Long sizeInBytes;

    /**
     * Video duration in seconds (if available from Cloudinary)
     */
    private Double duration;

    /**
     * Video width in pixels
     */
    private Integer width;

    /**
     * Video height in pixels
     */
    private Integer height;

    /**
     * Video format (e.g., mp4, mov, avi)
     */
    private String format;

    /**
     * Upload timestamp
     */
    private LocalDateTime uploadedAt;

    /**
     * Cloudinary resource type (always "video" for videos)
     */
    private String resourceType;
}
