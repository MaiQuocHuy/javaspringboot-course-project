package project.ktc.springboot_app.lesson.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLessonWithQuizDto {

    @NotBlank(message = "Lesson title is required")
    @Size(min = 3, max = 255, message = "Lesson title must be between 3 and 255 characters")
    private String title;

    @NotBlank(message = "Lesson description is required")
    @Size(min = 10, max = 1000, message = "Lesson description must be between 10 and 1000 characters")
    private String description;

    @NotBlank(message = "Section ID is required")
    private String sectionId;

    // Type is always QUIZ for this endpoint, but include for clarity
    @Builder.Default
    private String type = "QUIZ";

    @Valid
    @NotNull(message = "Quiz data is required")
    private CreateQuizWithinLessonDto quiz;
}
