package project.ktc.springboot_app.lesson.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed submission information for instructor review")
public class SubmissionDetailResponseDto {

  @Schema(description = "Submission ID", example = "qr-123")
  private String id;

  @Schema(description = "Lesson ID", example = "lesson-45")
  private String lessonId;

  @Schema(description = "Student information")
  private StudentInfo student;

  @Schema(description = "Quiz score (0-100)", example = "85.5")
  private BigDecimal score;

  @Schema(description = "Submission timestamp", example = "2025-08-23T10:15:00")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime submittedAt;

  @Schema(description = "Submission status", example = "SUBMITTED")
  private String status;

  @Schema(description = "List of questions with answers")
  private List<QuestionAnswerDto> answers;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Student information summary")
  public static class StudentInfo {
    @Schema(description = "Student ID", example = "user-789")
    private String id;

    @Schema(description = "Student full name", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "Student email", example = "student@example.com")
    private String email;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Question with student answer and correctness")
  public static class QuestionAnswerDto {
    @Schema(description = "Question ID", example = "question-1")
    private String questionId;

    @Schema(description = "Question text", example = "What is Spring Boot?")
    private String questionText;

    @Schema(description = "Available options")
    private java.util.Map<String, String> options;

    @Schema(description = "Student's answer", example = "A")
    private String answer;

    @Schema(description = "Correct answer", example = "A")
    private String correctAnswer;

    @Schema(description = "Whether student answer is correct", example = "true")
    private Boolean isCorrect;

    @Schema(description = "Question explanation", example = "Spring Boot is a framework...")
    private String explanation;
  }
}
