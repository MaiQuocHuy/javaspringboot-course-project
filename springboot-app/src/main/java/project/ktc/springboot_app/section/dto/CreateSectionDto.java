package project.ktc.springboot_app.section.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSectionDto {

    @NotBlank(message = "Section title is required")
    @Size(min = 3, max = 255, message = "Section title must be between 3 and 255 characters")
    private String title;
}
