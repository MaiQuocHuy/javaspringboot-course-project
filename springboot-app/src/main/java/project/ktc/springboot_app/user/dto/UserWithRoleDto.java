package project.ktc.springboot_app.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for user with role information */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWithRoleDto {

  private String id;
  private String email;
  private String name;
  private String thumbnailUrl;
  private String bio;
  private Boolean isActive;

  private RoleInfoDto role;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RoleInfoDto {
    private String id;
    private String name;
  }
}
