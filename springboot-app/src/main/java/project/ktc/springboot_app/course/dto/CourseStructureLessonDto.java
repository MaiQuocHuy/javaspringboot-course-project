package project.ktc.springboot_app.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Course structure lesson DTO")
public class CourseStructureLessonDto {

    @Schema(description = "Lesson ID", example = "lesson-001")
    private String id;

    @Schema(description = "Lesson title", example = "Introduction to React")
    private String title;

    @Schema(description = "Lesson type", example = "VIDEO", allowableValues = { "VIDEO", "QUIZ" })
    private String type;

    @Schema(description = "Lesson order", example = "0")
    private Integer order;

    @Schema(description = "Video information (only for VIDEO type lessons)")
    private CourseStructureVideoDto video;

    @Schema(description = "Quiz information (only for QUIZ type lessons)")
    private CourseStructureQuizDto quiz;
}