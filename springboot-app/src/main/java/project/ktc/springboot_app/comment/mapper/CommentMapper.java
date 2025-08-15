package project.ktc.springboot_app.comment.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import project.ktc.springboot_app.comment.dto.CommentResponse;
import project.ktc.springboot_app.comment.entity.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentMapper {

    /**
     * Convert Comment entity to CommentResponse DTO
     * 
     * @param comment The comment entity
     * @return The comment response DTO
     */
    public CommentResponse toCommentResponse(Comment comment) {
        if (comment == null) {
            return null;
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .depth(comment.getDepth())
                .isEdited(comment.getIsEdited())
                .isDeleted(comment.getIsDeleted())
                .replyCount(comment.getReplies() != null ? comment.getReplies().size() : 0)
                .user(toUserSummary(comment))
                .children(new ArrayList<>()) // Will be populated separately
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    /**
     * Convert Comment entity to CommentResponse DTO with nested replies
     * 
     * @param comment        The comment entity
     * @param includeReplies Whether to include nested replies
     * @return The comment response DTO with replies
     */
    public CommentResponse toCommentResponseWithReplies(Comment comment, boolean includeReplies) {
        CommentResponse response = toCommentResponse(comment);

        if (includeReplies && comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            List<CommentResponse> replyResponses = comment.getReplies().stream()
                    .map(reply -> toCommentResponseWithReplies(reply, true)) // Recursively include replies
                    .collect(Collectors.toList());
            response.setChildren(replyResponses);
        }

        return response;
    }

    /**
     * Convert list of Comment entities to list of CommentResponse DTOs
     * 
     * @param comments The list of comment entities
     * @return The list of comment response DTOs
     */
    public List<CommentResponse> toCommentResponseList(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }

        return comments.stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Build comment tree structure from flat list of comments
     * Groups replies under their parent comments
     * 
     * @param comments Flat list of comments and replies
     * @return List of root comments with nested replies
     */
    public List<CommentResponse> buildCommentTree(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }

        // Separate root comments from replies
        List<Comment> rootComments = comments.stream()
                .filter(Comment::isRootComment)
                .collect(Collectors.toList());

        List<Comment> replies = comments.stream()
                .filter(Comment::isReply)
                .collect(Collectors.toList());

        // Convert root comments to DTOs
        List<CommentResponse> rootResponses = rootComments.stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());

        // Group replies by parent ID and attach to root comments
        for (CommentResponse rootResponse : rootResponses) {
            List<CommentResponse> commentReplies = replies.stream()
                    .filter(reply -> reply.getParent().getId().equals(rootResponse.getId()))
                    .map(this::toCommentResponse)
                    .collect(Collectors.toList());

            rootResponse.setChildren(commentReplies);
            rootResponse.setReplyCount(commentReplies.size());
        }

        return rootResponses;
    }

    /**
     * Create user summary from comment's user
     * 
     * @param comment The comment entity
     * @return The user summary DTO
     */
    private CommentResponse.UserSummary toUserSummary(Comment comment) {
        if (comment.getUser() == null) {
            return null;
        }

        return CommentResponse.UserSummary.builder()
                .id(comment.getUser().getId())
                .name(comment.getUser().getName())
                .avatarUrl(comment.getUser().getThumbnailUrl()) // Assuming this is the avatar URL field
                .build();
    }
}
