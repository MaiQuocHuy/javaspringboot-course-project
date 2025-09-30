package project.ktc.springboot_app.lesson.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for API response containing individual student submission information. Used in the lesson
 * submissions endpoint response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Individual student submission information for API response")
public class LessonSubmissionResponseDto {

  @Schema(description = "Submission ID (null if not submitted)", example = "sub-123")
  private String submissionId;

  @Schema(description = "Student information")
  private StudentSummary student;

  @Schema(
      description = "Submission status",
      example = "submitted",
      allowableValues = {"not_submitted", "submitted"})
  private String status;

  @Schema(description = "Quiz score (null if not submitted)", example = "8.5")
  private BigDecimal score;

  @Schema(
      description = "Submission timestamp (null if not submitted)",
      example = "2025-07-26T10:30:00Z")
  private LocalDateTime submittedAt;

  /** Nested DTO for student summary information */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Student summary information")
  public static class StudentSummary {

    @Schema(description = "Student ID", example = "stu-001")
    private String id;

    @Schema(description = "Student name", example = "Nguyen Van A")
    private String name;

    @Schema(description = "Student email", example = "student@example.com")
    private String email;
  }
}
