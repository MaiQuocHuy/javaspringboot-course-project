package project.ktc.springboot_app.log.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.log.entity.SystemLog;

/** DTO for creating a new system log entry */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLogDto {

  @NotBlank(message = "User ID is required")
  private String userId;

  @NotNull(message = "Action is required")
  private SystemLog.Action action;

  @NotBlank(message = "Entity type is required")
  @Size(min = 1, max = 50, message = "Entity type must be between 1 and 50 characters")
  private String entityType;

  @Size(max = 36, message = "Entity ID cannot exceed 36 characters")
  private String entityId;

  // JSON string for old values - can be null
  private String oldValues;

  // JSON string for new values - can be null
  private String newValues;
}
