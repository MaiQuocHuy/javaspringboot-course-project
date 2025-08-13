package project.ktc.springboot_app.lesson.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import project.ktc.springboot_app.quiz.dto.CreateQuizQuestionDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizWithinLessonDto {

    @Valid
    @NotEmpty(message = "At least one question is required")
    @Size(max = 50, message = "Cannot have more than 50 questions")
    private List<CreateQuizQuestionDto> questions;
}
