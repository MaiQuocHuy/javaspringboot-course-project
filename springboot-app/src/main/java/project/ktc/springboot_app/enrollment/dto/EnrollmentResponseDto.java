package project.ktc.springboot_app.enrollment.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponseDto {
  private String courseId;
  private String title;
  private LocalDateTime enrollmentDate;
}
