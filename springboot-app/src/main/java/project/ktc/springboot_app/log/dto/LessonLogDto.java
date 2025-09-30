package project.ktc.springboot_app.log.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for logging lesson data in system logs Contains all relevant lesson
 * information for audit
 * trail
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonLogDto {

	private String id;
	private String title;
	private String type;
	private Integer orderIndex;
	private String sectionId;
	private String sectionTitle;
	private String courseId;
	private String courseTitle;

	// Video content information (if lesson type is VIDEO)
	private VideoContentLogDto videoContent;

	// Timestamps
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class VideoContentLogDto {
		private String id;
		private String url;
		private Integer duration;
		private String uploadedBy;
		private LocalDateTime createdAt;
	}
}
