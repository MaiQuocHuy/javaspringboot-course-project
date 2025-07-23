package project.ktc.springboot_app.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for updating user role")
public class UpdateUserRoleDto {

    @Schema(description = "User's new role", example = "ADMIN", required = true)
    @NotBlank(message = "Role cannot be blank")
    private String role;

}
