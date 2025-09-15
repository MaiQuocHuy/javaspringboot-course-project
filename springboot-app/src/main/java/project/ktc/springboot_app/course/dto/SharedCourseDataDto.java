package project.ktc.springboot_app.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.entity.Course;

import java.util.List;
import java.util.Map;

/**
 * DTO for shared course data that can be cached for all users.
 * Contains courses with categories and enrollment counts, but no user-specific
 * data.
 * 
 * @author KTC Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedCourseDataDto {

    /**
     * List of courses with their categories loaded
     */
    private List<Course> coursesWithCategories;

    /**
     * Map of course ID to enrollment count
     * Key: courseId, Value: enrollment count
     */
    private Map<String, Long> enrollmentCounts;

    /**
     * Total number of pages for pagination
     */
    private int totalPages;

    /**
     * Total number of elements across all pages
     */
    private long totalElements;

    /**
     * Current page number
     */
    private int pageNumber;

    /**
     * Page size
     */
    private int pageSize;

    /**
     * Whether this is the first page
     */
    private boolean first;

    /**
     * Whether this is the last page
     */
    private boolean last;
}