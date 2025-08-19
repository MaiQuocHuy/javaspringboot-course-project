package project.ktc.springboot_app.filter_rule.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.filter_rule.enums.EffectiveFilter;
import project.ktc.springboot_app.filter_rule.security.EffectiveFilterContext;
import project.ktc.springboot_app.filter_rule.specifications.EffectiveFilterSpecifications;

import java.util.List;
import java.util.Optional;

/**
 * Example service demonstrating RBAC with effective filter usage
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseAccessService {

    private final CourseRepository courseRepository;
    private final AuthorizationService authorizationService;

    /**
     * Get courses with effective filter applied
     * This method demonstrates how to use the RBAC system in service layer
     * 
     * @param user     the authenticated user
     * @param pageable pagination parameters
     * @return filtered courses based on user's effective permissions
     */
    public Page<Course> getCoursesWithFilter(User user, Pageable pageable) {
        log.debug("Getting courses with filter for user: {}", user.getEmail());

        // Get effective filter for the user
        EffectiveFilter effectiveFilter = authorizationService.getEffectiveFilter(user, "course:READ");

        if (effectiveFilter == EffectiveFilter.DENIED) {
            log.warn("User {} denied access to courses", user.getEmail());
            return Page.empty(pageable);
        }

        // Set context for specification
        EffectiveFilterContext.setCurrentFilter(effectiveFilter);
        EffectiveFilterContext.setCurrentUser(user);

        try {
            // Apply effective filter specification
            Specification<Course> spec = EffectiveFilterSpecifications.applyCourseFilter();

            return courseRepository.findAll(spec, pageable);
        } finally {
            // Clean up context
            EffectiveFilterContext.clear();
        }
    }

    /**
     * Get a specific course with permission check
     * Demonstrates method-level security
     * 
     * @param courseId the course ID
     * @return the course if accessible
     */
    @PreAuthorize("hasPermission(#courseId, 'Course', 'course:READ')")
    public Optional<Course> getCourseById(String courseId) {
        log.debug("Getting course by ID: {}", courseId);

        // The @PreAuthorize annotation will set the effective filter context
        Specification<Course> spec = EffectiveFilterSpecifications.applyCourseFilter();

        // Add ID filter
        Specification<Course> idSpec = (root, query, cb) -> cb.equal(root.get("id"), courseId);

        Specification<Course> combinedSpec = EffectiveFilterSpecifications.and(spec, idSpec);

        return courseRepository.findOne(combinedSpec);
    }

    /**
     * Update course with permission check
     * Demonstrates how to check UPDATE permissions
     * 
     * @param courseId the course ID
     * @param course   the updated course data
     * @return the updated course
     */
    @PreAuthorize("hasPermission(#courseId, 'Course', 'course:UPDATE')")
    public Course updateCourse(String courseId, Course course) {
        log.debug("Updating course: {}", courseId);

        // Permission check passed, proceed with update
        // The effective filter context is available if needed
        EffectiveFilter currentFilter = EffectiveFilterContext.getCurrentFilter();
        User currentUser = EffectiveFilterContext.getCurrentUser();

        log.debug("Updating course with filter: {} for user: {}",
                currentFilter, currentUser != null ? currentUser.getEmail() : "unknown");

        // Perform the update
        course.setId(courseId);
        return courseRepository.save(course);
    }

    /**
     * Get all courses accessible to a user (without pagination)
     * 
     * @param user the authenticated user
     * @return list of accessible courses
     */
    public List<Course> getAllAccessibleCourses(User user) {
        log.debug("Getting all accessible courses for user: {}", user.getEmail());

        AuthorizationService.AuthorizationResult result = authorizationService.evaluatePermission(user, "course:READ");

        if (!result.isHasPermission()) {
            return List.of();
        }

        // Set context
        EffectiveFilterContext.setCurrentFilter(result.getEffectiveFilter());
        EffectiveFilterContext.setCurrentUser(user);

        try {
            Specification<Course> spec = EffectiveFilterSpecifications.applyCourseFilter();
            return courseRepository.findAll(spec);
        } finally {
            EffectiveFilterContext.clear();
        }
    }

    /**
     * Example of combining business logic with RBAC filters
     * 
     * @param user       the authenticated user
     * @param categoryId optional category filter
     * @param pageable   pagination parameters
     * @return filtered courses
     */
    public Page<Course> getCoursesWithBusinessFilter(User user, String categoryId, Pageable pageable) {
        log.debug("Getting courses with business filter for user: {} and category: {}",
                user.getEmail(), categoryId);

        // Check permissions first
        AuthorizationService.AuthorizationResult result = authorizationService.evaluatePermission(user, "course:READ");

        if (!result.isHasPermission()) {
            return Page.empty(pageable);
        }

        // Set context
        EffectiveFilterContext.setCurrentFilter(result.getEffectiveFilter());
        EffectiveFilterContext.setCurrentUser(user);

        try {
            // Combine RBAC filter with business logic
            Specification<Course> rbacSpec = EffectiveFilterSpecifications.applyCourseFilter();

            Specification<Course> businessSpec = EffectiveFilterSpecifications.withAdditionalCriteria(
                    (root, query, cb) -> {
                        if (categoryId != null) {
                            return cb.equal(root.join("categories").get("id"), categoryId);
                        }
                        return cb.conjunction();
                    });

            Specification<Course> combinedSpec = EffectiveFilterSpecifications.and(rbacSpec, businessSpec);

            return courseRepository.findAll(combinedSpec, pageable);
        } finally {
            EffectiveFilterContext.clear();
        }
    }
}
