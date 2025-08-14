package project.ktc.springboot_app.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a new comment")
public class CreateCommentRequest {

    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 2000, message = "Content must be between 1 and 2000 characters")
    @Schema(description = "Comment content", example = "This is a great lesson!", minLength = 1, maxLength = 2000)
    private String content;

    @Schema(description = "Parent comment ID for replies (null for root comments)", example = "parent-comment-uuid")
    private String parentId;
}
