package project.ktc.springboot_app.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Quiz information for course structure")
public class CourseStructureQuizDto {

  @Schema(description = "List of quiz questions")
  private List<QuizQuestion> questions;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Quiz question details")
  public static class QuizQuestion {

    @Schema(description = "Question ID", example = "question-001")
    private String id;

    @Schema(description = "Question text", example = "What is React?")
    private String questionText;

    @Schema(
        description = "Answer options",
        example =
            "{\"A\": \"Library\", \"B\": \"Framework\", \"C\": \"Language\", \"D\": \"Tool\"}")
    private Map<String, String> options;

    @Schema(description = "Correct answer", example = "A")
    private String correctAnswer;

    @Schema(
        description = "Explanation of the correct answer",
        example = "React is a JavaScript library for building user interfaces")
    private String explanation;
  }
}
