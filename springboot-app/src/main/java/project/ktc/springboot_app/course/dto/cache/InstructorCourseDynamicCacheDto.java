package project.ktc.springboot_app.course.dto.cache;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cache DTO for instructor course dynamic information (frequently changing
 * data) Contains metrics
 * and status that change often
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorCourseDynamicCacheDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String courseId;

	// Dynamic metrics
	private Integer enrollmentCount;
	private Double averageRating;
	private BigDecimal revenue;
	private Integer sectionCount;
	private LocalDateTime lastContentUpdate;

	// Permissions (computed based on current state)
	private Boolean canEdit;
	private Boolean canDelete;
	private Boolean canUnpublish;
	private Boolean canResubmit;

	// Cache metadata
	private LocalDateTime cacheCreatedAt;
}
