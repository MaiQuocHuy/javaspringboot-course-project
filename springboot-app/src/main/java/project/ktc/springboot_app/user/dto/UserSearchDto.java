package project.ktc.springboot_app.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for user search and filter criteria")
public class UserSearchDto {

  @Schema(description = "Search by name or email", example = "john")
  private String search;

  @Schema(
      description = "Filter by user role",
      example = "STUDENT",
      allowableValues = {"STUDENT", "INSTRUCTOR", "ADMIN"})
  private String role;

  @Schema(description = "Filter by active status", example = "true")
  private Boolean isActive;

  @Schema(
      description = "Sort field",
      example = "name",
      allowableValues = {"name", "email", "createdAt", "role"})
  private String sortBy;

  @Schema(
      description = "Sort direction",
      example = "asc",
      allowableValues = {"asc", "desc"})
  private String sortDirection;
}
