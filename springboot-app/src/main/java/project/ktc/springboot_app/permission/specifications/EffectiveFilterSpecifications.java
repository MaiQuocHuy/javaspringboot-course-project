package project.ktc.springboot_app.permission.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.permission.entity.FilterType;
import project.ktc.springboot_app.security.CustomPermissionEvaluator;

/**
 * JPA Specifications for applying effective filters dynamically Uses the new
 * filter type system
 * with FilterType entity
 */
@Slf4j
public class EffectiveFilterSpecifications {

	/**
   * Apply effective filter to Course entities using new filter system
   *
   * @return Specification that applies the current effective filter
   */
  public static Specification<Course> applyCourseFilter() {
    return (Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
      FilterType.EffectiveFilterType filter =
          CustomPermissionEvaluator.EffectiveFilterContext.getCurrentFilter();
      User currentUser = CustomPermissionEvaluator.EffectiveFilterContext.getCurrentUser();

      if (filter == null) {
        log.warn("No effective filter found in context, denying access");
        return cb.disjunction(); // Always false - deny access
      }

      log.debug(
          "Applying filter: {} for user: {}",
          filter,
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

        case DENIED -> {
          log.debug("Access denied - returning false predicate");
          yield cb.disjunction(); // Always false - deny access
        }
      };
    };
  }

	/**
	 * Apply effective filter with additional business logic constraints Combines
	 * effective filter
	 * with published/approved status
	 *
	 * @param includePublishedOnly
	 *            whether to include only published courses
	 * @return Specification that applies the effective filter with business
	 *         constraints
	 */
	public static Specification<Course> applyCourseFilterWithConstraints(
			boolean includePublishedOnly) {
		return (Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			// Apply effective filter
			Specification<Course> effectiveFilterSpec = applyCourseFilter();
			Predicate effectiveFilterPredicate = effectiveFilterSpec.toPredicate(root, query, cb);
			if (effectiveFilterPredicate != null) {
				predicates.add(effectiveFilterPredicate);
			}

			// Add business constraints
			if (includePublishedOnly) {
				predicates.add(cb.isTrue(root.get("isPublished")));
				predicates.add(cb.isTrue(root.get("isApproved")));
			}

			// Always exclude deleted courses
			predicates.add(cb.isFalse(root.get("isDeleted")));

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	/**
   * Generic method to apply effective filter to any entity with instructor relationship
   *
   * @param <T> the entity type
   * @return Specification that applies the current effective filter
   */
  public static <T> Specification<T> applyInstructorBasedFilter() {
    return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
      FilterType.EffectiveFilterType filter =
          CustomPermissionEvaluator.EffectiveFilterContext.getCurrentFilter();
      User currentUser = CustomPermissionEvaluator.EffectiveFilterContext.getCurrentUser();

      if (filter == null) {
        log.warn("No effective filter found in context, denying access");
        return cb.disjunction();
      }

      return switch (filter) {
        case ALL -> cb.conjunction();

        case OWN -> {
          if (currentUser == null) {
            log.warn("OWN filter requires user context but none found");
            yield cb.disjunction();
          }
          // Check if entity has instructor field
          try {
            yield cb.equal(root.get("instructor").get("id"), currentUser.getId());
          } catch (IllegalArgumentException e) {
            // If no instructor field, try createdBy
            try {
              yield cb.equal(root.get("createdBy").get("id"), currentUser.getId());
            } catch (IllegalArgumentException e2) {
              log.warn(
                  "Entity {} has neither instructor nor createdBy field for OWN filter",
                  root.getJavaType().getSimpleName());
              yield cb.disjunction();
            }
          }
        }

        case DENIED -> cb.disjunction();
      };
    };
  }

	/**
   * Generic method to apply effective filter to any entity with createdBy relationship
   *
   * @param <T> the entity type
   * @return Specification that applies the current effective filter
   */
  public static <T> Specification<T> applyCreatedByBasedFilter() {
    return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
      FilterType.EffectiveFilterType filter =
          CustomPermissionEvaluator.EffectiveFilterContext.getCurrentFilter();
      User currentUser = CustomPermissionEvaluator.EffectiveFilterContext.getCurrentUser();

      if (filter == null) {
        log.warn("No effective filter found in context, denying access");
        return cb.disjunction();
      }

      return switch (filter) {
        case ALL -> cb.conjunction();

        case OWN -> {
          if (currentUser == null) {
            log.warn("OWN filter requires user context but none found");
            yield cb.disjunction();
          }
          // Access only to user's own created entities
          yield cb.equal(root.get("createdBy").get("id"), currentUser.getId());
        }

        case DENIED -> cb.disjunction();
      };
    };
  }

	/**
   * Apply filter with custom ownership logic
   *
   * @param <T> the entity type
   * @param ownershipPredicate function to determine ownership
   * @return Specification that applies the effective filter with custom ownership
   */
  public static <T> Specification<T> applyCustomOwnershipFilter(
      OwnershipPredicate<T> ownershipPredicate) {
    return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
      FilterType.EffectiveFilterType filter =
          CustomPermissionEvaluator.EffectiveFilterContext.getCurrentFilter();
      User currentUser = CustomPermissionEvaluator.EffectiveFilterContext.getCurrentUser();

      if (filter == null) {
        log.warn("No effective filter found in context, denying access");
        return cb.disjunction();
      }

      return switch (filter) {
        case ALL -> cb.conjunction();

        case OWN -> {
          if (currentUser == null) {
            log.warn("OWN filter requires user context but none found");
            yield cb.disjunction();
          }
          // Apply custom ownership logic
          yield ownershipPredicate.apply(root, query, cb, currentUser);
        }

        case DENIED -> cb.disjunction();
      };
    };
  }

	/** Functional interface for custom ownership logic */
	@FunctionalInterface
	public interface OwnershipPredicate<T> {
		Predicate apply(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb, User user);
	}
}
