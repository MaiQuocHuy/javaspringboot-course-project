package project.ktc.springboot_app.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentQuizStatsDto {
  private Long totalQuizzes;
  private Long passedQuizzes; // >= 80 score
  private Long failedQuizzes; // < 80 score
  private Double averageScore;
}
