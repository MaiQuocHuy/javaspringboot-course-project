package project.ktc.springboot_app.log.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.enums.CourseLevel;

/** DTO for capturing course data in system logs */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseLogDto {

	private String id;
	private String title;
	private String description;
	private BigDecimal price;
	private CourseLevel level;
	private String thumbnailUrl;
	private String thumbnailId;
	private Boolean isPublished;
	private Boolean isApproved;
	private Boolean isDeleted;
	private String instructorId;
	private String instructorName;
	private List<String> categoryIds;
	private List<String> categoryNames;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
