package project.ktc.springboot_app.permission.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import project.ktc.springboot_app.entity.Permission;
import project.ktc.springboot_app.entity.RolePermission;
import project.ktc.springboot_app.permission.dto.PermissionResponseDto;

/**
 * Mapper utility for converting Permission entities to DTOs
 */
@Component
public class PermissionMapper {

    /**
     * Convert Permission entity to PermissionResponseDto
     * 
     * @param permission the permission entity
     * @return permission response DTO
     */
    public PermissionResponseDto toResponseDto(Permission permission) {
        if (permission == null) {
            return null;
        }

        return PermissionResponseDto.builder()
                .key(permission.getPermissionKey())
                .description(permission.getDescription())
                .resource(mapResourceDto(permission))
                .action(mapActionDto(permission))
                .isActive(permission.getIsActive())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }

    /**
     * Convert list of Permission entities to list of PermissionResponseDto
     * 
     * @param permissions list of permission entities
     * @return list of permission response DTOs
     */
    public List<PermissionResponseDto> toResponseDtoList(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return List.of();
        }

        return permissions.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Map Resource entity to ResourceDto
     * 
     * @param permission the permission containing resource
     * @return resource DTO
     */
    private PermissionResponseDto.ResourceDto mapResourceDto(Permission permission) {
        if (permission.getResource() == null) {
            return null;
        }

        return PermissionResponseDto.ResourceDto.builder()
                .key(permission.getResource().getName())
                .name(permission.getResource().getDescription() != null
                        ? permission.getResource().getDescription()
                        : permission.getResource().getName())
                .isActive(permission.getResource().getIsActive())
                .build();
    }

    /**
     * Map Action entity to ActionDto
     * 
     * @param permission the permission containing action
     * @return action DTO
     */
    private PermissionResponseDto.ActionDto mapActionDto(Permission permission) {
        if (permission.getAction() == null) {
            return null;
        }

        return PermissionResponseDto.ActionDto.builder()
                .key(permission.getAction().getName())
                .name(permission.getAction().getDescription() != null
                        ? permission.getAction().getDescription()
                        : permission.getAction().getName())
                .isActive(permission.getAction().getIsActive())
                .build();
    }

    /**
     * Convert RolePermission entity to PermissionResponseDto with role-specific
     * fields
     * 
     * @param rolePermission the role permission entity
     * @return permission response DTO with role-specific information
     */
    public PermissionResponseDto toRolePermissionResponseDto(RolePermission rolePermission) {
        if (rolePermission == null || rolePermission.getPermission() == null) {
            return null;
        }

        Permission permission = rolePermission.getPermission();

        return PermissionResponseDto.builder()
                .key(permission.getPermissionKey())
                .description(permission.getDescription())
                .resource(mapResourceDto(permission))
                .action(mapActionDto(permission))
                .isActive(permission.getIsActive())
                .roleActive(rolePermission.getIsActive())
                .permissionActive(permission.getIsActive())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }

    /**
     * Convert list of RolePermission entities to list of PermissionResponseDto
     * 
     * @param rolePermissions list of role permission entities
     * @return list of permission response DTOs with role-specific information
     */
    public List<PermissionResponseDto> toRolePermissionResponseDtoList(List<RolePermission> rolePermissions) {
        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return List.of();
        }

        return rolePermissions.stream()
                .map(this::toRolePermissionResponseDto)
                .collect(Collectors.toList());
    }
}
