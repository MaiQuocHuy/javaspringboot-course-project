package project.ktc.springboot_app.chat.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Simplified chat message response for the new API format")
public class SimpleChatMessageResponse {

	@Schema(description = "Message ID", example = "123")
	private String id;

	@Schema(description = "Sender user ID", example = "45")
	private String senderId;

	@Schema(description = "Sender user name", example = "John Doe")
	private String senderName;

	@Schema(description = "Sender thumbnail URL", example = "https://example.com/thumbnail.jpg")
	private String senderThumbnailUrl;

	@Schema(description = "Sender role", example = "STUDENT", allowableValues = { "STUDENT", "INSTRUCTOR" })
	private String senderRole;

	@Schema(description = "Message type", example = "TEXT", allowableValues = { "TEXT", "FILE" })
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
