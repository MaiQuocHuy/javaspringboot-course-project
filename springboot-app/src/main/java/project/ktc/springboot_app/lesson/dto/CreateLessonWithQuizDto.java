package project.ktc.springboot_app.lesson.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLessonWithQuizDto {

	@NotBlank(message = "Lesson title is required")
	@Size(min = 3, max = 255, message = "Lesson title must be between 3 and 255 characters")
	private String title;

	// @NotBlank(message = "Section ID is required")
	// private String sectionId;

	// Type is always QUIZ for this endpoint, but include for clarity
	@Builder.Default
	private String type = "QUIZ";

	@Valid
	@NotNull(message = "Quiz data is required")
	private CreateQuizWithinLessonDto quiz;
}
