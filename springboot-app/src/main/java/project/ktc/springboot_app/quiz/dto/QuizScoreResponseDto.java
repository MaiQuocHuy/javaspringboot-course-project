package project.ktc.springboot_app.quiz.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizScoreResponseDto {
  private String id;
  private LessonSummary lesson;
  private SectionSummary section;
  private CourseSummary course;
  private BigDecimal score;
  private Integer totalQuestions;
  private Integer correctAnswers;
  private LocalDateTime completedAt;
  private Boolean canReview;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LessonSummary {
    private String id;
    private String title;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SectionSummary {
    private String id;
    private String title;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CourseSummary {
    private String id;
    private String title;
  }
}
