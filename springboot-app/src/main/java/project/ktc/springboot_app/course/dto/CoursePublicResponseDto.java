package project.ktc.springboot_app.course.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.enums.CourseLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePublicResponseDto {
	private String id;
	private String title;
	private String slug;
	private String description;
	private BigDecimal price;
	private CourseLevel level;
	private String thumbnailUrl;
	private Long enrollCount;
	private Double averageRating;
	private Long sectionCount;
	private Integer totalHours; // Total course duration in hours
	private Boolean isEnrolled;
	private List<CategorySummary> categories;
	private InstructorSummary instructor;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CategorySummary {
		private String id;
		private String name;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class InstructorSummary {
		private String id;
		private String name;
		private String avatar;
	}
}
