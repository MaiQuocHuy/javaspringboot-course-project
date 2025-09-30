package project.ktc.springboot_app.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Course structure section DTO")
public class CourseStructureSectionDto {

	@Schema(description = "Section ID", example = "section-001")
	private String id;

	@Schema(description = "Section title", example = "Getting Started with React")
	private String title;

	@Schema(description = "Section description", example = "Learn the fundamentals of React")
	private String description;

	@Schema(description = "Section order", example = "0")
	private Integer order;

	@Schema(description = "Number of lessons in this section", example = "2")
	private Integer lessonCount;

	@Schema(description = "List of lessons in this section")
	private List<CourseStructureLessonDto> lessons;
}
