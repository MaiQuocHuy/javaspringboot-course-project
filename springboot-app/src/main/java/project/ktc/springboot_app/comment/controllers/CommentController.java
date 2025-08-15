package project.ktc.springboot_app.comment.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.comment.dto.CommentResponse;
import project.ktc.springboot_app.comment.dto.CreateCommentRequest;
import project.ktc.springboot_app.comment.dto.UpdateCommentRequest;
import project.ktc.springboot_app.comment.services.CommentServiceImp;
import project.ktc.springboot_app.common.dto.ApiErrorResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.utils.SecurityUtil;

import java.util.List;

@RestController
@RequestMapping("/api/lessons/{lessonId}/comments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Nested Set Comments", description = "Production-ready threaded comment system using Nested Set Model")
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

        private final CommentServiceImp commentService;

        // =================== CORE COMMENT OPERATIONS ===================

        @Operation(summary = "Create a threaded comment", description = "Create a new comment or reply using the Nested Set Model. Supports unlimited nesting depth with optimized performance.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Comment created successfully", content = @Content(schema = @Schema(implementation = CommentResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Lesson or parent comment not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
        })
        @PostMapping
        @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CommentResponse>> createComment(
                        @Parameter(description = "Lesson ID", required = true) @PathVariable String lessonId,
                        @Parameter(description = "Comment creation request", required = true) @Valid @RequestBody CreateCommentRequest request) {

                String currentUserId = SecurityUtil.getCurrentUserId();

                return commentService.createComment(lessonId, request, currentUserId);
        }

        @Operation(summary = "Get paginated root comments", description = "Get root comments with pagination for initial page load.")
        @GetMapping("/roots")
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<CommentResponse>>> getRootCommentsPaginated(
                        @Parameter(description = "Lesson ID", required = true) @PathVariable String lessonId,
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sort) {

                Sort.Direction direction = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

                return commentService.getCommentsByLesson(lessonId, pageable);
        }

        @Operation(summary = "Get comment replies", description = "Get all replies (subtree) for a specific comment using efficient nested set queries.")
        @GetMapping("/{commentId}/replies")
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<CommentResponse>>> getCommentReplies(
                        @Parameter(description = "Lesson ID", required = true) @PathVariable String lessonId,
                        @Parameter(description = "Comment ID", required = true) @PathVariable String commentId) {

                List<CommentResponse> replies = commentService.getReplies(commentId);
                return ApiResponseUtil.success(replies, "Replies retrieved successfully");
        }

        @Operation(summary = "Update comment content", description = "Update an existing comment. Only the comment author can update their comment.")
        @PutMapping("/{commentId}")
        @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CommentResponse>> updateComment(
                        @Parameter(description = "Lesson ID", required = true) @PathVariable String lessonId,
                        @Parameter(description = "Comment ID", required = true) @PathVariable String commentId,
                        @Parameter(description = "Comment update request", required = true) @Valid @RequestBody UpdateCommentRequest request) {

                String currentUserId = SecurityUtil.getCurrentUserId();
                return commentService.updateComment(commentId, request, currentUserId);
        }

        @Operation(summary = "Delete comment subtree", description = "Soft delete a comment and all its replies. Only the comment author can delete.")
        @DeleteMapping("/{commentId}")
        @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> deleteComment(
                        @Parameter(description = "Lesson ID", required = true) @PathVariable String lessonId,
                        @Parameter(description = "Comment ID", required = true) @PathVariable String commentId) {

                String currentUserId = SecurityUtil.getCurrentUserId();
                boolean isAdmin = SecurityUtil.getCurrentUserRoles()
                                .contains(project.ktc.springboot_app.auth.enums.UserRoleEnum.ADMIN);
                return commentService.deleteComment(commentId, currentUserId, isAdmin);
        }

        @Operation(summary = "Get comment count", description = "Get total number of comments for a lesson")
        @GetMapping("/count")
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Long>> getCommentCount(
                        @Parameter(description = "Lesson ID", required = true) @PathVariable String lessonId) {

                Long count = commentService.getCommentCountByLesson(lessonId);
                return ApiResponseUtil.success(count, "Comment count retrieved successfully");
        }
}
