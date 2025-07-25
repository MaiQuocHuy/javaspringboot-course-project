package project.ktc.springboot_app.section.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderSectionsDto {

    @NotNull(message = "Section order is required")
    @NotEmpty(message = "Section order cannot be empty")
    private List<String> sectionOrder;
}
