package project.ktc.springboot_app.lesson.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import project.ktc.springboot_app.quiz.dto.CreateQuizQuestionDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizWithinLessonDto {

    @NotBlank(message = "Quiz title is required")
    @Size(min = 3, max = 255, message = "Quiz title must be between 3 and 255 characters")
    private String title;

    @NotBlank(message = "Quiz description is required")
    @Size(min = 10, max = 1000, message = "Quiz description must be between 10 and 1000 characters")
    private String description;

    @Valid
    @NotEmpty(message = "At least one question is required")
    @Size(max = 50, message = "Cannot have more than 50 questions")
    private List<CreateQuizQuestionDto> questions;
}
