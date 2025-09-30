package project.ktc.springboot_app.utils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.auth.enums.UserRoleEnum;

@UtilityClass
@Slf4j
public class SecurityUtil {

  public String getCurrentUserId() {
    try {
      // Get the current authenticated user from security context
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication == null || !authentication.isAuthenticated()) {
        log.warn("No authenticated user found in security context");
        return null;
      }

      Object principal = authentication.getPrincipal();

      if (principal instanceof User user) {
        return user.getId(); // Giả sử getId() từ BaseEntity
      }

      log.warn("Unexpected principal type: {}", principal.getClass().getName());
      return null;
    } catch (Exception e) {
      log.error("Error getting current user ID", e);
      return null;
    }
  }

  public String getCurrentUserEmail() {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth == null || !auth.isAuthenticated()) {
        log.warn("SecurityContext has no authenticated user");
        return null;
      }

      Object principal = auth.getPrincipal();
      if (principal instanceof User user) {
        return user.getEmail();
      }

      log.warn("Unexpected principal type: {}", principal.getClass().getName());
      return null;
    } catch (Exception e) {
      log.error("Error getting current user email", e);
      return null;
    }
  }

  public String getCurrentUserName() {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth == null || !auth.isAuthenticated()) {
        log.warn("SecurityContext has no authenticated user");
        return null;
      }

      Object principal = auth.getPrincipal();
      if (principal instanceof User user) {
        return user.getName();
      }

      log.warn("Unexpected principal type: {}", principal.getClass().getName());
      return null;
    } catch (Exception e) {
      log.error("Error getting current user name", e);
      return null;
    }
  }

  public User getCurrentUser() {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth == null || !auth.isAuthenticated()) {
        log.warn("SecurityContext has no authenticated user");
        return null;
      }

      Object principal = auth.getPrincipal();
      if (principal instanceof User user) {
        return user;
      }

      log.warn("Unexpected principal type: {}", principal.getClass().getName());
      return null;
    } catch (Exception e) {
      log.error("Error getting current user", e);
      return null;
    }
  }

  public List<UserRoleEnum> getCurrentUserRoles() {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth == null || !auth.isAuthenticated()) {
        log.warn("SecurityContext has no authenticated user");
        return Collections.emptyList();
      }

      List<UserRoleEnum> roles =
          auth.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .map(authority -> authority.replace("ROLE_", "")) // Remove ROLE_ prefix if present
              .map(UserRoleEnum::valueOf)
              .collect(Collectors.toList());

      log.info("Current user roles: {}", roles);
      return roles;
    } catch (Exception e) {
      log.error("Error getting current user roles", e);
      return Collections.emptyList();
    }
  }

  public boolean hasRole(String roleName) {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth == null || !auth.isAuthenticated()) {
        return false;
      }

      return auth.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .anyMatch(
              authority -> authority.equals("ROLE_" + roleName) || authority.equals(roleName));
    } catch (Exception e) {
      log.error("Error checking role: {}", roleName, e);
      return false;
    }
  }
}
