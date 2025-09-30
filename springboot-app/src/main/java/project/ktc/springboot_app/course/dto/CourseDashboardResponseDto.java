package project.ktc.springboot_app.course.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.dto.common.BaseCourseResponseDto;

/**
 * Course dashboard response DTO extending base course information with
 * dashboard-specific fields
 * like permissions and revenue
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CourseDashboardResponseDto extends BaseCourseResponseDto {

	// Dashboard-specific fields
	private String status; // DRAFT, PENDING, PUBLISHED, etc.
	private LocalDateTime lastContentUpdate;
	private BigDecimal revenue;

	// Permission flags for UI control
	private boolean canEdit;
	private boolean canUnpublish;
	private boolean canDelete;
	private boolean canPublish;

	// Review status from course_review_status_history
	private String statusReview;
	private String reason;

	/** Constructor with all fields including inherited ones */
	@Builder(builderMethodName = "dashboardBuilder")
	public CourseDashboardResponseDto(
			// Base fields
			String id,
			String title,
			String description,
			BigDecimal price,
			project.ktc.springboot_app.course.enums.CourseLevel level,
			String thumbnailUrl,
			boolean isApproved,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			java.util.List<CategoryInfo> categories,
			int totalStudents,
			int sectionCount,
			double averageRating,
			// Dashboard-specific fields
			String status,
			LocalDateTime lastContentUpdate,
			BigDecimal revenue,
			boolean canEdit,
			boolean canUnpublish,
			boolean canDelete,
			boolean canPublish,
			String statusReview,
			String reason) {

		// Set inherited fields
		this.id = id;
		this.title = title;
		this.description = description;
		this.price = price;
		this.level = level;
		this.thumbnailUrl = thumbnailUrl;
		this.isApproved = isApproved;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.categories = categories;
		this.totalStudents = totalStudents;
		this.sectionCount = sectionCount;
		this.averageRating = averageRating;

		// Set dashboard-specific fields
		this.status = status;
		this.lastContentUpdate = lastContentUpdate;
		this.revenue = revenue;
		this.canEdit = canEdit;
		this.canUnpublish = canUnpublish;
		this.canDelete = canDelete;
		this.canPublish = canPublish;
		this.statusReview = statusReview;
		this.reason = reason;
	}
}
