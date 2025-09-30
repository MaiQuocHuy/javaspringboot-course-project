package project.ktc.springboot_app.permission.mapper;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.permission.dto.RoleResponseDto;

/** Mapper utility for converting UserRole entities to DTOs */
@Component
public class RoleMapper {

  /**
   * Convert UserRole entity to RoleResponseDto
   *
   * @param role the role entity
   * @return role response DTO
   */
  public RoleResponseDto toResponseDto(UserRole role) {
    if (role == null) {
      return null;
    }

    return RoleResponseDto.builder()
        .id(role.getId())
        .name(role.getRole())
        .createdAt(null) // UserRole doesn't have timestamps, could be enhanced
        .updatedAt(null) // UserRole doesn't have timestamps, could be enhanced
        .build();
  }

  /**
   * Convert list of UserRole entities to list of RoleResponseDto
   *
   * @param roles list of role entities
   * @return list of role response DTOs
   */
  public List<RoleResponseDto> toResponseDtoList(List<UserRole> roles) {
    if (roles == null) {
      return null;
    }

    return roles.stream().map(this::toResponseDto).collect(Collectors.toList());
  }
}
