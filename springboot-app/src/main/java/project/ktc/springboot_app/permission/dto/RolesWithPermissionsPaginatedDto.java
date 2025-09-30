package project.ktc.springboot_app.permission.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Paginated response DTO for roles with permissions */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response DTO for roles with permissions")
public class RolesWithPermissionsPaginatedDto {

  @Schema(description = "List of roles with their permissions")
  private List<RoleWithPermissionsDto> content;

  @Schema(description = "Pagination information")
  private PageInfo page;

  @Schema(description = "Response timestamp", example = "2023-10-05T12:34:56")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime timestamps;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Pagination information")
  public static class PageInfo {

    @Schema(description = "Current page number", example = "0")
    private Integer number;

    @Schema(description = "Page size", example = "10")
    private Integer size;

    @Schema(description = "Total number of pages", example = "1")
    private Integer totalPages;

    @Schema(description = "Total number of elements", example = "1")
    private Long totalElements;

    @Schema(description = "Is first page", example = "true")
    private Boolean first;

    @Schema(description = "Is last page", example = "true")
    private Boolean last;
  }
}
