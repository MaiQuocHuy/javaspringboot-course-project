package project.ktc.springboot_app.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.*;
import project.ktc.springboot_app.notification.entity.NotificationPriority;

/** DTO for notification responses */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {

  private String id;

  private String user_id;

  private String resource_id;

  private String entity_id;

  private String message;

  private String action_url;

  private NotificationPriority priority;

  private Boolean is_read;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime created_at;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime updated_at;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime expired_at;
}
