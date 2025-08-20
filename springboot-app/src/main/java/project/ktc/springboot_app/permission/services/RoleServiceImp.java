package project.ktc.springboot_app.permission.services;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.permission.dto.CreateRoleRequest;
import project.ktc.springboot_app.permission.interfaces.RoleService;
import project.ktc.springboot_app.user_role.repositories.UserRoleRepository;

/**
 * Implementation of RoleService for role management operations
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImp implements RoleService {

    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional
    public UserRole createRole(CreateRoleRequest request) {
        log.info("Creating new role with name: {}", request.getName());

        // Validate role name uniqueness
        String roleName = request.getName().toUpperCase().trim();
        if (existsByRoleName(roleName)) {
            log.warn("Role creation failed - role name already exists: {}", roleName);
            throw new IllegalArgumentException("Role name '" + roleName + "' already exists");
        }

        try {
            // Convert string to RoleType enum
            String roleType = roleName;

            // Create new role entity
            UserRole newRole = UserRole.builder()
                    .role(roleType)
                    .build();

            // Save role to database
            UserRole savedRole = userRoleRepository.save(newRole);
            log.info("Role created successfully with ID: {} and name: {}", savedRole.getId(), savedRole.getRole());

            return savedRole;

        } catch (IllegalArgumentException e) {
            log.error("Invalid role name provided: {}. Valid roles are: STUDENT, INSTRUCTOR, ADMIN", roleName);
            throw new IllegalArgumentException(
                    "Invalid role name '" + roleName + "'. Valid roles are: STUDENT, INSTRUCTOR, ADMIN");
        } catch (DataIntegrityViolationException e) {
            log.error("Database constraint violation while creating role: {}", roleName, e);
            throw new IllegalArgumentException("Role name '" + roleName + "' already exists");
        } catch (Exception e) {
            log.error("Unexpected error while creating role: {}", roleName, e);
            throw new RuntimeException("Failed to create role due to internal server error");
        }
    }

    @Override
    public UserRole findById(String roleId) {
        log.debug("Finding role by ID: {}", roleId);
        return userRoleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.warn("Role not found with ID: {}", roleId);
                    return new RuntimeException("Role not found with ID: " + roleId);
                });
    }

    @Override
    public UserRole findByRoleType(String roleType) {
        log.debug("Finding role by type: {}", roleType);
        return userRoleRepository.findByRole(roleType).orElse(null);
    }

    @Override
    public boolean existsByRoleName(String roleName) {
        try {
            String roleType = roleName.toUpperCase().trim();
            boolean exists = userRoleRepository.findByRole(roleType).isPresent();
            log.debug("Role existence check for '{}': {}", roleName, exists);
            return exists;
        } catch (IllegalArgumentException e) {
            log.debug("Invalid role name for existence check: {}", roleName);
            return false;
        }
    }

    @Override
    public List<UserRole> getAllRoles() {
        log.debug("Retrieving all roles");
        List<UserRole> roles = userRoleRepository.findAll();
        log.debug("Found {} roles", roles.size());
        return roles;
    }
}
