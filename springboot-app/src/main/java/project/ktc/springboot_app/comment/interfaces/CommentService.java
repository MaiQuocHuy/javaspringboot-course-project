package project.ktc.springboot_app.comment.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.comment.dto.CommentResponse;
import project.ktc.springboot_app.comment.dto.CreateCommentRequest;
import project.ktc.springboot_app.comment.dto.UpdateCommentRequest;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;

public interface CommentService {

  /**
   * Create a new comment for a lesson
   *
   * @param lessonId The lesson ID
   * @param request The comment creation request
   * @param userId The ID of the user creating the comment
   * @return The created comment response
   */
  ResponseEntity<ApiResponse<CommentResponse>> createComment(
      String lessonId, CreateCommentRequest request, String userId);

  /**
   * Get paginated comments for a lesson with nested replies
   *
   * @param lessonId The lesson ID
   * @param pageable Pagination information
   * @return Paginated response of comment responses with nested replies
   */
  ResponseEntity<ApiResponse<PaginatedResponse<CommentResponse>>> getCommentsByLesson(
      String lessonId, Pageable pageable);

  /**
   * Update an existing comment
   *
   * @param commentId The comment ID to update
   * @param request The update request
   * @param userId The ID of the user making the update
   * @return The updated comment response
   */
  ResponseEntity<ApiResponse<CommentResponse>> updateComment(
      String commentId, UpdateCommentRequest request, String userId);

  /**
   * Delete a comment (soft delete)
   *
   * @param commentId The comment ID to delete
   * @param userId The ID of the user requesting deletion
   * @param isAdmin Whether the user has admin privileges
   */
  ResponseEntity<ApiResponse<Void>> deleteComment(String commentId, String userId, boolean isAdmin);

  /**
   * Get a specific comment by ID
   *
   * @param commentId The comment ID
   * @return The comment response
   */
  ResponseEntity<ApiResponse<CommentResponse>> getCommentById(String commentId);

  /**
   * Get total comment count for a lesson
   *
   * @param lessonId The lesson ID
   * @return Total number of comments (including replies)
   */
  Long getCommentCountByLesson(String lessonId);
}
