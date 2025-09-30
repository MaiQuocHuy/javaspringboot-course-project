package project.ktc.springboot_app.course.dto.common;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.enums.CourseLevel;

/**
 * Base course response DTO containing common fields shared across different
 * course response types.
 * This promotes code reuse and consistency across different endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseCourseResponseDto {

	protected String id;
	protected String title;
	protected String description;
	protected BigDecimal price;
	protected CourseLevel level;
	protected String thumbnailUrl;

	protected boolean isApproved;
	protected LocalDateTime createdAt;
	protected LocalDateTime updatedAt;

	protected List<CategoryInfo> categories;

	// Statistics
	protected int totalStudents;
	protected int sectionCount;
	protected double averageRating;

	/** Common category information used across different course DTOs */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CategoryInfo {
		private String id;
		private String name;
	}

	/** Common instructor information used across different course DTOs */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class InstructorInfo {
		private String id;
		private String name;
		private String email;
		private String avatar;
	}
}
