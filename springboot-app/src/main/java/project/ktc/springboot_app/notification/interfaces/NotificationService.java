package project.ktc.springboot_app.notification.interfaces;

import project.ktc.springboot_app.notification.dto.CreateNotificationDto;
import project.ktc.springboot_app.notification.dto.NotificationResponseDto;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for notification operations
 */
public interface NotificationService {

    /**
     * Create a new notification
     * 
     * @param createNotificationDto the notification data
     * @return the created notification response
     */
    CompletableFuture<NotificationResponseDto> createNotification(CreateNotificationDto createNotificationDto);

    /**
     * Create a notification synchronously (for internal use)
     * 
     * @param createNotificationDto the notification data
     * @return the created notification response
     */
    NotificationResponseDto createNotificationSync(CreateNotificationDto createNotificationDto);
}
