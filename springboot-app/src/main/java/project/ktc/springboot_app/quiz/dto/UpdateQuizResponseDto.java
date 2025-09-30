package project.ktc.springboot_app.quiz.dto;

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
public class UpdateQuizResponseDto {

  private String lessonId;
  private String title; // Lesson title
  private List<QuizQuestionResponseDto> questions;
  private LocalDateTime updatedAt;

  // Statistics for the response
  private int totalQuestions;
  private int questionsUpdated;
  private int questionsAdded;
  private int questionsRemoved;
}
