package project.ktc.springboot_app.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizDto {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Lesson ID is required")
    @NotBlank(message = "Lesson ID cannot be blank")
    private String lessonId;

    @Valid
    @NotEmpty(message = "At least one question is required")
    @Size(max = 50, message = "Cannot have more than 50 questions")
    private List<CreateQuizQuestionDto> questions;
}
