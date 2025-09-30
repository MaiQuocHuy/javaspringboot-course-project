package project.ktc.springboot_app.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import project.ktc.springboot_app.entity.BaseEntity;

/** Notification entity representing system notifications for users */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

	@Column(name = "user_id", nullable = false, length = 36)
	private String userId;

	@Column(name = "resource_id", nullable = false, length = 36)
	private String resourceId;

	@Column(name = "entity_id", nullable = false, length = 36)
	private String entityId;

	@Column(name = "message", nullable = false, columnDefinition = "TEXT")
	private String message;

	@Column(name = "action_url", nullable = false)
	private String actionUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "priority", nullable = false)
	@Builder.Default
	private NotificationPriority priority = NotificationPriority.LOW;

	@Column(name = "is_read", nullable = false)
	@Builder.Default
	private Boolean isRead = false;

	@Column(name = "read_at")
	private LocalDateTime readAt;

	@Column(name = "expired_at")
	private LocalDateTime expiredAt;
}
