package project.ktc.springboot_app.course.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.entity.CourseReviewStatus;
import project.ktc.springboot_app.course.enums.CourseLevel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cache DTO for instructor course base information (less frequently changing
 * data)
 * Contains basic course details that don't change often
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorCourseBaseCacheDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private CourseLevel level;
    private String thumbnailUrl;
    private Boolean isApproved;
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Review status information
    private CourseReviewStatus.ReviewStatus status;
    private String statusReview; // From course_review_status_history
    private String reason; // From course_review_status_history

    // Categories (simplified for cache)
    private List<CategoryCacheDto> categories;

    /**
     * Nested DTO for Category information in cache
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryCacheDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String id;
        private String name;
        private String slug;
    }
}