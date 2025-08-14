package project.ktc.springboot_app.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for comment with nested replies")
public class CommentResponse {

    @Schema(description = "Comment ID", example = "comment-uuid-123")
    private String id;

    @Schema(description = "Comment content", example = "This is a great lesson!")
    private String content;

    @Schema(description = "Comment depth level (0=root, 1=reply, 2=reply-to-reply)", example = "0", minimum = "0", maximum = "2")
    private Integer depth;

    @Schema(description = "Whether comment has been edited", example = "false")
    private Boolean isEdited;

    @Schema(description = "Whether comment has been deleted", example = "false")
    private Boolean isDeleted;

    @Schema(description = "Number of replies to this comment", example = "3")
    private Integer replyCount;

    @Schema(description = "Author information")
    private UserSummary author;

    @Schema(description = "List of replies (only for root comments and first-level replies)")
    private List<CommentResponse> replies;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Schema(description = "Comment creation timestamp", example = "2025-08-14T10:30:00.000")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Schema(description = "Comment last update timestamp", example = "2025-08-14T10:30:00.000")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User summary information")
    public static class UserSummary {
        @Schema(description = "User ID", example = "user-uuid-123")
        private String id;

        @Schema(description = "User name", example = "John Doe")
        private String name;

        @Schema(description = "User avatar URL", example = "https://example.com/avatar.jpg")
        private String avatarUrl;
    }
}
