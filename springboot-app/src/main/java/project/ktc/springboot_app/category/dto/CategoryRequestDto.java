package project.ktc.springboot_app.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Category request DTO for create and update operations")
public class CategoryRequestDto {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    @Schema(description = "Category name", example = "Programming", required = true)
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Category description", example = "Programming courses including various languages and frameworks")
    private String description;
}
