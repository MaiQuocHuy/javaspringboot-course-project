package project.ktc.springboot_app.permission.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.permission.entity.FilterType;
import project.ktc.springboot_app.permission.services.AuthorizationService;
import project.ktc.springboot_app.permission.security.CustomPermissionEvaluator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Demo controller to test the new permission system
 */
@RestController
@RequestMapping("/api/permissions/test")
@RequiredArgsConstructor
@Slf4j
public class PermissionTestController {

    private final AuthorizationService authorizationService;

    /**
     * Test basic permission check
     */
    @GetMapping("/course-read")
    @PreAuthorize("hasPermission(null, 'course:READ')")
    public ResponseEntity<Map<String, Object>> testCourseRead(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        // Get current filter from context
        FilterType.EffectiveFilterType currentFilter = CustomPermissionEvaluator.EffectiveFilterContext
                .getCurrentFilter();

        User currentUser = CustomPermissionEvaluator.EffectiveFilterContext.getCurrentUser();

        response.put("success", true);
        response.put("message", "course:READ permission granted");
        response.put("effectiveFilter", currentFilter != null ? currentFilter.toString() : "NONE");
        response.put("user", currentUser != null ? currentUser.getEmail() : "UNKNOWN");

        log.info("Permission test successful - Filter: {}, User: {}", currentFilter,
                currentUser != null ? currentUser.getEmail() : "UNKNOWN");

        return ResponseEntity.ok(response);
    }

    /**
     * Test permission with target object
     */
    @GetMapping("/course-update/{courseId}")
    @PreAuthorize("hasPermission(#courseId, 'Course', 'course:UPDATE')")
    public ResponseEntity<Map<String, Object>> testCourseUpdate(
            @PathVariable String courseId,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        FilterType.EffectiveFilterType currentFilter = CustomPermissionEvaluator.EffectiveFilterContext
                .getCurrentFilter();

        User currentUser = CustomPermissionEvaluator.EffectiveFilterContext.getCurrentUser();

        response.put("success", true);
        response.put("message", "course:UPDATE permission granted");
        response.put("courseId", courseId);
        response.put("effectiveFilter", currentFilter != null ? currentFilter.toString() : "NONE");
        response.put("user", currentUser != null ? currentUser.getEmail() : "UNKNOWN");

        log.info("Permission test successful for courseId: {} - Filter: {}, User: {}",
                courseId, currentFilter, currentUser != null ? currentUser.getEmail() : "UNKNOWN");

        return ResponseEntity.ok(response);
    }

    /**
     * Get all user permissions without @PreAuthorize
     */
    @GetMapping("/user-permissions")
    public ResponseEntity<Map<String, Object>> getUserPermissions(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        User user = (User) authentication.getPrincipal();
        Set<String> permissions = authorizationService.getUserPermissions(user);

        response.put("success", true);
        response.put("user", user.getEmail());
        response.put("role", user.getRole().getRole());
        response.put("permissions", permissions);
        response.put("permissionCount", permissions.size());

        log.info("Retrieved {} permissions for user: {}", permissions.size(), user.getEmail());

        return ResponseEntity.ok(response);
    }

    /**
     * Test specific permission check without @PreAuthorize
     */
    @PostMapping("/check-permission")
    public ResponseEntity<Map<String, Object>> checkPermission(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        String permissionKey = request.get("permission");
        if (permissionKey == null || permissionKey.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Permission key is required");
            return ResponseEntity.badRequest().body(response);
        }

        User user = (User) authentication.getPrincipal();
        AuthorizationService.AuthorizationResult result = authorizationService.evaluatePermission(user, permissionKey);

        response.put("success", true);
        response.put("user", user.getEmail());
        response.put("permission", permissionKey);
        response.put("allowed", result.isAllowed());
        response.put("effectiveFilter", result.getEffectiveFilter().toString());
        response.put("reason", result.getReason());

        log.info("Permission check: {} for user: {} - Result: {}",
                permissionKey, user.getEmail(), result.isAllowed());

        return ResponseEntity.ok(response);
    }

    /**
     * Test filter type check
     */
    @PostMapping("/check-filter-type")
    public ResponseEntity<Map<String, Object>> checkFilterType(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        String permissionKey = request.get("permission");
        String filterTypeStr = request.get("filterType");

        if (permissionKey == null || filterTypeStr == null) {
            response.put("success", false);
            response.put("message", "Permission and filterType are required");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            FilterType.EffectiveFilterType filterType = FilterType.EffectiveFilterType
                    .valueOf(filterTypeStr.toUpperCase());

            User user = (User) authentication.getPrincipal();
            boolean hasFilterType = authorizationService.hasFilterType(user, permissionKey, filterType);

            response.put("success", true);
            response.put("user", user.getEmail());
            response.put("permission", permissionKey);
            response.put("filterType", filterType.toString());
            response.put("hasFilterType", hasFilterType);

            log.info("Filter type check: {} {} for user: {} - Result: {}",
                    permissionKey, filterType, user.getEmail(), hasFilterType);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Invalid filter type: " + filterTypeStr);
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Clear filter context (for testing)
     */
    @PostMapping("/clear-context")
    public ResponseEntity<Map<String, Object>> clearContext() {
        CustomPermissionEvaluator.EffectiveFilterContext.clear();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Filter context cleared");

        return ResponseEntity.ok(response);
    }
}
