package project.ktc.springboot_app.course.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.dto.common.BaseCourseResponseDto;

/**
 * Instructor course detail response DTO extending base course information with
 * detailed sections
 * and lessons information
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class InstructorCourseDetailResponseDto extends BaseCourseResponseDto {

	// Detail-specific fields
	private String slug;
	private InstructorInfo instructor;
	private LocalDateTime lastContentUpdate;

	private Boolean isPublished;
	private int enrollmentCount;
	private long ratingCount;

	private List<SectionInfo> sections;

	// Review status from course_review_status_history
	private String statusReview;
	private String reason;

	// Manual constructor that includes base fields
	public InstructorCourseDetailResponseDto(
			// Base fields
			String id,
			String title,
			String description,
			java.math.BigDecimal price,
			project.ktc.springboot_app.course.enums.CourseLevel level,
			String thumbnailUrl,
			boolean isApproved,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			List<CategoryInfo> categories,
			int totalStudents,
			int sectionCount,
			double averageRating,
			// Detail-specific fields
			String slug,
			InstructorInfo instructor,
			LocalDateTime lastContentUpdate,
			Boolean isPublished,
			int enrollmentCount,
			long ratingCount,
			List<SectionInfo> sections,
			String statusReview,
			String reason) {

		// Set base fields
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

		// Set detail-specific fields
		this.slug = slug;
		this.instructor = instructor;
		this.lastContentUpdate = lastContentUpdate;
		this.isPublished = isPublished;
		this.enrollmentCount = enrollmentCount;
		this.ratingCount = ratingCount;
		this.sections = sections;
		this.statusReview = statusReview;
		this.reason = reason;
	}

	/** Section information with lessons and calculated statistics */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SectionInfo {
		private String id;
		private String title;
		private Integer totalVideoDuration; // in seconds, null if no videos
		private Integer totalQuizQuestion; // null if no quizzes
		private List<LessonInfo> lessons;
	}

	/** Lesson information with type-specific properties */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class LessonInfo {
		private String id;
		private String title;
		private String type; // VIDEO or QUIZ

		// VIDEO-specific fields (only populated for VIDEO type)
		private String videoUrl;
		private Integer duration; // in seconds

		// QUIZ-specific fields (only populated for QUIZ type)
		private Integer quizQuestionCount;
	}
}
