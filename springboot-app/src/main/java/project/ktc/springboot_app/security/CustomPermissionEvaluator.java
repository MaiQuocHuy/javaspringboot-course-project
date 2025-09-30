package project.ktc.springboot_app.security;

import java.io.Serializable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.permission.entity.FilterType;
import project.ktc.springboot_app.permission.services.AuthorizationService;
import project.ktc.springboot_app.permission.services.ResourceOwnershipService;

/**
 * Custom Permission Evaluator for Spring Security @PreAuthorize Uses the new
 * database schema with
 * filter_types table
 */
@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class CustomPermissionEvaluator implements PermissionEvaluator {

	private final AuthorizationService authorizationService;
	private final ResourceOwnershipService resourceOwnershipService;

	/**
	 * Evaluate permission for domain object
	 *
	 * @param authentication
	 *            the authentication object
	 * @param targetDomainObject
	 *            the target object (can be null)
	 * @param permission
	 *            the permission string
	 * @return true if access is granted
	 */
	@Override
	public boolean hasPermission(
			Authentication authentication, Object targetDomainObject, Object permission) {
		if (authentication == null || !authentication.isAuthenticated()) {
			log.debug("Authentication is null or not authenticated");
			return false;
		}

		User user = extractUser(authentication);
		if (user == null) {
			log.debug("User not found in authentication");
			return false;
		}
		if (isRejectedRole(user.getRole().getRole())) {
			log.debug("User role is not high enough");
			return false;
		}
		String permissionKey = permission.toString();

		log.debug(
				"Evaluating permission: {} for user: {} with target: {}",
				permissionKey,
				user.getEmail(),
				targetDomainObject != null ? targetDomainObject.getClass().getSimpleName() : "null");

		try {
			AuthorizationService.AuthorizationResult result = authorizationService.evaluatePermission(user,
					permissionKey);

			if (!result.isAllowed()) {
				log.debug(
						"Permission denied: {} for user: {}, reason: {}",
						permissionKey,
						user.getEmail(),
						result.getReason());
				return false;
			}

			// Store the effective filter and user in thread-local context for later use
			EffectiveFilterContext.setCurrentFilter(result.getEffectiveFilter());
			EffectiveFilterContext.setCurrentUser(result.getUser());

			log.debug(
					"Permission granted: {} for user: {} with filter: {}",
					permissionKey,
					user.getEmail(),
					result.getEffectiveFilter());

			return true;

		} catch (Exception e) {
			log.error("Error evaluating permission: {} for user: {}", permissionKey, user.getEmail(), e);
			return false;
		}
	}

	/**
	 * Evaluate permission for target by ID This method handles instance-level
	 * permission checks with
	 * ResourceOwnershipService integration.
	 *
	 * @param authentication
	 *            the authentication object
	 * @param targetId
	 *            the target resource ID (UUID string)
	 * @param targetType
	 *            the target resource type (e.g., "Course", "Review", "Enrollment",
	 *            "User")
	 * @param permission
	 *            the permission string (e.g., "course:READ", "course:WRITE")
	 * @return true if access is granted
	 */
	@Override
	public boolean hasPermission(
			Authentication authentication, Serializable targetId, String targetType, Object permission) {
		if (authentication == null || !authentication.isAuthenticated()) {
			log.debug("Authentication is null or not authenticated");
			return false;
		}

		User user = extractUser(authentication);
		if (user == null) {
			log.debug("User not found in authentication");
			return false;
		}

		if (isRejectedRole(user.getRole().getRole())) {
			log.debug("User role is not high enough");
			return false;
		}

		String permissionKey = permission.toString();
		String resourceId = parseResourceId(targetId);

		log.debug(
				"Evaluating instance-level permission: {} for user: {} on {} with ID: {}",
				permissionKey,
				user.getEmail(),
				targetType,
				resourceId);

		try {
			// Evaluate the permission through the authorization service
			AuthorizationService.AuthorizationResult result = authorizationService.evaluatePermission(user,
					permissionKey);

			if (!result.isAllowed()) {
				log.debug(
						"Permission denied by authorization service: {} for user: {}, reason: {}",
						permissionKey,
						user.getEmail(),
						result.getReason());
				return false;
			}

			// Check instance-level access using ResourceOwnershipService
			boolean hasInstanceAccess = checkInstanceLevelAccess(user, resourceId, targetType,
					result.getEffectiveFilter());

			if (!hasInstanceAccess) {
				log.debug(
						"Instance-level access denied for user: {} on {} {} with filter: {}",
						user.getEmail(),
						targetType,
						resourceId,
						result.getEffectiveFilter());
				return false;
			}

			// Store the effective filter and user in thread-local context for later use
			EffectiveFilterContext.setCurrentFilter(result.getEffectiveFilter());
			EffectiveFilterContext.setCurrentUser(result.getUser());

			log.debug(
					"Instance-level permission granted: {} for user: {} on {} {} with filter: {}",
					permissionKey,
					user.getEmail(),
					targetType,
					resourceId,
					result.getEffectiveFilter());

			return true;

		} catch (Exception e) {
			log.error(
					"Error evaluating instance-level permission: {} for user: {} on {} {}",
					permissionKey,
					user.getEmail(),
					targetType,
					resourceId,
					e);
			return false;
		}
	}

	/**
	 * Check instance-level access based on filter type and ownership.
	 *
	 * @param user
	 *            the authenticated user
	 * @param resourceId
	 *            the resource ID (UUID string)
	 * @param targetType
	 *            the resource type (Course, Enrollment, Review, User)
	 * @param effectiveFilter
	 *            the effective filter type
	 * @return true if access is granted
	 */
	private boolean checkInstanceLevelAccess(
			User user,
			String resourceId,
			String targetType,
			FilterType.EffectiveFilterType effectiveFilter) {

		if (resourceId == null || resourceId.trim().isEmpty()) {
			log.debug("Resource ID is null or empty, denying access");
			return false;
		}

		// ALL filter type grants access to all resources
		if (effectiveFilter == FilterType.EffectiveFilterType.ALL) {
			log.debug(
					"ALL filter grants access to {} {} for user: {}",
					targetType,
					resourceId,
					user.getEmail());
			return true;
		}

		// DENIED filter type denies all access
		if (effectiveFilter == FilterType.EffectiveFilterType.DENIED) {
			log.debug(
					"DENIED filter blocks access to {} {} for user: {}",
					targetType,
					resourceId,
					user.getEmail());
			return false;
		}

		// OWN filter type requires ownership check
		if (effectiveFilter == FilterType.EffectiveFilterType.OWN) {
			boolean hasOwnership = checkResourceOwnership(user, resourceId, targetType);
			log.debug(
					"OWN filter ownership check for {} {}: {} for user: {}",
					targetType,
					resourceId,
					hasOwnership,
					user.getEmail());
			return hasOwnership;
		}

		// Unknown filter type - deny access for security
		log.warn(
				"Unknown filter type: {} for resource {} {}, denying access",
				effectiveFilter,
				targetType,
				resourceId);
		return false;
	}

	/**
	 * Check if user owns or has a relationship with the specified resource.
	 *
	 * @param user
	 *            the user
	 * @param resourceId
	 *            the resource ID (UUID string)
	 * @param targetType
	 *            the resource type
	 * @return true if user has ownership/relationship with the resource
	 */
	private boolean checkResourceOwnership(User user, String resourceId, String targetType) {
		try {
			switch (targetType.toLowerCase()) {
				case "course":
					return resourceOwnershipService.isInstructorOfCourse(user.getId(), resourceId);

				case "enrollment":
					return resourceOwnershipService.isEnrollmentOwner(user.getId(), resourceId);

				case "review":
					return resourceOwnershipService.isReviewAuthor(user.getId(), resourceId);

				case "user":
					return resourceOwnershipService.canAccessUserProfile(user.getId(), resourceId);

				default:
					log.warn("Unknown resource type for ownership check: {}", targetType);
					return false;
			}
		} catch (Exception e) {
			log.error(
					"Error checking ownership for user: {} on {} {}",
					user.getEmail(),
					targetType,
					resourceId,
					e);
			return false;
		}
	}

	/**
	 * Parse resource ID from Serializable object.
	 *
	 * @param targetId
	 *            the target ID object
	 * @return parsed String ID (UUID) or null if parsing fails
	 */
	private String parseResourceId(Serializable targetId) {
		if (targetId == null) {
			return null;
		}

		try {
			if (targetId instanceof String) {
				return (String) targetId;
			}
			if (targetId instanceof Number) {
				return targetId.toString();
			}

			// Convert any other type to string
			String result = targetId.toString();
			log.debug(
					"Converted resource ID from type: {} to string: {}",
					targetId.getClass().getSimpleName(),
					result);
			return result;

		} catch (Exception e) {
			log.warn("Failed to parse resource ID: {}", targetId, e);
			return null;
		}
	}

	/**
	 * Extract User from Authentication object
	 *
	 * @param authentication
	 *            the authentication object
	 * @return User object or null if not found
	 */
	private User extractUser(Authentication authentication) {
		Object principal = authentication.getPrincipal();

		if (principal instanceof User) {
			return (User) principal;
		}

		// If principal is UserDetails, try to get User from it
		if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
			// This would require additional logic to get User from UserDetails
			// For now, assume principal is always User
			log.warn("Principal is UserDetails but not User: {}", principal.getClass());
			return null;
		}

		log.warn("Unknown principal type: {}", principal.getClass());
		return null;
	}

	/** Thread-local context for storing effective filter information */
	public static class EffectiveFilterContext {
		private static final ThreadLocal<FilterType.EffectiveFilterType> currentFilter = new ThreadLocal<>();
		private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

		public static FilterType.EffectiveFilterType getCurrentFilter() {
			return currentFilter.get();
		}

		public static void setCurrentFilter(FilterType.EffectiveFilterType filter) {
			currentFilter.set(filter);
		}

		public static User getCurrentUser() {
			return currentUser.get();
		}

		public static void setCurrentUser(User user) {
			currentUser.set(user);
		}

		public static void clear() {
			currentFilter.remove();
			currentUser.remove();
		}
	}

	/** Check User Role */
	public boolean isRejectedRole(String role) {
		return role.equals("STUDENT") || role.equals("INSTRUCTOR");
	}
}
