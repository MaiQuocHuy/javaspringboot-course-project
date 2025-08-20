package project.ktc.springboot_app.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for updating user status")
public class UpdateUserStatusDto {

    @Schema(description = "User's active status", example = "true", required = true)
    @NotNull(message = "Status cannot be null")
    private Boolean isActive;
}
