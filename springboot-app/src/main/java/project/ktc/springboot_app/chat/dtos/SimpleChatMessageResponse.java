package project.ktc.springboot_app.chat.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Simplified chat message response for the new API format")
public class SimpleChatMessageResponse {

    @Schema(description = "Message ID", example = "123")
    private String id;

    @Schema(description = "Sender user ID", example = "45")
    private String senderId;

    @Schema(description = "Message type", example = "text", allowableValues = { "text", "file" })
    private String type;

    @Schema(description = "Message content", example = "Hello world")
    private String content;

    @Schema(description = "File URL for file type messages", example = "https://example.com/file.pdf")
    private String fileUrl;

    @Schema(description = "File name for file type messages", example = "document.pdf")
    private String fileName;

    @Schema(description = "File size in bytes for file type messages", example = "1024")
    private Long fileSize;

    @Schema(description = "MIME type for file type messages", example = "application/pdf")
    private String mimeType;

    @Schema(description = "Message creation timestamp in ISO 8601 UTC format", example = "2025-08-26T15:30:00Z")
    private LocalDateTime createdAt;
}
