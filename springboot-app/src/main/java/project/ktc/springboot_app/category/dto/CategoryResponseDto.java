package project.ktc.springboot_app.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Category response with course count")
public class CategoryResponseDto {

    @Schema(description = "Category unique identifier", example = "33333333-3333-3333-3333-333333333333")
    private String id;

    @Schema(description = "Category name", example = "Development")
    private String name;

    @Schema(description = "SEO-friendly slug", example = "development")
    private String slug;

    @Schema(description = "Total number of published and non-deleted courses in this category", example = "42")
    private Long courseCount;
}
