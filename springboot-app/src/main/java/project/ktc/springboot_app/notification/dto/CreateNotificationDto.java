package project.ktc.springboot_app.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;
import project.ktc.springboot_app.notification.entity.NotificationPriority;

/** DTO for creating notification requests */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationDto {

	@NotBlank(message = "User ID is required")
	private String user_id;

	@NotBlank(message = "Resource ID is required")
	private String resource_id;

	@NotBlank(message = "Entity ID is required")
	private String entity_id;

	@NotBlank(message = "Message is required")
	private String message;

	@NotBlank(message = "Action URL is required")
	private String action_url;

	@NotNull(message = "Priority is required")
	private NotificationPriority priority;

	private LocalDateTime expired_at;
}
