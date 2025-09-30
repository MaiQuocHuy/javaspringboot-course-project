package project.ktc.springboot_app.comment.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.comment.dto.CommentResponse;
import project.ktc.springboot_app.comment.dto.CreateCommentRequest;
import project.ktc.springboot_app.comment.dto.UpdateCommentRequest;
import project.ktc.springboot_app.comment.entity.Comment;
import project.ktc.springboot_app.comment.interfaces.CommentService;
import project.ktc.springboot_app.comment.repositories.CommentRepository;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.exception.ResourceNotFoundException;
import project.ktc.springboot_app.common.exception.ValidationException;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.lesson.entity.Lesson;
import project.ktc.springboot_app.lesson.repositories.LessonRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;

/** Production-ready Nested Set Model Comment Service */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentServiceImp implements CommentService {

  private final CommentRepository commentRepository;
  private final LessonRepository lessonRepository;
  private final UserRepository userRepository;

  // =================== READ OPERATIONS ===================

  /** Get all comments for a lesson in flattened tree order */
  @Transactional(readOnly = true)
  public List<CommentResponse> getCommentsForLesson(String lessonId) {
    List<Comment> comments = commentRepository.findAllByLessonIdOrderByLft(lessonId);
    return comments.stream().map(this::convertToResponseDto).collect(Collectors.toList());
  }

  /** Get paginated root comments */
  @Transactional(readOnly = true)
  public PaginatedResponse<CommentResponse> getRootComments(String lessonId, Pageable pageable) {
    Page<Comment> comments = commentRepository.findRootCommentsByLessonId(lessonId, pageable);

    // Convert Page to PaginatedResponse
    List<CommentResponse> responseContent =
        comments.getContent().stream().map(this::convertToResponseDto).collect(Collectors.toList());

    return PaginatedResponse.<CommentResponse>builder()
        .content(responseContent)
        .page(
            PaginatedResponse.PageInfo.builder()
                .number(comments.getNumber())
                .size(comments.getSize())
                .totalPages(comments.getTotalPages())
                .totalElements(comments.getTotalElements())
                .first(comments.isFirst())
                .last(comments.isLast())
                .build())
        .build();
  }

  /** Get replies to a specific comment */
  @Transactional(readOnly = true)
  public List<CommentResponse> getReplies(String commentId) {
    Comment parent =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));

    List<Comment> replies =
        commentRepository.findSubtreeByParentLftRgt(
            parent.getLesson().getId(), parent.getLft(), parent.getRgt());

    return replies.stream()
        .map(
            comment -> {
              CommentResponse response = convertToResponseDto(comment);
              response.setRelativeDepth(comment.getDepth() - parent.getDepth() - 1);
              return response;
            })
        .collect(Collectors.toList());
  }

  /** Get comment count for a lesson */
  @Transactional(readOnly = true)
  public Long getCommentCount(String lessonId) {
    return commentRepository.countByLessonId(lessonId);
  }

  // =================== WRITE OPERATIONS ===================

  /** Create a new comment with atomic nested set insertion */
  private Comment addComment(String lessonId, String content, String parentId, String userId) {
    // Validate inputs
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    Lesson lesson =
        lessonRepository
            .findById(lessonId)
            .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));

    Comment newComment =
        Comment.builder().content(content).user(user).lesson(lesson).isDeleted(false).build();

    // Set ID manually since it's from BaseEntity
    newComment.setId(UUID.randomUUID().toString());

    if (parentId != null) {
      insertReply(newComment, parentId);
    } else {
      insertRootComment(newComment);
    }

    return commentRepository.save(newComment);
  }

  /** Insert a reply to an existing comment */
  private void insertReply(Comment newComment, String parentId) {
    Comment parent =
        commentRepository
            .findByIdForUpdate(parentId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Parent comment not found: " + parentId));

    int insertPosition = parent.getRgt() - 1;

    commentRepository.shiftRightValuesForInsertion(parent.getLesson().getId(), insertPosition);
    commentRepository.shiftLeftValuesForInsertion(parent.getLesson().getId(), insertPosition);

    newComment.setLft(insertPosition + 1);
    newComment.setRgt(insertPosition + 2);
    newComment.setParent(parent);
    newComment.setDepth(parent.getDepth() + 1);
  }

  /** Insert a root comment */
  private void insertRootComment(Comment newComment) {
    Integer maxRgt = commentRepository.findMaxRgtByLessonId(newComment.getLesson().getId());

    newComment.setLft(maxRgt + 1);
    newComment.setRgt(maxRgt + 2);
    newComment.setParent(null);
    newComment.setDepth(0);
  }

  /** Update comment content (internal method) */
  private Comment updateCommentInternal(String commentId, String content, String userId) {
    Comment comment =
        commentRepository
            .findByIdWithUser(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));

    if (!comment.getUser().getId().equals(userId)) {
      throw new ValidationException("User can only update their own comments");
    }

    if (comment.getIsDeleted()) {
      throw new ValidationException("Cannot update deleted comment");
    }

    comment.updateContent(content);

    return commentRepository.save(comment);
  }

  /** Soft delete a comment and its subtree (internal method) */
  @Transactional(isolation = Isolation.SERIALIZABLE)
  private void deleteCommentInternal(String commentId, String userId, boolean isAdmin) {
    Comment comment =
        commentRepository
            .findByIdWithUser(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));

    if (!isAdmin && !comment.getUser().getId().equals(userId)) {
      throw new ValidationException("User can only delete their own comments");
    }

    commentRepository.markSubtreeAsDeleted(
        comment.getLesson().getId(), comment.getLft(), comment.getRgt());
  }

  // =================== INTERFACE IMPLEMENTATION METHODS ===================

  @Override
  @Transactional
  public ResponseEntity<ApiResponse<CommentResponse>> createComment(
      String lessonId, CreateCommentRequest request, String userId) {
    Comment savedComment =
        addComment(lessonId, request.getContent(), request.getParentId(), userId);
    return convertToResponse(savedComment);
  }

  @Override
  @Transactional(readOnly = true)
  public ResponseEntity<ApiResponse<PaginatedResponse<CommentResponse>>> getCommentsByLesson(
      String lessonId, Pageable pageable) {
    PaginatedResponse<CommentResponse> response = getRootComments(lessonId, pageable);
    return ApiResponseUtil.success(response, "Comments retrieved successfully");
  }

  @Override
  @Transactional
  public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
      String commentId, UpdateCommentRequest request, String userId) {
    Comment updatedComment = updateCommentInternal(commentId, request.getContent(), userId);
    return convertToResponse(updatedComment);
  }

  @Override
  @Transactional
  public ResponseEntity<ApiResponse<Void>> deleteComment(
      String commentId, String userId, boolean isAdmin) {
    deleteCommentInternal(commentId, userId, isAdmin);
    return ApiResponseUtil.success(null, "Comment deleted successfully");
  }

  @Override
  @Transactional(readOnly = true)
  public ResponseEntity<ApiResponse<CommentResponse>> getCommentById(String commentId) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
    return convertToResponse(comment);
  }

  @Override
  @Transactional(readOnly = true)
  public Long getCommentCountByLesson(String lessonId) {
    return getCommentCount(lessonId);
  }

  // =================== UTILITY METHODS ===================

  /** Convert Comment entity to Response DTO (for internal use) */
  private CommentResponse convertToResponseDto(Comment comment) {
    CommentResponse.UserSummary user =
        CommentResponse.UserSummary.builder()
            .id(comment.getUser().getId())
            .name(comment.getUser().getName())
            .avatarUrl(comment.getUser().getThumbnailUrl())
            .build();
    return CommentResponse.builder()
        .id(comment.getId())
        .content(comment.getContent())
        .user(user)
        .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
        .depth(comment.getDepth())
        .lft(comment.getLft())
        .rgt(comment.getRgt())
        .isDeleted(comment.getIsDeleted())
        .isEdited(comment.getIsEdited())
        .createdAt(comment.getCreatedAt())
        .updatedAt(comment.getUpdatedAt())
        .replyCount(calculateReplyCount(comment))
        .hasReplies(calculateReplyCount(comment) > 0)
        .isLeaf(calculateReplyCount(comment) == 0)
        .build();
  }

  /** Convert Comment entity to Response DTO (for interface methods) */
  private ResponseEntity<ApiResponse<CommentResponse>> convertToResponse(Comment comment) {
    CommentResponse commentResponse = convertToResponseDto(comment);
    return ApiResponseUtil.success(commentResponse, "Comment retrieved successfully");
  }

  /** Calculate reply count from nested set values */
  private Integer calculateReplyCount(Comment comment) {
    if (comment.getLft() == null || comment.getRgt() == null) {
      return 0;
    }
    return (comment.getRgt() - comment.getLft() - 1) / 2;
  }
}
