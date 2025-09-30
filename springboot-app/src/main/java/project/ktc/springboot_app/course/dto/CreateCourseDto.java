package project.ktc.springboot_app.course.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.enums.CourseLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseDto {

	@NotBlank(message = "Course title is required")
	@Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
	private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 255, message = "Description must be between 20 and 255 characters")
    private String description;

	@NotBlank(message = "Slug is required")
	private String slug;

	@NotNull(message = "Price is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
	@DecimalMax(value = "9999.99", message = "Price cannot exceed 9999.99")
	private BigDecimal price;

	@NotNull(message = "At least one category ID is required")
	@Size(min = 1, message = "At least one category must be selected")
	private List<String> categoryIds;

	@NotNull(message = "Course level is required")
	private CourseLevel level;
}
