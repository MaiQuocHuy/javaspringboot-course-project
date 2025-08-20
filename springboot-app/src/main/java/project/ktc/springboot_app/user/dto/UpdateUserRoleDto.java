package project.ktc.springboot_app.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for updating user role")
public class UpdateUserRoleDto {

    @Schema(description = "User's new role", example = "ADMIN", required = true, allowableValues = { "STUDENT",
            "INSTRUCTOR", "ADMIN" })
    @NotNull(message = "Role cannot be null")
    @Pattern(regexp = "^(STUDENT|INSTRUCTOR|ADMIN)$", message = "Role must be one of: STUDENT, INSTRUCTOR, ADMIN")
    private String role;

}
