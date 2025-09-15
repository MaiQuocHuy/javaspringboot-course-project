package project.ktc.springboot_app.course.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.enums.CourseLevel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cache-specific DTO for Course entity
 * Contains only serializable data without JPA relationships
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCacheDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private CourseLevel level;
    private String thumbnailUrl;
    private Boolean isApproved;
    private Boolean isPublished;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Instructor information (flattened)
    private String instructorId;
    private String instructorName;
    private String instructorBio;
    private String instructorThumbnailUrl;

    // Categories (simplified)
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
        private String description;
        private Boolean isActive;
    }
}