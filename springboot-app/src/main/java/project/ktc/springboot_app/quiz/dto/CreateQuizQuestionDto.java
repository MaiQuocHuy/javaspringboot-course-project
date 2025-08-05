package project.ktc.springboot_app.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import project.ktc.springboot_app.quiz.validation.ValidQuizQuestion;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidQuizQuestion
public class CreateQuizQuestionDto {

    @NotBlank(message = "Question text is required")
    @Size(min = 10, max = 1000, message = "Question text must be between 10 and 1000 characters")
    private String questionText;

    @NotNull(message = "Options are required")
    @Size(min = 2, max = 6, message = "Must have between 2 and 6 options")
    private Map<String, String> options;

    @NotBlank(message = "Correct answer is required")
    private String correctAnswer;

    @Size(max = 500, message = "Explanation cannot exceed 500 characters")
    private String explanation;
}
