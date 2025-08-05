package project.ktc.springboot_app.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.quiz.validation.ValidQuizQuestion;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidQuizQuestion
public class UpdateQuizQuestionDto {

    private String id; // Optional for updates, if provided will update existing question

    @NotBlank(message = "Question text is required")
    @Size(max = 1000, message = "Question text cannot exceed 1000 characters")
    private String questionText;

    @NotNull(message = "Options are required")
    private Map<String, String> options;

    @NotBlank(message = "Correct answer is required")
    @Pattern(regexp = "^[ABCD]$", message = "Correct answer must be A, B, C, or D")
    private String correctAnswer;

    @Size(max = 500, message = "Explanation cannot exceed 500 characters")
    private String explanation;
}
