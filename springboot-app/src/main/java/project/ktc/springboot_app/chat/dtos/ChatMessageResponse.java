package project.ktc.springboot_app.chat.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Chat message response object")
public class ChatMessageResponse {

	@Schema(description = "Unique message identifier", example = "7200a420-2ff3-4f18-9933-1b86d05f1a78")
	private String id;

	@Schema(description = "Course identifier this message belongs to", example = "course-uuid-123")
	private String courseId;

	@Schema(description = "User ID of the message sender", example = "user-uuid-456")
	private String senderId;

	@Schema(description = "Display name of the message sender", example = "John Doe")
	private String senderName;

	@Schema(description = "Role of the message sender", example = "STUDENT", allowableValues = { "STUDENT",
			"INSTRUCTOR", "ADMIN" })
	private String senderRole;

	@Schema(description = "Type of message", example = "TEXT", allowableValues = { "TEXT", "FILE", "AUDIO", "VIDEO" })
	private String type;

	@Schema(description = "Text content for TEXT type messages", example = "Hello everyone! How are you doing with the course?")
	private String content;

	@Schema(description = "File URL for FILE type messages", example = "https://example.com/files/document.pdf")
	private String fileUrl;

	@Schema(description = "Original file name for FILE type messages", example = "lecture-notes.pdf")
	private String fileName;

	@Schema(description = "File size in bytes for FILE type messages", example = "1024000")
	private Long fileSize;

	@Schema(description = "MIME type of the file for FILE type messages", example = "application/pdf")
	private String fileType;

	@Schema(description = "Audio file URL for AUDIO type messages", example = "https://example.com/audio/recording.mp3")
	private String audioUrl;

	@Schema(description = "Duration in seconds for AUDIO type messages", example = "180")
	private Integer audioDuration;

	@Schema(description = "Video file URL for VIDEO type messages", example = "https://example.com/videos/explanation.mp4")
	private String videoUrl;

	@Schema(description = "Video thumbnail URL for VIDEO type messages", example = "https://example.com/thumbnails/video-thumb.jpg")
	private String videoThumbnailUrl;

	@Schema(description = "Video duration in seconds for VIDEO type messages", example = "300")
	private Integer videoDuration;

	@Schema(description = "Message creation timestamp", example = "2025-08-26T10:30:00")
	private LocalDateTime createdAt;

	@Schema(description = "Thumbnail URL of the sender", example = "https://example.com/profiles/johndoe.jpg")
	private String senderThumbnailUrl;
}
