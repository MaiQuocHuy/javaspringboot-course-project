package project.ktc.springboot_app.cache.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cache DTO for instructor statistics This DTO is optimized for Redis
 * serialization with a flat
 * structure to avoid any potential circular reference issues.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorStatisticsCacheDto implements Serializable {

	private static final long serialVersionUID = 1L;

	// Course Statistics
	private String courseTitle;
	private String courseValue;
	private String courseDescription;

	// Student Statistics
	private String studentTitle;
	private String studentValue;
	private String studentDescription;

	// Revenue Statistics
	private String revenueTitle;
	private String revenueValue;
	private String revenueDescription;

	// Rating Statistics
	private String ratingTitle;
	private String ratingValue;
	private String ratingDescription;

	// Cache metadata
	private LocalDateTime cachedAt;
	private String instructorId;
}
