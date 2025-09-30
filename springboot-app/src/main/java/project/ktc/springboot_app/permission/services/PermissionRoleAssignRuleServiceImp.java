package project.ktc.springboot_app.permission.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.permission.entity.Permission;
import project.ktc.springboot_app.permission.entity.PermissionRoleAssignRule;
import project.ktc.springboot_app.permission.interfaces.PermissionRoleAssignRuleService;
import project.ktc.springboot_app.permission.repositories.PermissionRepository;
import project.ktc.springboot_app.permission.repositories.PermissionRoleAssignRuleRepository;
import project.ktc.springboot_app.user_role.repositories.UserRoleRepository;

/**
 * Implementation of PermissionRoleAssignRuleService Handles business logic for
 * permission-role
 * assignment constraints
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PermissionRoleAssignRuleServiceImp implements PermissionRoleAssignRuleService {

	private static final Logger logger = LoggerFactory.getLogger(PermissionRoleAssignRuleServiceImp.class);

	private final PermissionRoleAssignRuleRepository assignRuleRepository;
	private final UserRoleRepository userRoleRepository;
	private final PermissionRepository permissionRepository;

	@Override
	public boolean canAssignPermissionToRole(String roleId, String permissionId) {
		logger.debug("Checking if permission {} can be assigned to role {}", permissionId, roleId);

		if (roleId == null || permissionId == null) {
			logger.warn("Role ID or Permission ID is null");
			return false;
		}

		boolean canAssign = assignRuleRepository.canAssignPermissionToRole(roleId, permissionId);
		logger.debug("Permission assignment check result: {}", canAssign);

		return canAssign;
	}

	@Override
	public Set<String> getAssignablePermissionIds(String roleId) {
		logger.debug("Getting assignable permission IDs for role: {}", roleId);

		if (roleId == null) {
			logger.warn("Role ID is null");
			return Set.of();
		}

		Set<String> permissionIds = assignRuleRepository.findAssignablePermissionIdsByRoleId(roleId);
		logger.debug("Found {} assignable permissions for role {}", permissionIds.size(), roleId);

		return permissionIds;
	}

	@Override
	public Set<String> getAssignablePermissionKeys(String roleId) {
		logger.debug("Getting assignable permission keys for role: {}", roleId);

		if (roleId == null) {
			logger.warn("Role ID is null");
			return Set.of();
		}

		Set<String> permissionKeys = assignRuleRepository.findAssignablePermissionKeysByRoleId(roleId);
		logger.debug("Found {} assignable permission keys for role {}", permissionKeys.size(), roleId);

		return permissionKeys;
	}

	@Override
	public List<PermissionRoleAssignRule> getAssignmentRulesByRole(String roleId) {
		logger.debug("Getting assignment rules for role: {}", roleId);

		if (roleId == null) {
			logger.warn("Role ID is null");
			return List.of();
		}

		List<PermissionRoleAssignRule> rules = assignRuleRepository.findByRoleId(roleId);
		logger.debug("Found {} assignment rules for role {}", rules.size(), roleId);

		return rules;
	}

	@Override
	public List<PermissionRoleAssignRule> getActiveAssignmentRulesByRole(String roleId) {
		logger.debug("Getting active assignment rules for role: {}", roleId);

		if (roleId == null) {
			logger.warn("Role ID is null");
			return List.of();
		}

		List<PermissionRoleAssignRule> rules = assignRuleRepository.findActiveByRoleId(roleId);
		logger.debug("Found {} active assignment rules for role {}", rules.size(), roleId);

		return rules;
	}

	@Override
	public List<PermissionRoleAssignRule> getRolesByPermission(String permissionId) {
		logger.debug("Getting roles that can be assigned permission: {}", permissionId);

		if (permissionId == null) {
			logger.warn("Permission ID is null");
			return List.of();
		}

		List<PermissionRoleAssignRule> rules = assignRuleRepository.findRolesByPermissionId(permissionId);
		logger.debug("Found {} roles for permission {}", rules.size(), permissionId);

		return rules;
	}

	@Override
	public long countAssignablePermissions(String roleId) {
		logger.debug("Counting assignable permissions for role: {}", roleId);

		if (roleId == null) {
			logger.warn("Role ID is null");
			return 0;
		}

		long count = assignRuleRepository.countAssignablePermissionsByRoleId(roleId);
		logger.debug("Found {} assignable permissions for role {}", count, roleId);

		return count;
	}

	@Override
	@Transactional
	public PermissionRoleAssignRule createAssignmentRule(
			String roleId, String permissionId, boolean isActive) {
		logger.info(
				"Creating assignment rule: role={}, permission={}, active={}",
				roleId,
				permissionId,
				isActive);

		// Validate inputs
		if (roleId == null || permissionId == null) {
			throw new IllegalArgumentException("Role ID and Permission ID must not be null");
		}

		// Check if rule already exists
		PermissionRoleAssignRule existingRule = assignRuleRepository.findByRoleIdAndPermissionId(roleId, permissionId);
		if (existingRule != null) {
			throw new IllegalArgumentException(
					"Assignment rule already exists for role " + roleId + " and permission " + permissionId);
		}

		// Validate role exists
		UserRole role = userRoleRepository
				.findById(roleId)
				.orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

		// Validate permission exists
		Permission permission = permissionRepository
				.findById(permissionId)
				.orElseThrow(
						() -> new IllegalArgumentException("Permission not found with ID: " + permissionId));

		// Create new assignment rule
		PermissionRoleAssignRule newRule = PermissionRoleAssignRule.builder()
				.role(role)
				.permission(permission)
				.isActive(isActive)
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();

		PermissionRoleAssignRule savedRule = assignRuleRepository.save(newRule);
		logger.info("Successfully created assignment rule with ID: {}", savedRule.getId());

		return savedRule;
	}

	@Override
	@Transactional
	public PermissionRoleAssignRule updateAssignmentRule(String ruleId, boolean isActive) {
		logger.info("Updating assignment rule: ruleId={}, active={}", ruleId, isActive);

		if (ruleId == null) {
			throw new IllegalArgumentException("Rule ID must not be null");
		}

		// Find existing rule
		PermissionRoleAssignRule existingRule = assignRuleRepository
				.findById(ruleId)
				.orElseThrow(
						() -> new IllegalArgumentException("Assignment rule not found with ID: " + ruleId));

		// Update the rule
		existingRule.setIsActive(isActive);
		existingRule.setUpdatedAt(LocalDateTime.now());

		PermissionRoleAssignRule updatedRule = assignRuleRepository.save(existingRule);
		logger.info("Successfully updated assignment rule with ID: {}", updatedRule.getId());

		return updatedRule;
	}

	@Override
	@Transactional
	public void deleteAssignmentRule(String ruleId) {
		logger.info("Deleting assignment rule with ID: {}", ruleId);

		if (ruleId == null) {
			throw new IllegalArgumentException("Rule ID must not be null");
		}

		// Check if rule exists
		if (!assignRuleRepository.existsById(ruleId)) {
			throw new IllegalArgumentException("Assignment rule not found with ID: " + ruleId);
		}

		assignRuleRepository.deleteById(ruleId);
		logger.info("Successfully deleted assignment rule with ID: {}", ruleId);
	}

	@Override
	public void validatePermissionAssignment(String roleId, String permissionId) {
		logger.debug("Validating permission assignment: role={}, permission={}", roleId, permissionId);

		if (roleId == null || permissionId == null) {
			throw new IllegalArgumentException("Role ID and Permission ID must not be null");
		}

		boolean canAssign = canAssignPermissionToRole(roleId, permissionId);
		if (!canAssign) {
			throw new IllegalArgumentException(
					String.format(
							"Permission %s cannot be assigned to role %s. Check permission_role_assign_rules table.",
							permissionId, roleId));
		}

		logger.debug(
				"Permission assignment validation passed for role {} and permission {}",
				roleId,
				permissionId);
	}
}
