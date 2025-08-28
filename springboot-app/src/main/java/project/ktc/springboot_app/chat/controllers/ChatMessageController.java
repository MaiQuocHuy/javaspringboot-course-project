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
import project.ktc.springboot_app.chat.dtos.UpdateMessageRequest;
import project.ktc.springboot_app.chat.dtos.AsyncSendMessageRequest;
import project.ktc.springboot_app.chat.dtos.AsyncMessageAcknowledgment;
import project.ktc.springboot_app.chat.dtos.ChatMessagesListResponse;
import project.ktc.springboot_app.chat.services.ChatMessageServiceImp;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR')")
@Tag(name = "Chat API", description = "API for real-time course chat messaging")
public class ChatMessageController {

        private final ChatMessageServiceImp chatMessageService;

        @PostMapping("/{courseId}/messages")
        @Operation(summary = "Send a chat message asynchronously", description = "Send a message to a course chat with immediate acknowledgment and async processing. Returns PENDING status immediately, then sends confirmation via WebSocket.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "202", description = "Message acknowledged and being processed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AsyncMessageAcknowledgment.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated"),
                        @ApiResponse(responseCode = "403", description = "User not enrolled in course"),
                        @ApiResponse(responseCode = "404", description = "Course not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<AsyncMessageAcknowledgment>> sendMessage(
                        @Parameter(description = "Course ID", required = true, example = "7200a420-2ff3-4f18-9933-1b86d05f1a78") @PathVariable String courseId,
                        @Parameter(description = "Async message content with tempId", required = true) @Valid @RequestBody AsyncSendMessageRequest request,
                        Authentication authentication) {
                String email = authentication.getName();
                return chatMessageService.sendMessageAsync(courseId, email, request);
        }

        @PostMapping("/{courseId}/messages/sync")
        @Operation(summary = "Send a chat message synchronously", description = "Send a message to a course chat synchronously. User must be enrolled in the course or be the instructor.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Message sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatMessageResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated"),
                        @ApiResponse(responseCode = "403", description = "User not enrolled in course or insufficient permissions"),
                        @ApiResponse(responseCode = "404", description = "Course not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<ChatMessageResponse>> sendMessageSync(
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

        @DeleteMapping("/{courseId}/messages/{messageId}")
        @Operation(summary = "Delete a chat message", description = "Delete a specific chat message from a course. Only the message owner or instructor/admin can delete it.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Message deleted successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid courseId or messageId format"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated"),
                        @ApiResponse(responseCode = "403", description = "User is not the message owner or lacks permission"),
                        @ApiResponse(responseCode = "404", description = "Course or message not found"),
                        @ApiResponse(responseCode = "409", description = "Message already deleted"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> deleteMessage(
                        @Parameter(description = "Course ID", required = true, example = "7200a420-2ff3-4f18-9933-1b86d05f1a78") @PathVariable String courseId,
                        @Parameter(description = "Message ID", required = true, example = "7200a420-2ff3-4f18-9933-1b86d05f1a79") @PathVariable String messageId,
                        Authentication authentication) {
                String email = authentication.getName();
                return chatMessageService.deleteMessage(courseId, messageId, email);
        }

        @PatchMapping("/{courseId}/messages/{messageId}")
        @Operation(summary = "Update a chat message", description = "Update the content of a specific chat message from a course. Only the message owner can update it and only TEXT messages can be updated.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Message updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatMessageResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data or message type is not TEXT"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated"),
                        @ApiResponse(responseCode = "403", description = "User is not the message owner"),
                        @ApiResponse(responseCode = "404", description = "Course or message not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<ChatMessageResponse>> updateMessage(
                        @Parameter(description = "Course ID", required = true, example = "7200a420-2ff3-4f18-9933-1b86d05f1a78") @PathVariable String courseId,
                        @Parameter(description = "Message ID", required = true, example = "7200a420-2ff3-4f18-9933-1b86d05f1a79") @PathVariable String messageId,
                        @Parameter(description = "Updated message content", required = true) @Valid @RequestBody UpdateMessageRequest request,
                        Authentication authentication) {
                String email = authentication.getName();
                return chatMessageService.updateMessage(courseId, messageId, email, request);
        }
}
