package project.ktc.springboot_app.lesson.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for reordering lessons within a section")
public class ReorderLessonsDto {

  @NotNull(message = "Lesson order cannot be null")
  @NotEmpty(message = "Lesson order cannot be empty")
  @Schema(
      description =
          "Array of lesson IDs in their intended order (must include all lesson IDs of the section)",
      example = "[\"lesson-uuid-1\", \"lesson-uuid-2\", \"lesson-uuid-3\"]",
      required = true)
  private List<String> lessonOrder;
}
