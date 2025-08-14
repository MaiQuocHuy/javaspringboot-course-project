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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.comment.dto.CommentResponse;
import project.ktc.springboot_app.comment.dto.CreateCommentRequest;
import project.ktc.springboot_app.comment.dto.UpdateCommentRequest;
import project.ktc.springboot_app.comment.services.CommentServiceImp;
import project.ktc.springboot_app.common.dto.ApiErrorResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.utils.SecurityUtil;
import project.ktc.springboot_app.auth.entitiy.User;

@RestController
@RequestMapping("/api/lessons/{lessonId}/comments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comments", description = "API for managing lesson comments")
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

    private final CommentServiceImp commentService;

    @Operation(summary = "Create a new comment", description = "Create a new comment for a lesson. Can be a root comment or a reply to an existing comment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created successfully", content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Lesson or parent comment not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Comment depth limit exceeded", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CommentResponse>> createComment(
            @Parameter(description = "Lesson ID", required = true) @PathVariable String lessonId,
            @Parameter(description = "Comment creation request", required = true) @Valid @RequestBody CreateCommentRequest request) {

        String currentUserId = SecurityUtil.getCurrentUserId();
        log.info("Creating comment for lesson {} by user {}", lessonId, currentUserId);

        return commentService.createComment(lessonId, request, currentUserId);
    }

    @Operation(summary = "Get comments for a lesson", description = "Get paginated comments for a lesson with nested replies. Only root comments are paginated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Lesson not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<CommentResponse>>> getComments(
            @Parameter(description = "Lesson ID", required = true) @PathVariable String lessonId,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort direction for root comments", example = "desc") @RequestParam(defaultValue = "desc") String sort) {

        log.info("Fetching comments for lesson {} - page: {}, size: {}, sort: {}", lessonId, page, size, sort);

        // Create pageable with sorting by createdAt
        Sort.Direction direction = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        return commentService.getCommentsByLesson(lessonId, pageable);
    }

    @Operation(summary = "Update a comment", description = "Update an existing comment. Only the comment author can update their comment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment updated successfully", content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not the comment author", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{commentId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CommentResponse>> updateComment(
            @Parameter(description = "Lesson ID", required = true) @PathVariable String lessonId,
            @Parameter(description = "Comment ID", required = true) @PathVariable String commentId,
            @Parameter(description = "Comment update request", required = true) @Valid @RequestBody UpdateCommentRequest request) {

        String currentUserId = SecurityUtil.getCurrentUserId();
        log.info("Updating comment {} by user {}", commentId, currentUserId);

        return commentService.updateComment(commentId, request, currentUserId);
    }

    @Operation(summary = "Delete a comment", description = "Soft delete a comment. Comment authors can delete their own comments. Admins can delete any comment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not authorized to delete this comment", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> deleteComment(
            @Parameter(description = "Lesson ID", required = true) @PathVariable String lessonId,
            @Parameter(description = "Comment ID", required = true) @PathVariable String commentId,
            Authentication authentication) {

        String userId = ((User) authentication.getPrincipal()).getId();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        log.info("Deleting comment {} by user {} (admin: {})", commentId, userId, isAdmin);

        return commentService.deleteComment(commentId, userId, isAdmin);
    }

    @Operation(summary = "Get a specific comment", description = "Get details of a specific comment by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment retrieved successfully", content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{commentId}")
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CommentResponse>> getComment(
            @Parameter(description = "Lesson ID", required = true) @PathVariable String lessonId,
            @Parameter(description = "Comment ID", required = true) @PathVariable String commentId) {

        log.info("Fetching comment {} for lesson {}", commentId, lessonId);

        return commentService.getCommentById(commentId);
    }

    @Operation(summary = "Get comment count for a lesson", description = "Get the total number of comments (including replies) for a lesson")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Lesson not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/count")
    public ResponseEntity<Long> getCommentCount(
            @Parameter(description = "Lesson ID", required = true) @PathVariable String lessonId) {

        log.info("Fetching comment count for lesson {}", lessonId);

        Long count = commentService.getCommentCountByLesson(lessonId);
        return ResponseEntity.ok(count);
    }
}
