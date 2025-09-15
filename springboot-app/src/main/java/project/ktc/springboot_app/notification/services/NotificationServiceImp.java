package project.ktc.springboot_app.notification.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.common.exception.ResourceNotFoundException;
import project.ktc.springboot_app.notification.dto.CreateNotificationDto;
import project.ktc.springboot_app.notification.dto.NotificationResponseDto;
import project.ktc.springboot_app.notification.entity.Notification;
import project.ktc.springboot_app.notification.interfaces.NotificationService;
import project.ktc.springboot_app.notification.repositories.NotificationRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;

import java.util.concurrent.CompletableFuture;

/**
 * Implementation of NotificationService
*/
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImp implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Async("taskExecutor")
    public CompletableFuture<NotificationResponseDto> createNotification(CreateNotificationDto createNotificationDto) {
        log.info("Creating notification asynchronously for user: {}", createNotificationDto.getUser_id());

        try {
            NotificationResponseDto response = createNotificationSync(createNotificationDto);
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Error creating notification asynchronously: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    @Transactional
    public NotificationResponseDto createNotificationSync(CreateNotificationDto createNotificationDto) {
        log.info("Creating notification for user: {}", createNotificationDto.getUser_id());

        // Validate user exists
        userRepository.findById(createNotificationDto.getUser_id())
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", createNotificationDto.getUser_id());
                    return new ResourceNotFoundException(
                            "User not found with ID: " + createNotificationDto.getUser_id());
                });

        // Create notification entity
        Notification notification = Notification.builder()
                .userId(createNotificationDto.getUser_id())
                .resourceId(createNotificationDto.getResource_id())
                .entityId(createNotificationDto.getEntity_id())
                .message(createNotificationDto.getMessage())
                .actionUrl(createNotificationDto.getAction_url())
                .priority(createNotificationDto.getPriority())
                .expiredAt(createNotificationDto.getExpired_at())
                .isRead(false)
                .build();

        // Save notification
        Notification savedNotification = notificationRepository.save(notification);

        log.info("Notification created successfully with ID: {} for user: {}",
                savedNotification.getId(), createNotificationDto.getUser_id());

        // Map to response DTO
        return mapToResponseDto(savedNotification);
    }

    /**
     * Map Notification entity to response DTO
     */
    private NotificationResponseDto mapToResponseDto(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .user_id(notification.getUserId())
                .resource_id(notification.getResourceId())
                .entity_id(notification.getEntityId())
                .message(notification.getMessage())
                .action_url(notification.getActionUrl())
                .priority(notification.getPriority())
                .is_read(notification.getIsRead())
                .created_at(notification.getCreatedAt())
                .updated_at(notification.getUpdatedAt())
                .expired_at(notification.getExpiredAt())
                .build();
    }
}
