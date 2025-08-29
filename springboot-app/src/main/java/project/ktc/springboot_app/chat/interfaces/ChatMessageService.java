package project.ktc.springboot_app.chat.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.chat.dtos.ChatMessageResponse;
import project.ktc.springboot_app.chat.dtos.SendMessageRequest;
import project.ktc.springboot_app.chat.dtos.ChatMessagesListResponse;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;

/**
 * Service interface for chat message operations.
 * Following codebase patterns - services return ResponseEntity<ApiResponse<T>>,
 * controllers call them directly.
 */
public interface ChatMessageService {

        /**
         * Sends a message in a course chat.
         * 
         * @param courseId    The ID of the course
         * @param senderEmail The email of the message sender
         * @param request     The message content and details
         * @return ResponseEntity with the created chat message response
         */
        ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(String courseId, String senderEmail,
                        SendMessageRequest request);

        /**
         * Lists messages in a course chat with pagination.
         * 
         * @param courseId The ID of the course
         * @param type     Optional message type filter
         * @param pageable Pagination parameters
         * @return ResponseEntity with paginated list of chat messages
         */
        ResponseEntity<ApiResponse<PaginatedResponse<ChatMessageResponse>>> listMessages(String courseId, String type,
                        Pageable pageable);

        /**
         * Gets messages for a course with support for regular pagination and infinite
         * scroll.
         * 
         * @param courseId        The ID of the course
         * @param userEmail       The email of the requesting user
         * @param page            Page number (ignored if beforeMessageId or
         *                        afterMessageId is provided)
         * @param size            Number of messages per page
         * @param beforeMessageId Fetch messages created before this message ID (keyset
         *                        pagination)
         * @param afterMessageId  Fetch messages created after this message ID (keyset
         *                        pagination)
         * @return ResponseEntity with list of chat messages
         */
        ResponseEntity<ApiResponse<ChatMessagesListResponse>> getMessages(String courseId, String userEmail,
                        Integer page,
                        Integer size,
                        String beforeMessageId, String afterMessageId);

        /**
         * Deletes a specific chat message from a course.
         * Only the message owner or instructor/admin can delete it.
         * 
         * @param courseId  The ID of the course
         * @param messageId The ID of the message to delete
         * @param userEmail The email of the requesting user
         * @return ResponseEntity with no content on success
         */
        ResponseEntity<ApiResponse<Void>> deleteMessage(String courseId, String messageId, String userEmail);

        /**
         * Updates the content of a specific chat message from a course.
         * Only the message owner can update it and only TEXT messages can be updated.
         * 
         * @param courseId  The ID of the course
         * @param messageId The ID of the message to update
         * @param userEmail The email of the requesting user
         * @param request   The update request containing new content
         * @return ResponseEntity with updated chat message response
         */
        ResponseEntity<ApiResponse<ChatMessageResponse>> updateMessage(String courseId, String messageId,
                        String userEmail, project.ktc.springboot_app.chat.dtos.UpdateMessageRequest request);

        /**
         * Sends a message asynchronously in a course chat.
         * Returns immediate acknowledgment with PENDING status, then processes in
         * background.
         * 
         * @param courseId    The ID of the course
         * @param senderEmail The email of the message sender
         * @param request     The async message request with tempId
         * @return ResponseEntity with async acknowledgment
         */
        ResponseEntity<ApiResponse<project.ktc.springboot_app.chat.dtos.AsyncMessageAcknowledgment>> sendMessageAsync(
                        String courseId, String senderEmail,
                        project.ktc.springboot_app.chat.dtos.AsyncSendMessageRequest request);
}
