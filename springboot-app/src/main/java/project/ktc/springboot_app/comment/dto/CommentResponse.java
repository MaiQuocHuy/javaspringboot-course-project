package project.ktc.springboot_app.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response DTO for comment with nested set model support")
public class CommentResponse {

    @Schema(description = "Comment ID", example = "comment-uuid-123")
    private String id;

    @Schema(description = "Comment content", example = "This is a great lesson!")
    private String content;

    @Schema(description = "Comment depth level (0=root, 1=reply, 2=reply-to-reply, etc.)", example = "0", minimum = "0")
    private Integer depth;

    @Schema(description = "Relative depth from a specific ancestor (used in subtree queries)", example = "0", minimum = "0")
    private Integer relativeDepth;

    @Schema(description = "Left value in nested set model (for internal use)", example = "1")
    private Integer lft;

    @Schema(description = "Right value in nested set model (for internal use)", example = "8")
    private Integer rgt;

    @Schema(description = "Whether comment has been edited", example = "false")
    private Boolean isEdited;

    @Schema(description = "Whether comment has been deleted", example = "false")
    private Boolean isDeleted;

    @Schema(description = "Number of direct and indirect replies to this comment", example = "3")
    private Integer replyCount;

    @Schema(description = "Whether this comment has any replies", example = "true")
    private Boolean hasReplies;

    @Schema(description = "Whether this comment is a leaf node (no children)", example = "false")
    private Boolean isLeaf;

    @Schema(description = "Parent comment ID if this is a reply", example = "parent-comment-uuid")
    private String parentId;

    @Schema(description = "User information")
    private UserSummary user;

    @Schema(description = "List of child comments (only included when specifically requested)")
    private List<CommentResponse> children;

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

    // Helper methods for nested set operations
    public boolean isRootComment() {
        return depth == 0;
    }

    public boolean canHaveReplies() {
        return !isDeleted;
    }

    /**
     * Calculate actual reply count from nested set values
     */
    public int calculateReplyCount() {
        if (lft == null || rgt == null) {
            return 0;
        }
        return (rgt - lft - 1) / 2;
    }

    /**
     * Set relative depth for subtree queries
     */
    public void setRelativeDepthFromAncestor(CommentResponse ancestor) {
        if (ancestor == null) {
            this.relativeDepth = this.depth;
        } else {
            this.relativeDepth = this.depth - ancestor.depth;
        }
    }
}
