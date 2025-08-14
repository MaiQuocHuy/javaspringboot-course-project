package project.ktc.springboot_app.comment.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.comment.dto.CommentResponse;
import project.ktc.springboot_app.comment.dto.CreateCommentRequest;
import project.ktc.springboot_app.comment.dto.UpdateCommentRequest;
import project.ktc.springboot_app.comment.entity.Comment;
import project.ktc.springboot_app.comment.exception.CommentDepthExceededException;
import project.ktc.springboot_app.comment.exception.CommentOwnershipException;
import project.ktc.springboot_app.comment.interfaces.CommentService;
import project.ktc.springboot_app.comment.mapper.CommentMapper;
import project.ktc.springboot_app.comment.repositories.CommentRepository;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.lesson.entity.Lesson;
import project.ktc.springboot_app.lesson.repositories.LessonRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.exception.ResourceNotFoundException;
import project.ktc.springboot_app.common.exception.ValidationException;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentServiceImp implements CommentService {

    private final CommentRepository commentRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(String lessonId, CreateCommentRequest request,
            String userId) {
        log.info("Creating comment for lesson {} by user {}", lessonId, userId);

        // Validate and fetch lesson
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        // Validate and fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Validate content
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ValidationException("Comment content cannot be empty");
        }

        Comment comment = Comment.builder()
                .lesson(lesson)
                .user(user)
                .content(request.getContent().trim())
                .depth(0)
                .build();

        // Handle reply logic
        if (request.getParentId() != null && !request.getParentId().trim().isEmpty()) {
            Comment parentComment = commentRepository.findByIdWithUser(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent comment not found with id: " + request.getParentId()));
            // Check depth limit
            if (!parentComment.canAddReply()) {
                throw new CommentDepthExceededException("Cannot reply to this comment. Maximum depth exceeded.");
            }

            // Ensure parent belongs to same lesson
            if (!parentComment.getLesson().getId().equals(lessonId)) {
                throw new ValidationException("Parent comment does not belong to the specified lesson");
            }

            comment.setParent(parentComment);
            comment.setDepth(parentComment.getDepth() + 1);
        }

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created successfully with id: {}", savedComment.getId());

        return ApiResponseUtil.success(commentMapper.toCommentResponse(savedComment), "Comment created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PaginatedResponse<CommentResponse>>> getCommentsByLesson(String lessonId,
            Pageable pageable) {
        log.info("Fetching comments for lesson {} with pagination: page={}, size={}",
                lessonId, pageable.getPageNumber(), pageable.getPageSize());

        // Validate lesson exists
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException("Lesson not found with id: " + lessonId);
        }

        // Get paginated root comments
        Page<Comment> rootCommentsPage = commentRepository.findRootCommentsByLessonId(lessonId, pageable);

        if (rootCommentsPage.getContent().isEmpty()) {
            PaginatedResponse<CommentResponse> emptyResponse = PaginatedResponse.<CommentResponse>builder()
                    .content(List.of())
                    .page(PaginatedResponse.PageInfo.builder()
                            .number(pageable.getPageNumber())
                            .size(pageable.getPageSize())
                            .totalPages(0)
                            .totalElements(0)
                            .first(true)
                            .last(true)
                            .build())
                    .build();
            return ApiResponseUtil.success(emptyResponse, "Comments retrieved successfully");
        }

        // Get all root comment IDs for fetching replies
        List<String> rootCommentIds = rootCommentsPage.getContent().stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        // Fetch ALL replies in the lesson for the root comments (including nested
        // replies)
        List<Comment> allReplies = commentRepository.findAllRepliesInThread(lessonId, rootCommentIds);

        // Build comment tree structure recursively
        List<CommentResponse> commentResponses = rootCommentsPage.getContent().stream()
                .map(rootComment -> {
                    CommentResponse response = commentMapper.toCommentResponse(rootComment);

                    // Build nested replies recursively
                    List<CommentResponse> nestedReplies = buildNestedReplies(rootComment.getId(), allReplies);
                    response.setReplies(nestedReplies);
                    response.setReplyCount(countTotalReplies(rootComment.getId(), allReplies));

                    return response;
                })
                .collect(Collectors.toList());

        // Create paginated response using builder pattern
        PaginatedResponse<CommentResponse> paginatedResponse = PaginatedResponse.<CommentResponse>builder()
                .content(commentResponses)
                .page(PaginatedResponse.PageInfo.builder()
                        .number(rootCommentsPage.getNumber())
                        .size(rootCommentsPage.getSize())
                        .totalPages(rootCommentsPage.getTotalPages())
                        .totalElements(rootCommentsPage.getTotalElements())
                        .first(rootCommentsPage.isFirst())
                        .last(rootCommentsPage.isLast())
                        .build())
                .build();

        return ApiResponseUtil.success(paginatedResponse, "Comments retrieved successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(String commentId, UpdateCommentRequest request,
            String userId) {
        log.info("Updating comment {} by user {}", commentId, userId);

        Comment comment = commentRepository.findByIdWithUser(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        // Check ownership
        if (!comment.isOwnedBy(userId)) {
            throw new CommentOwnershipException("You do not have permission to update this comment");
        }

        // Check if comment is deleted
        if (comment.getIsDeleted()) {
            throw new ValidationException("Cannot update a deleted comment");
        }

        // Validate content
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ValidationException("Comment content cannot be empty");
        }

        // Update content and mark as edited if changed
        comment.updateContent(request.getContent().trim());

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment updated successfully with id: {}", savedComment.getId());

        return ApiResponseUtil.success(commentMapper.toCommentResponse(savedComment), "Comment updated successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteComment(String commentId, String userId, boolean isAdmin) {
        log.info("Deleting comment {} by user {} (admin: {})", commentId, userId, isAdmin);

        Comment comment = commentRepository.findByIdWithUser(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        // Check permissions
        if (!isAdmin && !comment.isOwnedBy(userId)) {
            throw new CommentOwnershipException("You do not have permission to delete this comment");
        }

        // Check if already deleted
        if (comment.getIsDeleted()) {
            throw new ValidationException("Comment is already deleted");
        }

        // Soft delete the comment
        comment.markAsDeleted();
        commentRepository.save(comment);

        log.info("Comment deleted successfully with id: {}", commentId);
        return ApiResponseUtil.success(null, "Comment deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<CommentResponse>> getCommentById(String commentId) {
        log.info("Fetching comment by id: {}", commentId);

        Comment comment = commentRepository.findByIdWithUser(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        return ApiResponseUtil.success(commentMapper.toCommentResponse(comment), "Comment retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCommentCountByLesson(String lessonId) {
        log.info("Counting comments for lesson: {}", lessonId);

        // Validate lesson exists
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException("Lesson not found with id: " + lessonId);
        }

        return commentRepository.countByLessonId(lessonId);
    }

    /**
     * Build nested replies recursively for a given parent comment
     */
    private List<CommentResponse> buildNestedReplies(String parentId, List<Comment> allReplies) {
        return allReplies.stream()
                .filter(reply -> reply.getParent() != null && reply.getParent().getId().equals(parentId))
                .map(reply -> {
                    CommentResponse response = commentMapper.toCommentResponse(reply);

                    // Recursively build nested replies for this reply
                    List<CommentResponse> nestedReplies = buildNestedReplies(reply.getId(), allReplies);
                    response.setReplies(nestedReplies);
                    response.setReplyCount(nestedReplies.size());

                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Count total replies recursively for a given parent comment
     */
    private int countTotalReplies(String parentId, List<Comment> allReplies) {
        List<Comment> directReplies = allReplies.stream()
                .filter(reply -> reply.getParent() != null && reply.getParent().getId().equals(parentId))
                .collect(Collectors.toList());

        int count = directReplies.size();

        // Add count of nested replies
        for (Comment reply : directReplies) {
            count += countTotalReplies(reply.getId(), allReplies);
        }

        return count;
    }
}
