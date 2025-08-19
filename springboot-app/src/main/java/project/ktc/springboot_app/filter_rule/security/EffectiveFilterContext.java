package project.ktc.springboot_app.filter_rule.security;

import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.filter_rule.enums.EffectiveFilter;

/**
 * Thread-local context for storing effective filter information
 * Used to pass filter context from PermissionEvaluator to repository layer
 */
public class EffectiveFilterContext {

    private static final ThreadLocal<EffectiveFilter> currentFilter = new ThreadLocal<>();
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();
    private static final ThreadLocal<Object> targetObject = new ThreadLocal<>();

    /**
     * Set the effective filter for current thread
     * 
     * @param filter the effective filter
     */
    public static void setCurrentFilter(EffectiveFilter filter) {
        currentFilter.set(filter);
    }

    /**
     * Get the effective filter for current thread
     * 
     * @return the effective filter or null if not set
     */
    public static EffectiveFilter getCurrentFilter() {
        return currentFilter.get();
    }

    /**
     * Set the current user for current thread
     * 
     * @param user the authenticated user
     */
    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

    /**
     * Get the current user for current thread
     * 
     * @return the user or null if not set
     */
    public static User getCurrentUser() {
        return currentUser.get();
    }

    /**
     * Set the target object for current thread
     * 
     * @param target the target object
     */
    public static void setTargetObject(Object target) {
        targetObject.set(target);
    }

    /**
     * Get the target object for current thread
     * 
     * @return the target object or null if not set
     */
    public static Object getTargetObject() {
        return targetObject.get();
    }

    /**
     * Clear all thread-local variables
     * Should be called after request processing
     */
    public static void clear() {
        currentFilter.remove();
        currentUser.remove();
        targetObject.remove();
    }

    /**
     * Check if context has been set
     * 
     * @return true if filter and user are set
     */
    public static boolean hasContext() {
        return currentFilter.get() != null && currentUser.get() != null;
    }
}
