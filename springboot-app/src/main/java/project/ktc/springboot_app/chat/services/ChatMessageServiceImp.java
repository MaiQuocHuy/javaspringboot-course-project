package project.ktc.springboot_app.chat.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.chat.dtos.SendMessageRequest;
import project.ktc.springboot_app.chat.dtos.ChatMessageResponse;
import project.ktc.springboot_app.chat.dtos.SimpleChatMessageResponse;
import project.ktc.springboot_app.chat.dtos.ChatMessagesListResponse;
import project.ktc.springboot_app.chat.entities.*;
import project.ktc.springboot_app.chat.interfaces.ChatMessageService;
import project.ktc.springboot_app.chat.repositories.ChatMessageRepository;
import project.ktc.springboot_app.chat.repositories.ChatMessageTypeRepository;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImp implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageTypeRepository chatMessageTypeRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(String courseId, String senderEmail,
            SendMessageRequest request) {
        try {
            var course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new NoSuchElementException("Course not found"));
            var sender = userRepository.findByEmail(senderEmail)
                    .orElseThrow(() -> new NoSuchElementException("Sender not found"));
            var type = chatMessageTypeRepository.findByNameIgnoreCase(request.getType())
                    .orElseThrow(() -> new NoSuchElementException("Invalid message type"));

            // Validate access: user must be enrolled in the course OR be the course
            // instructor
            boolean isInstructor = course.getInstructor().getId().equals(sender.getId());
            boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(sender.getId(), courseId);

            if (!isInstructor && !isEnrolled) {
                return ApiResponseUtil.forbidden("User not authorized to send messages in this course");
            }

            ChatMessage message = ChatMessage.builder()
                    .course(course)
                    .sender(sender)
                    .senderRole(sender.getRole() != null ? sender.getRole().getRole() : "STUDENT")
                    .messageType(type)
                    .build();

            // Persist parent first to get ID
            message = chatMessageRepository.save(message);

            switch (type.getName().toUpperCase()) {
                case "TEXT" -> {
                    ChatMessageText text = ChatMessageText.builder()
                            .message(message)
                            .content(request.getContent())
                            .build();
                    message.setTextDetail(text);
                }
                case "FILE" -> {
                    ChatMessageFile file = ChatMessageFile.builder()
                            .message(message)
                            .fileUrl(request.getContent())
                            .fileName(request.getFileName())
                            .fileSize(request.getFileSize())
                            .mimeType(null) // Could be determined from file extension or passed in request
                            .build();
                    message.setFileDetail(file);
                }
                case "AUDIO" -> {
                    ChatMessageAudio audio = ChatMessageAudio.builder()
                            .message(message)
                            .audioUrl(request.getContent())
                            .fileName(request.getFileName())
                            .fileSize(request.getFileSize())
                            .duration(request.getDuration() != null ? request.getDuration().longValue() : null)
                            .mimeType(null)
                            .thumbnailUrl(null)
                            .build();
                    message.setAudioDetail(audio);
                }
                case "VIDEO" -> {
                    ChatMessageVideo video = ChatMessageVideo.builder()
                            .message(message)
                            .videoUrl(request.getContent())
                            .fileName(request.getFileName())
                            .fileSize(request.getFileSize())
                            .thumbnailUrl(request.getThumbnailUrl())
                            .duration(request.getDuration() != null ? request.getDuration().longValue() : null)
                            .mimeType(null)
                            .resolution(null) // Could be determined from video metadata
                            .build();
                    message.setVideoDetail(video);
                }
                default -> throw new IllegalArgumentException("Unsupported message type: " + type.getName());
            }

            // Save again with details (cascade persists detail)
            message = chatMessageRepository.save(message);

            ChatMessageResponse response = toResponse(message);
            messagingTemplate.convertAndSend("/topic/courses." + courseId + ".chat", response);

            return ApiResponseUtil.created(response, "Message sent successfully");

        } catch (NoSuchElementException e) {
            return ApiResponseUtil.notFound(e.getMessage());
        } catch (SecurityException e) {
            return ApiResponseUtil.forbidden(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponseUtil.internalServerError("Failed to send message. Please try again later.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PaginatedResponse<ChatMessageResponse>>> listMessages(String courseId,
            String type, Pageable pageable) {
        try {
            // Get the authenticated user to verify access
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null) {
                return ApiResponseUtil.unauthorized("No authenticated user found");
            }

            var course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new NoSuchElementException("Course not found"));
            var user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            // Validate access: user must be enrolled in the course OR be the course
            // instructor
            boolean isInstructor = course.getInstructor().getId().equals(user.getId());
            boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId);

            if (!isInstructor && !isEnrolled) {
                return ApiResponseUtil.forbidden("User not authorized to view messages in this course");
            }

            Page<ChatMessage> messagesPage;
            if (type == null || type.isBlank()) {
                messagesPage = chatMessageRepository.findByCourseId(courseId, pageable);
            } else {
                messagesPage = chatMessageRepository.findByCourseIdAndType(courseId, type.toUpperCase(), pageable);
            }

            List<ChatMessageResponse> messageDtos = messagesPage.getContent().stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            // Create paginated response
            PaginatedResponse<ChatMessageResponse> paginatedResponse = PaginatedResponse.<ChatMessageResponse>builder()
                    .content(messageDtos)
                    .page(PaginatedResponse.PageInfo.builder()
                            .number(messagesPage.getNumber())
                            .size(messagesPage.getSize())
                            .totalElements(messagesPage.getTotalElements())
                            .totalPages(messagesPage.getTotalPages())
                            .first(messagesPage.isFirst())
                            .last(messagesPage.isLast())
                            .build())
                    .build();

            return ApiResponseUtil.success(paginatedResponse, "Messages retrieved successfully");

        } catch (NoSuchElementException e) {
            return ApiResponseUtil.notFound(e.getMessage());
        } catch (SecurityException e) {
            return ApiResponseUtil.forbidden(e.getMessage());
        } catch (Exception e) {
            return ApiResponseUtil.internalServerError("Failed to retrieve messages. Please try again later.");
        }
    }

    private ChatMessageResponse toResponse(ChatMessage m) {
        return ChatMessageResponse.builder()
                .id(m.getId())
                .courseId(m.getCourse().getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getName())
                .senderRole(m.getSenderRole())
                .type(m.getMessageType().getName())
                .textContent(m.getTextDetail() != null ? m.getTextDetail().getContent() : null)
                .fileUrl(m.getFileDetail() != null ? m.getFileDetail().getFileUrl() : null)
                .fileName(m.getFileDetail() != null ? m.getFileDetail().getFileName() : null)
                .fileSize(m.getFileDetail() != null ? m.getFileDetail().getFileSize() : null)
                .fileType(m.getFileDetail() != null ? m.getFileDetail().getMimeType() : null)
                .audioUrl(m.getAudioDetail() != null ? m.getAudioDetail().getAudioUrl() : null)
                .audioDuration(m.getAudioDetail() != null && m.getAudioDetail().getDuration() != null
                        ? m.getAudioDetail().getDuration().intValue()
                        : null)
                .videoUrl(m.getVideoDetail() != null ? m.getVideoDetail().getVideoUrl() : null)
                .videoThumbnailUrl(m.getVideoDetail() != null ? m.getVideoDetail().getThumbnailUrl() : null)
                .videoDuration(m.getVideoDetail() != null && m.getVideoDetail().getDuration() != null
                        ? m.getVideoDetail().getDuration().intValue()
                        : null)
                .createdAt(m.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<ChatMessagesListResponse>> getMessages(String courseId, String userEmail,
            Integer page, Integer size,
            String beforeMessageId, String afterMessageId) {
        try {
            // Validate input parameters
            if (page < 0) {
                return ApiResponseUtil.badRequest("Invalid query parameters: page must be non-negative");
            }
            if (size < 1 || size > 100) {
                return ApiResponseUtil.badRequest("Invalid query parameters: size must be between 1 and 100");
            }
            // Verify course exists
            var course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new NoSuchElementException("Course not found"));

            // Verify user exists and access
            var user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            // Validate access: user must be enrolled in the course OR be the course
            // instructor
            boolean isInstructor = course.getInstructor().getId().equals(user.getId());
            boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId);

            if (!isInstructor && !isEnrolled) {
                return ApiResponseUtil.forbidden("User not enrolled in course");
            }

            List<ChatMessage> messages;
            Pageable pageable = PageRequest.of(0, size);

            // Determine pagination strategy
            if (beforeMessageId != null || afterMessageId != null) {
                // Use keyset pagination (infinite scroll)
                if (beforeMessageId != null && afterMessageId != null) {
                    return ApiResponseUtil.badRequest(
                            "Invalid query parameters: cannot specify both beforeMessageId and afterMessageId");
                }

                if (beforeMessageId != null) {
                    String beforeMessageUuid = beforeMessageId;
                    if (beforeMessageUuid == null) {
                        return ApiResponseUtil.badRequest("Invalid query parameters: beforeMessageId not found");
                    }
                    messages = chatMessageRepository.findByCourseIdBeforeMessageId(courseId, beforeMessageUuid,
                            pageable);
                } else {
                    String afterMessageUuid = afterMessageId;
                    if (afterMessageUuid == null) {
                        return ApiResponseUtil.badRequest("Invalid query parameters: afterMessageId not found");
                    }
                    messages = chatMessageRepository.findByCourseIdAfterMessageId(courseId, afterMessageUuid, pageable);
                }

                // For infinite scroll, return simple message list
                List<SimpleChatMessageResponse> messageList = messages.stream()
                        .map(this::toSimpleResponse)
                        .collect(Collectors.toList());

                ChatMessagesListResponse response = ChatMessagesListResponse.builder()
                        .messages(messageList)
                        .page(null) // Not applicable for keyset pagination
                        .size(size)
                        .totalElements(null) // Not applicable for keyset pagination
                        .totalPages(null) // Not applicable for keyset pagination
                        .build();

                return ApiResponseUtil.success(response, "Messages retrieved successfully");
            } else {
                // Use regular pagination
                pageable = PageRequest.of(page, size);
                messages = chatMessageRepository.findByCourseIdOrderByCreatedAtAsc(courseId, pageable);

                Long totalElements = chatMessageRepository.countByCourseId(courseId);
                Integer totalPages = (int) Math.ceil((double) totalElements / size);

                List<SimpleChatMessageResponse> messageList = messages.stream()
                        .map(this::toSimpleResponse)
                        .collect(Collectors.toList());

                ChatMessagesListResponse response = ChatMessagesListResponse.builder()
                        .messages(messageList)
                        .page(page)
                        .size(size)
                        .totalElements(totalElements)
                        .totalPages(totalPages)
                        .build();

                return ApiResponseUtil.success(response, "Messages retrieved successfully");
            }

        } catch (NoSuchElementException e) {
            if (e.getMessage().equals("Course not found")) {
                return ApiResponseUtil.notFound("Course not found");
            }
            return ApiResponseUtil.notFound(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ApiResponseUtil.badRequest("Invalid query parameters");
        } catch (Exception e) {
            return ApiResponseUtil.internalServerError("Failed to retrieve messages. Please try again later.");
        }
    }

    // Helper method to convert String UUID to Long (for response)
    private Long messageIdToLong(String uuid) {
        try {
            // Simple approach: find position in list
            // This is a simplified approach - in production you might need a proper mapping
            var allMessages = chatMessageRepository.findAll();
            for (int i = 0; i < allMessages.size(); i++) {
                if (allMessages.get(i).getId().equals(uuid)) {
                    return (long) (i + 1);
                }
            }
            return 1L; // Default fallback
        } catch (Exception e) {
            return 1L;
        }
    }

    private SimpleChatMessageResponse toSimpleResponse(ChatMessage m) {
        // Determine the simplified type: "text" or "file"
        String type = "text";
        String content = null;
        String fileUrl = null;
        String fileName = null;
        Long fileSize = null;
        String mimeType = null;

        if (m.getTextDetail() != null) {
            type = "text";
            content = m.getTextDetail().getContent();
        } else if (m.getFileDetail() != null) {
            type = "file";
            fileUrl = m.getFileDetail().getFileUrl();
            fileName = m.getFileDetail().getFileName();
            fileSize = m.getFileDetail().getFileSize();
            mimeType = m.getFileDetail().getMimeType();
        } else if (m.getAudioDetail() != null) {
            type = "file";
            fileUrl = m.getAudioDetail().getAudioUrl(); // Maps to file_url column
            fileName = m.getAudioDetail().getFileName();
            fileSize = m.getAudioDetail().getFileSize();
            mimeType = m.getAudioDetail().getMimeType();
        } else if (m.getVideoDetail() != null) {
            type = "file";
            fileUrl = m.getVideoDetail().getVideoUrl(); // Maps to file_url column
            fileName = m.getVideoDetail().getFileName();
            fileSize = m.getVideoDetail().getFileSize();
            mimeType = m.getVideoDetail().getMimeType();
        }

        return SimpleChatMessageResponse.builder()
                .id(m.getId()) // Keep String UUID as is
                .senderId(m.getSender().getId())
                .type(type)
                .content(content)
                .fileUrl(fileUrl)
                .fileName(fileName)
                .fileSize(fileSize)
                .mimeType(mimeType)
                .createdAt(m.getCreatedAt())
                .build();
    }
}
