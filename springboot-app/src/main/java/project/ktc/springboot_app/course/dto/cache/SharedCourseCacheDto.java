package project.ktc.springboot_app.course.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Cache-specific DTO for shared course data
 * Uses cache DTOs instead of JPA entities for Redis serialization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedCourseCacheDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // Course data with categories (cache-friendly)
    private List<CourseCacheDto> coursesWithCategories;

    // Enrollment counts by course ID
    private Map<String, Long> enrollmentCounts;

    // Pagination information
    private Integer totalPages;
    private Long totalElements;
    private Integer pageNumber;
    private Integer pageSize;
    private Boolean first;
    private Boolean last;
}