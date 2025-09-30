package project.ktc.springboot_app.instructor_application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.instructor_application.entity.InstructorApplication.ApplicationStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminApplicationDetailDto {

  private String id;

  private UserBasicDto applicant;

  // Thông tin người review (nếu có)
  // private UserBasicDto reviewer;

  private ApplicationStatus status;

  private String documents;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime submittedAt;

  private String rejectionReason;

  // Nested DTO cho user info
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class UserBasicDto {
    private String id;
    private String name;
    private String email;
    private String thumbnailUrl;
  }
}
