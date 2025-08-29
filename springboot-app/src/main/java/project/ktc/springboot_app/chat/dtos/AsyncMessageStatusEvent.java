package project.ktc.springboot_app.chat.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "WebSocket event for async message status updates")
public class AsyncMessageStatusEvent {

    @Schema(description = "Temporary ID from the original request", example = "temp-123")
    private String tempId;

    @Schema(description = "Actual message ID (if successful)", example = "msg-456")
    private String messageId;

    @Schema(description = "Status of the message", example = "SENT", allowableValues = { "PENDING", "UPLOADING", "SENT",
            "FAILED" })
    private String status;

    @Schema(description = "Error message (if status = FAILED)", example = "Database error")
    private String error;

    @Schema(description = "File URL (if media message)", example = "https://cloudinary.com/...")
    private String fileUrl;

    @Schema(description = "Thumbnail URL (for video messages)", example = "https://cloudinary.com/thumb.jpg")
    private String thumbnailUrl;

    @Schema(description = "Upload progress percentage (0-100)", example = "75")
    private Integer progress;

    @Schema(description = "Message type", example = "video", allowableValues = { "text", "file", "video", "audio" })
    private String type;
}
