package project.ktc.springboot_app.notification.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.notification.entity.NotificationPriority;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private String id;

    private String userId;

    private String resourceId;

    private String entityId;

    private String message;

    private String actionUrl;

    private NotificationPriority priority;

    private Boolean isRead;

    private LocalDateTime readAt;

    private LocalDateTime expiredAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
