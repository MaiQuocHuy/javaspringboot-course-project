package project.ktc.springboot_app.chat.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import project.ktc.springboot_app.chat.validators.ValidAsyncMessageRequest;

@Data
@ValidAsyncMessageRequest
@Schema(description = "Request object for sending a chat message asynchronously")
public class AsyncSendMessageRequest {

    @NotBlank
    @Size(min = 1, max = 64)
    @Schema(description = "Temporary ID for tracking the message", example = "temp-123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tempId;

    @NotBlank
    @Pattern(regexp = "^(TEXT|FILE|VIDEO|AUDIO)$", message = "Type must be 'TEXT', 'FILE', 'VIDEO', or 'AUDIO'")
    @Schema(description = "Type of message", example = "TEXT", allowableValues = { "TEXT", "FILE", "VIDEO",
            "AUDIO" }, requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Size(max = 5000)
    @Schema(description = "Message content (required if type = 'TEXT')", example = "Hello world!")
    private String content;

    // For when file is already uploaded and URL is provided (recommended pattern)
    @Pattern(regexp = "^https://.*", message = "File URL must start with https://")
    @Schema(description = "File URL (for pre-uploaded files via /api/upload/* endpoints)", example = "https://res.cloudinary.com/example/raw/upload/v1234567890/documents/sample.pdf")
    private String fileUrl;

    @Size(max = 255)
    @Schema(description = "File name", example = "document.pdf")
    private String fileName;

    @Max(value = 104857600, message = "File size cannot exceed 100MB")
    @Schema(description = "File size in bytes (max 100MB)", example = "1024000")
    private Long fileSize;

    // Media-specific fields
    @Schema(description = "Video thumbnail URL (for video messages)", example = "https://res.cloudinary.com/example/image/upload/v1234567890/video_thumbnails/thumb.jpg")
    private String thumbnailUrl;

    @Min(value = 0, message = "Duration must be non-negative")
    @Schema(description = "Duration in seconds (for video/audio messages)", example = "120")
    private Integer duration;

    @Size(max = 100)
    @Schema(description = "MIME type of the file", example = "video/mp4")
    private String mimeType;

    @Size(max = 50)
    @Schema(description = "Video resolution (for video messages)", example = "1920x1080")
    private String resolution;
}
