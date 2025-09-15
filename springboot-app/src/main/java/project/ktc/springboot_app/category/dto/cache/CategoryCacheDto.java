package project.ktc.springboot_app.category.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Cache DTO for Category data to avoid caching ResponseEntity objects.
 * This DTO contains only the essential category information needed for caching.
 * 
 * @author KTC Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCacheDto implements Serializable {

    /**
     * Category unique identifier
     */
    private String id;

    /**
     * Category name
     */
    private String name;

    /**
     * Category description
     */
    private String description;

    /**
     * SEO-friendly slug
     */
    private String slug;

    /**
     * Total number of published and non-deleted courses in this category
     */
    private Long courseCount;
}