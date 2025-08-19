package project.ktc.springboot_app.filter_rule.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.filter_rule.entity.RoleFilterRule;
import project.ktc.springboot_app.filter_rule.interfaces.RoleFilterRuleService;
import project.ktc.springboot_app.filter_rule.repositories.RoleFilterRuleRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of RoleFilterRuleService
 * Handles scope-based permission filtering logic according to Decision Table
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoleFilterRuleServiceImpl implements RoleFilterRuleService {

    private final RoleFilterRuleRepository roleFilterRuleRepository;

    @Override
    public Set<RoleFilterRule.FilterType> getFilterTypesForUser(User user, String permissionKey) {
        log.debug("Getting filter types for user: {} and permission: {}", user.getId(), permissionKey);

        String roleId = user.getRole().getId();
        Set<String> roleIds = Set.of(roleId);

        return getFilterTypesForRoles(roleIds, permissionKey);
    }

    @Override
    public Set<RoleFilterRule.FilterType> getFilterTypesForRoles(Set<String> roleIds, String permissionKey) {
        log.debug("Getting filter types for roles: {} and permission: {}", roleIds, permissionKey);

        List<RoleFilterRule> rules = roleFilterRuleRepository.findActiveRulesByRolesAndPermission(roleIds,
                permissionKey);

        Set<RoleFilterRule.FilterType> filterTypes = rules.stream()
                .map(RoleFilterRule::getFilterType)
                .collect(Collectors.toSet());

        log.debug("Found filter types: {} for roles: {} and permission: {}", filterTypes, roleIds, permissionKey);
        return filterTypes;
    }

    @Override
    public boolean hasFilterType(User user, String permissionKey, RoleFilterRule.FilterType filterType) {
        Set<RoleFilterRule.FilterType> userFilterTypes = getFilterTypesForUser(user, permissionKey);
        return userFilterTypes.contains(filterType);
    }

    @Override
    public List<RoleFilterRule> getFilterRulesForUser(User user, String permissionKey) {
        log.debug("Getting filter rules for user: {} and permission: {}", user.getId(), permissionKey);

        String roleId = user.getRole().getId();
        Set<String> roleIds = Set.of(roleId);

        return roleFilterRuleRepository.findActiveRulesByRolesAndPermission(roleIds, permissionKey);
    }

    @Override
    public boolean hasAnyFilterRules(User user, String permissionKey) {
        log.debug("Checking if user: {} has any filter rules for permission: {}", user.getId(), permissionKey);

        String roleId = user.getRole().getId();
        Set<String> roleIds = Set.of(roleId);

        List<RoleFilterRule> rules = roleFilterRuleRepository.findActiveRulesByRolesAndPermission(roleIds,
                permissionKey);
        boolean hasRules = !rules.isEmpty();

        log.debug("User: {} has filter rules for permission {}: {}", user.getId(), permissionKey, hasRules);
        return hasRules;
    }

    @Override
    public Set<String> getPermissionsWithFilterRules(User user) {
        log.debug("Getting permissions with filter rules for user: {}", user.getId());

        String roleId = user.getRole().getId();
        Set<String> roleIds = Set.of(roleId);

        List<RoleFilterRule> rules = roleFilterRuleRepository.findActiveRulesByRoles(roleIds);

        return rules.stream()
                .map(rule -> rule.getRolePermission().getPermissionKey())
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public RoleFilterRule createFilterRule(String rolePermissionId, RoleFilterRule.FilterType filterType) {
        log.info("Creating filter rule - rolePermissionId: {}, filterType: {}", rolePermissionId, filterType);

        // Check if rule already exists for this role permission
        boolean exists = roleFilterRuleRepository.existsActiveRule(rolePermissionId, filterType);
        if (exists) {
            throw new IllegalArgumentException(
                    String.format("Filter rule already exists for rolePermissionId: %s",
                            rolePermissionId));
        }

        RoleFilterRule rule = RoleFilterRule.builder()
                .rolePermissionId(rolePermissionId)
                .filterType(filterType)
                .isActive(true)
                .build();

        RoleFilterRule savedRule = roleFilterRuleRepository.save(rule);
        log.info("Created filter rule with ID: {}", savedRule.getId());

        return savedRule;
    }

    @Override
    @Transactional
    public RoleFilterRule updateFilterRuleStatus(String ruleId, boolean isActive) {
        log.info("Updating filter rule status - ruleId: {}, isActive: {}", ruleId, isActive);

        RoleFilterRule rule = roleFilterRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Filter rule not found with ID: " + ruleId));

        rule.setIsActive(isActive);
        RoleFilterRule updatedRule = roleFilterRuleRepository.save(rule);

        log.info("Updated filter rule status - ruleId: {}, newStatus: {}", ruleId, isActive);
        return updatedRule;
    }

    @Override
    @Transactional
    public void deleteFilterRule(String ruleId) {
        log.info("Deleting filter rule with ID: {}", ruleId);

        if (!roleFilterRuleRepository.existsById(ruleId)) {
            throw new IllegalArgumentException("Filter rule not found with ID: " + ruleId);
        }

        roleFilterRuleRepository.deleteById(ruleId);
        log.info("Deleted filter rule with ID: {}", ruleId);
    }

    @Override
    public List<RoleFilterRule> getFilterRulesByRole(String roleId) {
        log.debug("Getting all filter rules for role: {}", roleId);
        return roleFilterRuleRepository.findByRoleIdOrderByPermissionKeyAscFilterTypeAsc(roleId);
    }

    @Override
    public List<RoleFilterRule> getFilterRulesByPermission(String permissionKey) {
        log.debug("Getting all filter rules for permission: {}", permissionKey);
        return roleFilterRuleRepository.findByPermissionKeyAndIsActiveTrue(permissionKey);
    }
}
