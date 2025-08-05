package project.ktc.springboot_app.quiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuizDto {

    @NotEmpty(message = "Questions list cannot be empty")
    @Size(min = 1, max = 20, message = "Must have between 1 and 20 questions")
    @Valid
    private List<UpdateQuizQuestionDto> questions;
}
