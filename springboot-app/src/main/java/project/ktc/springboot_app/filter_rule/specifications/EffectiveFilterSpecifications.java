package project.ktc.springboot_app.filter_rule.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.filter_rule.enums.EffectiveFilter;
import project.ktc.springboot_app.filter_rule.security.EffectiveFilterContext;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for applying effective filters dynamically
 * Provides reusable filter logic for different entities
 */
@Slf4j
public class EffectiveFilterSpecifications {

    /**
     * Apply effective filter to Course entities
     * 
     * @return Specification that applies the current effective filter
     */
    public static Specification<Course> applyCourseFilter() {
        return (Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            EffectiveFilter filter = EffectiveFilterContext.getCurrentFilter();
            User currentUser = EffectiveFilterContext.getCurrentUser();

            if (filter == null) {
                log.warn("No effective filter found in context, denying access");
                return cb.disjunction(); // Always false - deny access
            }

            log.debug("Applying filter: {} for user: {}", filter,
                    currentUser != null ? currentUser.getEmail() : "unknown");

            return switch (filter) {
                case ALL -> cb.conjunction(); // Always true - no restrictions

                case OWN -> {
                    if (currentUser == null) {
                        log.warn("OWN filter requires user context but none found");
                        yield cb.disjunction();
                    }
                    // Access only to user's own courses (as instructor)
                    yield cb.equal(root.get("instructor").get("id"), currentUser.getId());
                }

                case PUBLISHED_ONLY -> {
                    // Access only to published and approved courses
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.isTrue(root.get("isPublished")));
                    predicates.add(cb.isTrue(root.get("isApproved")));
                    predicates.add(cb.isFalse(root.get("isDeleted")));
                    yield cb.and(predicates.toArray(new Predicate[0]));
                }

                case DENIED -> {
                    log.debug("Access denied - returning false predicate");
                    yield cb.disjunction(); // Always false - deny access
                }
            };
        };
    }

    /**
     * Combine multiple specifications with AND logic
     * 
     * @param specifications array of specifications to combine
     * @return combined specification
     */
    @SafeVarargs
    public static <T> Specification<T> and(Specification<T>... specifications) {
        return Specification.allOf(specifications);
    }

    /**
     * Combine multiple specifications with OR logic
     * 
     * @param specifications array of specifications to combine
     * @return combined specification
     */
    @SafeVarargs
    public static <T> Specification<T> or(Specification<T>... specifications) {
        return Specification.anyOf(specifications);
    }

    /**
     * Create a specification for additional business rules
     * This can be combined with effective filter specifications
     * 
     * @param additionalCriteria custom predicate
     * @return specification with additional criteria
     */
    public static <T> Specification<T> withAdditionalCriteria(
            SpecificationCriteria<T> additionalCriteria) {
        return (root, query, cb) -> additionalCriteria.toPredicate(root, query, cb);
    }

    /**
     * Functional interface for custom specification criteria
     */
    @FunctionalInterface
    public interface SpecificationCriteria<T> {
        Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
    }
}
