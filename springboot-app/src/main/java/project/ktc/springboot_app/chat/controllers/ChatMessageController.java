package project.ktc.springboot_app.chat.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.chat.dtos.ChatMessageResponse;
import project.ktc.springboot_app.chat.dtos.SendMessageRequest;
import project.ktc.springboot_app.chat.dtos.ChatMessagesListResponse;
import project.ktc.springboot_app.chat.interfaces.ChatMessageService;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR')")
@Tag(name = "Chat API", description = "API for real-time course chat messaging")
public class ChatMessageController {

        private final ChatMessageService chatMessageService;

        @PostMapping("/{courseId}/messages")
        @Operation(summary = "Send a chat message", description = "Send a message to a course chat. User must be enrolled in the course or be the instructor.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Message sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatMessageResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated"),
                        @ApiResponse(responseCode = "403", description = "User not enrolled in course or insufficient permissions"),
                        @ApiResponse(responseCode = "404", description = "Course not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<ChatMessageResponse>> sendMessage(
                        @Parameter(description = "Course ID", required = true, example = "7200a420-2ff3-4f18-9933-1b86d05f1a78") @PathVariable String courseId,
                        @Parameter(description = "Message content", required = true) @Valid @RequestBody SendMessageRequest request,
                        Authentication authentication) {
                String email = authentication.getName();
                return chatMessageService.sendMessage(courseId, email, request);
        }

        @GetMapping("/{courseId}/messages")
        @Operation(summary = "Get course chat messages", description = "Retrieve paginated list of messages for a course. User must be enrolled in the course or be the instructor. Supports both regular pagination and infinite scroll with keyset pagination.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated"),
                        @ApiResponse(responseCode = "403", description = "User not enrolled in course"),
                        @ApiResponse(responseCode = "404", description = "Course not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<ChatMessagesListResponse>> getMessages(
                        @Parameter(description = "Course ID", required = true, example = "7200a420-2ff3-4f18-9933-1b86d05f1a78") @PathVariable String courseId,
                        @Parameter(description = "Page number (0-based), ignored if beforeMessageId or afterMessageId is provided", example = "0") @RequestParam(defaultValue = "0") @Min(0) Integer page,
                        @Parameter(description = "Number of messages per page", example = "50") @RequestParam(defaultValue = "50") @Min(1) @Max(100) Integer size,
                        @Parameter(description = "Fetch messages created before this message ID (keyset pagination)", example = "123") @RequestParam(required = false) String beforeMessageId,
                        @Parameter(description = "Fetch messages created after this message ID (keyset pagination)", example = "456") @RequestParam(required = false) String afterMessageId,
                        Authentication authentication) {
                String email = authentication.getName();
                return chatMessageService.getMessages(courseId, email, page, size, beforeMessageId, afterMessageId);
        }
}
