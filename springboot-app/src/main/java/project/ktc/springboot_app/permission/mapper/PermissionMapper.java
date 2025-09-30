package project.ktc.springboot_app.permission.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import project.ktc.springboot_app.permission.dto.PermissionResponseDto;
import project.ktc.springboot_app.permission.dto.PermissionUpdateResponse;
import project.ktc.springboot_app.permission.dto.ResourceDto;
import project.ktc.springboot_app.permission.dto.ResourceTreeResponse;
import project.ktc.springboot_app.permission.entity.Permission;
import project.ktc.springboot_app.permission.entity.RolePermission;

/** Mapper utility for converting Permission entities to DTOs */
@Component
public class PermissionMapper {

	/**
	 * Convert Permission entity to PermissionResponseDto
	 *
	 * @param permission
	 *            the permission entity
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
	 * @param permissions
	 *            list of permission entities
	 * @return list of permission response DTOs
	 */
	public List<PermissionResponseDto> toResponseDtoList(List<Permission> permissions) {
		if (permissions == null || permissions.isEmpty()) {
			return List.of();
		}

		return permissions.stream().map(this::toResponseDto).collect(Collectors.toList());
	}

	/**
	 * Map Resource entity to ResourceDto
	 *
	 * @param permission
	 *            the permission containing resource
	 * @return resource DTO
	 */
	private PermissionResponseDto.ResourceDto mapResourceDto(Permission permission) {
		if (permission.getResource() == null) {
			return null;
		}

		return PermissionResponseDto.ResourceDto.builder()
				.key(permission.getResource().getName())
				.name(
						permission.getResource().getDescription() != null
								? permission.getResource().getDescription()
								: permission.getResource().getName())
				.isActive(permission.getResource().getIsActive())
				.build();
	}

	/**
	 * Map Action entity to ActionDto
	 *
	 * @param permission
	 *            the permission containing action
	 * @return action DTO
	 */
	private PermissionResponseDto.ActionDto mapActionDto(Permission permission) {
		if (permission.getAction() == null) {
			return null;
		}

		return PermissionResponseDto.ActionDto.builder()
				.key(permission.getAction().getName())
				.name(
						permission.getAction().getDescription() != null
								? permission.getAction().getDescription()
								: permission.getAction().getName())
				.isActive(permission.getAction().getIsActive())
				.build();
	}

	/**
	 * Convert RolePermission entity to PermissionResponseDto with role-specific
	 * fields
	 *
	 * @param rolePermission
	 *            the role permission entity
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
	 * @param rolePermissions
	 *            list of role permission entities
	 * @return list of permission response DTOs with role-specific information
	 */
	public List<PermissionResponseDto> toRolePermissionResponseDtoList(
			List<RolePermission> rolePermissions) {
		if (rolePermissions == null || rolePermissions.isEmpty()) {
			return List.of();
		}

		return rolePermissions.stream()
				.map(this::toRolePermissionResponseDto)
				.collect(Collectors.toList());
	}

	/**
	 * Convert list of RolePermission entities to PermissionUpdateResponse
	 *
	 * @param roleId
	 *            the role ID
	 * @param rolePermissions
	 *            list of role permission entities that were updated
	 * @return permission update response DTO
	 */
	public PermissionUpdateResponse toPermissionUpdateResponse(
			String roleId, List<RolePermission> rolePermissions) {
		if (rolePermissions == null || rolePermissions.isEmpty()) {
			return PermissionUpdateResponse.builder()
					.roleId(roleId)
					.updatedPermissions(List.of())
					.build();
		}

		List<PermissionUpdateResponse.UpdatedPermissionDto> updatedPermissions = rolePermissions.stream()
				.map(
						rp -> PermissionUpdateResponse.UpdatedPermissionDto.builder()
								.key(rp.getPermission().getPermissionKey())
								.isActive(rp.getIsActive())
								.filterType(
										rp.getFilterType() != null ? rp.getFilterType().getName() : null)
								.build())
				.collect(Collectors.toList());

		return PermissionUpdateResponse.builder()
				.roleId(roleId)
				.updatedPermissions(updatedPermissions)
				.build();
	}

	/**
	 * Convert list of ResourceDto to ResourceTreeResponse with statistics
	 *
	 * @param roleId
	 *            the role ID
	 * @param roleName
	 *            the role name
	 * @param resources
	 *            list of resource DTOs
	 * @return resource tree response with statistics
	 */
	public ResourceTreeResponse toResourceTreeResponse(
			String roleId, String roleName, List<ResourceDto> resources) {
		if (resources == null) {
			resources = List.of();
		}

		// Calculate statistics
		int totalResources = calculateTotalResources(resources);
		int assignedResources = countResourcesWithStatus(resources, true, false);
		int inheritedResources = countResourcesWithStatus(resources, false, true);

		ResourceTreeResponse.TreeStatistics statistics = ResourceTreeResponse.TreeStatistics.builder()
				.rootResourceCount(resources.size())
				.maxDepth(calculateMaxDepth(resources))
				.directAssignments(assignedResources)
				.inheritedAssignments(inheritedResources)
				.coveragePercentage(
						totalResources > 0
								? ((double) (assignedResources + inheritedResources) / totalResources) * 100.0
								: 0.0)
				.build();

		return ResourceTreeResponse.builder()
				.roleId(roleId)
				.roleName(roleName)
				.resources(resources)
				.timestamp(LocalDateTime.now())
				.totalResources(totalResources)
				.assignedResources(assignedResources)
				.inheritedResources(inheritedResources)
				.statistics(statistics)
				.build();
	}

	/** Calculate total number of resources recursively */
	private int calculateTotalResources(List<ResourceDto> resources) {
		int count = resources.size();
		for (ResourceDto resource : resources) {
			count += calculateTotalResources(resource.getChildren());
		}
		return count;
	}

	/** Count resources with specific status (assigned or inherited) */
	private int countResourcesWithStatus(
			List<ResourceDto> resources, boolean assigned, boolean inherited) {
		int count = 0;
		for (ResourceDto resource : resources) {
			if ((assigned && resource.isAssigned()) || (inherited && resource.isInherited())) {
				count++;
			}
			count += countResourcesWithStatus(resource.getChildren(), assigned, inherited);
		}
		return count;
	}

	/** Calculate maximum depth of resource tree */
	private int calculateMaxDepth(List<ResourceDto> resources) {
		int maxDepth = 0;
		for (ResourceDto resource : resources) {
			int childDepth = calculateMaxDepth(resource.getChildren());
			maxDepth = Math.max(maxDepth, childDepth + 1);
		}
		return maxDepth;
	}
}
