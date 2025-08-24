package project.ktc.springboot_app.permission.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.enrollment.entity.Enrollment;
import project.ktc.springboot_app.review.entity.Review;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;
import project.ktc.springboot_app.review.repositories.ReviewRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;

import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralized service for checking resource ownership across different
 * entities.
 * This service provides a unified way to determine if a user has ownership
 * or special relationships with various resources in the system.
 * 
 * Used by CustomPermissionEvaluator for instance-level permission checks.
 * 
 * @author Generated
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ResourceOwnershipService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    // ========================================
    // Course Ownership & Relationships
    // ========================================

    /**
     * Checks if the user is the instructor of the specified course.
     * 
     * @param userId   the user ID to check
     * @param courseId the course ID to check
     * @return true if user is the instructor of the course
     */
    public boolean isInstructorOfCourse(String userId, String courseId) {
        log.debug("Checking if user {} is instructor of course {}", userId, courseId);

        boolean isInstructor = courseRepository.existsByIdAndInstructorId(courseId, userId);

        log.debug("User {} {} instructor of course {}",
                userId, isInstructor ? "is" : "is not", courseId);

        return isInstructor;
    }

    /**
     * Checks if the user is enrolled in the specified course.
     * 
     * @param userId   the user ID to check
     * @param courseId the course ID to check
     * @return true if user is enrolled in the course
     */
    public boolean isEnrolledInCourse(String userId, String courseId) {
        log.debug("Checking if user {} is enrolled in course {}", userId, courseId);

        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);

        log.debug("User {} {} enrolled in course {}",
                userId, isEnrolled ? "is" : "is not", courseId);

        return isEnrolled;
    }

    /**
     * Checks if the user has any relationship with the course (instructor or
     * enrolled).
     * 
     * @param userId   the user ID to check
     * @param courseId the course ID to check
     * @return true if user has any relationship with the course
     */
    public boolean hasRelationshipWithCourse(String userId, String courseId) {
        log.debug("Checking if user {} has any relationship with course {}", userId, courseId);

        boolean hasRelationship = isInstructorOfCourse(userId, courseId) ||
                isEnrolledInCourse(userId, courseId);

        log.debug("User {} {} relationship with course {}",
                userId, hasRelationship ? "has" : "has no", courseId);

        return hasRelationship;
    }

    // ========================================
    // Review Ownership & Relationships
    // ========================================

    /**
     * Checks if the user is the author of the specified review.
     * 
     * @param userId   the user ID to check
     * @param reviewId the review ID to check
     * @return true if user is the author of the review
     */
    public boolean isReviewAuthor(String userId, String reviewId) {
        log.debug("Checking if user {} is author of review {}", userId, reviewId);

        boolean isAuthor = reviewRepository.existsByIdAndUserId(reviewId, userId);

        log.debug("User {} {} author of review {}",
                userId, isAuthor ? "is" : "is not", reviewId);

        return isAuthor;
    }

    /**
     * Checks if the user can access the review (either as author or instructor of
     * the course).
     * 
     * @param userId   the user ID to check
     * @param reviewId the review ID to check
     * @return true if user can access the review
     */
    public boolean canAccessReview(String userId, String reviewId) {
        log.debug("Checking if user {} can access review {}", userId, reviewId);

        // Check if user is the review author
        if (isReviewAuthor(userId, reviewId)) {
            log.debug("User {} can access review {} as author", userId, reviewId);
            return true;
        }

        // Check if user is instructor of the reviewed course
        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isPresent()) {
            boolean canAccess = isInstructorOfCourse(userId, review.get().getCourse().getId());
            log.debug("User {} {} access review {} as course instructor",
                    userId, canAccess ? "can" : "cannot", reviewId);
            return canAccess;
        }

        log.debug("User {} cannot access review {} - review not found", userId, reviewId);
        return false;
    }

    // ========================================
    // Enrollment Ownership & Relationships
    // ========================================

    /**
     * Checks if the user owns the specified enrollment.
     * 
     * @param userId       the user ID to check
     * @param enrollmentId the enrollment ID to check
     * @return true if user owns the enrollment
     */
    public boolean isEnrollmentOwner(String userId, String enrollmentId) {
        log.debug("Checking if user {} owns enrollment {}", userId, enrollmentId);

        boolean isOwner = enrollmentRepository.existsByIdAndUserId(enrollmentId, userId);

        log.debug("User {} {} owner of enrollment {}",
                userId, isOwner ? "is" : "is not", enrollmentId);

        return isOwner;
    }

    /**
     * Checks if the user can access the enrollment (either as owner or instructor).
     * 
     * @param userId       the user ID to check
     * @param enrollmentId the enrollment ID to check
     * @return true if user can access the enrollment
     */
    public boolean canAccessEnrollment(String userId, String enrollmentId) {
        log.debug("Checking if user {} can access enrollment {}", userId, enrollmentId);

        // Check if user owns the enrollment
        if (isEnrollmentOwner(userId, enrollmentId)) {
            log.debug("User {} can access enrollment {} as owner", userId, enrollmentId);
            return true;
        }

        // Check if user is instructor of the enrolled course
        Optional<Enrollment> enrollment = enrollmentRepository.findById(enrollmentId);
        if (enrollment.isPresent()) {
            boolean canAccess = isInstructorOfCourse(userId, enrollment.get().getCourse().getId());
            log.debug("User {} {} access enrollment {} as course instructor",
                    userId, canAccess ? "can" : "cannot", enrollmentId);
            return canAccess;
        }

        log.debug("User {} cannot access enrollment {} - enrollment not found", userId, enrollmentId);
        return false;
    }

    // ========================================
    // User Profile Access
    // ========================================

    /**
     * Checks if the user can access the specified user profile.
     * Users can access their own profile and potentially others based on business
     * rules.
     * 
     * @param currentUserId the current user ID
     * @param targetUserId  the target user ID to access
     * @return true if current user can access target user profile
     */
    public boolean canAccessUserProfile(String currentUserId, String targetUserId) {
        log.debug("Checking if user {} can access user profile {}", currentUserId, targetUserId);

        // Users can always access their own profile
        boolean canAccess = currentUserId.equals(targetUserId);

        log.debug("User {} {} access user profile {}",
                currentUserId, canAccess ? "can" : "cannot", targetUserId);

        return canAccess;
    }

    // ========================================
    // Generic Resource Ownership Methods
    // ========================================

    /**
     * Generic method to check resource ownership based on resource type.
     * 
     * @param userId       the user ID to check
     * @param resourceId   the resource ID to check
     * @param resourceType the type of resource ("Course", "Review", "Enrollment",
     *                     "User")
     * @return true if user owns or can access the resource
     */
    public boolean isResourceOwner(String userId, String resourceId, String resourceType) {
        log.debug("Checking resource ownership: user={}, resourceId={}, resourceType={}",
                userId, resourceId, resourceType);

        boolean isOwner = switch (resourceType.toLowerCase()) {
            case "course" -> isInstructorOfCourse(userId, resourceId);
            case "review" -> isReviewAuthor(userId, resourceId);
            case "enrollment" -> isEnrollmentOwner(userId, resourceId);
            case "user" -> canAccessUserProfile(userId, resourceId);
            default -> {
                log.warn("Unknown resource type: {}", resourceType);
                yield false;
            }
        };

        log.debug("User {} {} owner of {} {}",
                userId, isOwner ? "is" : "is not", resourceType, resourceId);

        return isOwner;
    }

    /**
     * Checks if user has any relationship with the resource (broader than
     * ownership).
     * 
     * @param userId       the user ID to check
     * @param resourceId   the resource ID to check
     * @param resourceType the type of resource
     * @return true if user has any relationship with the resource
     */
    public boolean hasResourceRelationship(String userId, String resourceId, String resourceType) {
        log.debug("Checking resource relationship: user={}, resourceId={}, resourceType={}",
                userId, resourceId, resourceType);

        boolean hasRelationship = switch (resourceType.toLowerCase()) {
            case "course" -> hasRelationshipWithCourse(userId, resourceId);
            case "review" -> canAccessReview(userId, resourceId);
            case "enrollment" -> canAccessEnrollment(userId, resourceId);
            case "user" -> canAccessUserProfile(userId, resourceId);
            default -> {
                log.warn("Unknown resource type: {}", resourceType);
                yield false;
            }
        };

        log.debug("User {} {} relationship with {} {}",
                userId, hasRelationship ? "has" : "has no", resourceType, resourceId);

        return hasRelationship;
    }

    // ========================================
    // Batch Operations
    // ========================================

    /**
     * Checks if user owns all specified resources of the given type.
     * 
     * @param userId       the user ID to check
     * @param resourceIds  list of resource IDs to check
     * @param resourceType the type of resources
     * @return true if user owns all specified resources
     */
    public boolean ownsAllResources(String userId, List<String> resourceIds, String resourceType) {
        log.debug("Checking if user {} owns all {} resources: {}", userId, resourceType, resourceIds);

        if (resourceIds == null || resourceIds.isEmpty()) {
            log.debug("No resources to check - returning true");
            return true;
        }

        boolean ownsAll = resourceIds.stream()
                .allMatch(resourceId -> isResourceOwner(userId, resourceId, resourceType));

        log.debug("User {} {} all {} resources",
                userId, ownsAll ? "owns" : "does not own", resourceType);

        return ownsAll;
    }

    /**
     * Checks if user owns any of the specified resources of the given type.
     * 
     * @param userId       the user ID to check
     * @param resourceIds  list of resource IDs to check
     * @param resourceType the type of resources
     * @return true if user owns any of the specified resources
     */
    public boolean ownsAnyResource(String userId, List<String> resourceIds, String resourceType) {
        log.debug("Checking if user {} owns any {} resources: {}", userId, resourceType, resourceIds);

        if (resourceIds == null || resourceIds.isEmpty()) {
            log.debug("No resources to check - returning false");
            return false;
        }

        boolean ownsAny = resourceIds.stream()
                .anyMatch(resourceId -> isResourceOwner(userId, resourceId, resourceType));

        log.debug("User {} {} any {} resources",
                userId, ownsAny ? "owns" : "does not own", resourceType);

        return ownsAny;
    }

    /**
     * Returns a map of resource IDs to ownership status for the specified user.
     * 
     * @param userId       the user ID to check
     * @param resourceIds  list of resource IDs to check
     * @param resourceType the type of resources
     * @return map of resourceId -> ownership status
     */
    public Map<String, Boolean> getOwnershipStatus(String userId, List<String> resourceIds, String resourceType) {
        log.debug("Getting ownership status for user {} on {} resources: {}",
                userId, resourceType, resourceIds);

        if (resourceIds == null || resourceIds.isEmpty()) {
            log.debug("No resources to check - returning empty map");
            return Map.of();
        }

        Map<String, Boolean> ownershipMap = resourceIds.stream()
                .collect(Collectors.toMap(
                        resourceId -> resourceId,
                        resourceId -> isResourceOwner(userId, resourceId, resourceType)));

        log.debug("Ownership status for user {}: {}", userId, ownershipMap);

        return ownershipMap;
    }

    // ========================================
    // Utility Methods
    // ========================================

    /**
     * Checks if a resource exists.
     * 
     * @param resourceId   the resource ID to check
     * @param resourceType the type of resource
     * @return true if resource exists
     */
    public boolean resourceExists(String resourceId, String resourceType) {
        log.debug("Checking if {} {} exists", resourceType, resourceId);

        boolean exists = switch (resourceType.toLowerCase()) {
            case "course" -> courseRepository.existsById(resourceId);
            case "review" -> reviewRepository.existsById(resourceId);
            case "enrollment" -> enrollmentRepository.existsById(resourceId);
            case "user" -> userRepository.existsById(resourceId);
            default -> {
                log.warn("Unknown resource type: {}", resourceType);
                yield false;
            }
        };

        log.debug("{} {} {}", resourceType, resourceId, exists ? "exists" : "does not exist");

        return exists;
    }

    /**
     * Gets the owner ID of a resource (if applicable).
     * 
     * @param resourceId   the resource ID
     * @param resourceType the type of resource
     * @return Optional containing the owner ID, or empty if not found/applicable
     */
    public Optional<String> getResourceOwnerId(String resourceId, String resourceType) {
        log.debug("Getting owner ID for {} {}", resourceType, resourceId);

        Optional<String> ownerId = switch (resourceType.toLowerCase()) {
            case "course" -> courseRepository.findById(resourceId)
                    .map(Course::getInstructor)
                    .map(User::getId);
            case "review" -> reviewRepository.findById(resourceId)
                    .map(Review::getUser)
                    .map(User::getId);
            case "enrollment" -> enrollmentRepository.findById(resourceId)
                    .map(Enrollment::getUser)
                    .map(User::getId);
            case "user" -> Optional.of(resourceId); // User owns themselves
            default -> {
                log.warn("Unknown resource type: {}", resourceType);
                yield Optional.empty();
            }
        };

        log.debug("Owner ID for {} {}: {}", resourceType, resourceId,
                ownerId.map(String::valueOf).orElse("not found"));

        return ownerId;
    }
}
