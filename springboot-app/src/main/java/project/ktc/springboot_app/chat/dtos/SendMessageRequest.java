package project.ktc.springboot_app.chat.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request object for sending a chat message")
public class SendMessageRequest {

    @NotBlank
    @Schema(description = "Type of message", example = "TEXT", allowableValues = { "TEXT", "FILE", "AUDIO",
            "VIDEO" }, requiredMode = Schema.RequiredMode.REQUIRED)
    private String type; // TEXT, FILE, AUDIO, VIDEO

    @NotBlank
    @Schema(description = "Message content. For TEXT messages this is the text content. For other types, this can be URL or base64 data", example = "Hello everyone! How are you doing with the course?", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content; // For TEXT; for others expect URL or base64 placeholder (further refinement
                            // later)

    @Schema(description = "File name (required for FILE, AUDIO, VIDEO types)", example = "document.pdf")
    private String fileName;

    @Schema(description = "File size in bytes (for FILE, AUDIO, VIDEO types)", example = "1024000")
    private Long fileSize;

    @Schema(description = "Duration in seconds (for AUDIO and VIDEO types)", example = "180")
    private Integer duration; // audio/video duration seconds

    @Schema(description = "Thumbnail URL for video messages", example = "https://example.com/thumbnails/video-thumb.jpg")
    private String thumbnailUrl; // video
}
