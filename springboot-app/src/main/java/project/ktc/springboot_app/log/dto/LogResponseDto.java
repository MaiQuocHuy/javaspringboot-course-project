package project.ktc.springboot_app.log.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for system log creation response */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogResponseDto {

  /** ID of the created log entry */
  private Long id;
}
