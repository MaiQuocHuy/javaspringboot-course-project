package project.ktc.springboot_app.filter_rule.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.dto.CourseResponseDto;
import project.ktc.springboot_app.filter_rule.enums.EffectiveFilter;
import project.ktc.springboot_app.filter_rule.security.EffectiveFilterContext;
import project.ktc.springboot_app.filter_rule.services.AuthorizationService;
import project.ktc.springboot_app.filter_rule.services.CourseAccessService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Example controller demonstrating comprehensive RBAC usage patterns
 */
@RestController
@RequestMapping("/api/rbac-demo")
@RequiredArgsConstructor
@Slf4j
public class RBACDemoController {

        private final CourseAccessService courseAccessService;
        private final AuthorizationService authorizationService;

        /**
         * Helper method to convert Course entity to CourseResponseDto to avoid circular
         * references
         */
        private CourseResponseDto mapToDto(Course course) {
                if (course == null) {
                        return null;
                }

                return CourseResponseDto.builder()
                                .id(course.getId())
                                .title(course.getTitle())
                                .description(course.getDescription())
                                .price(course.getPrice())
                                .level(course.getLevel())
                                .thumbnailUrl(course.getThumbnailUrl())
                                .thumbnailId(course.getThumbnailId())
                                .isPublished(course.getIsPublished())
                                .isApproved(course.getIsApproved())
                                .instructor(course.getInstructor() != null ? CourseResponseDto.InstructorInfo.builder()
                                                .id(course.getInstructor().getId())
                                                .name(course.getInstructor().getName())
                                                .email(course.getInstructor().getEmail())
                                                .build() : null)
                                .categories(course.getCategories() != null ? course.getCategories().stream()
                                                .map(cat -> CourseResponseDto.CategoryInfo.builder()
                                                                .id(cat.getId())
                                                                .name(cat.getName())
                                                                .build())
                                                .collect(Collectors.toList()) : null)
                                .createdAt(course.getCreatedAt())
                                .updatedAt(course.getUpdatedAt())
                                .build();
        }

        /**
         * Helper method to convert Page<Course> to Page<CourseResponseDto>
         */
        private Page<CourseResponseDto> mapToDto(Page<Course> coursePage) {
                return coursePage.map(this::mapToDto);
        }

        /**
         * Helper method to convert List<Course> to List<CourseResponseDto>
         */
        private List<CourseResponseDto> mapToDto(List<Course> courses) {
                return courses.stream()
                                .map(this::mapToDto)
                                .collect(Collectors.toList());
        }

        /**
         * Example 1: Basic permission check with effective filter
         */
        @GetMapping("/courses")
        @PreAuthorize("hasPermission(null, 'Course', 'course:READ')")
        public ResponseEntity<Page<CourseResponseDto>> getCourses(Pageable pageable, Authentication auth) {
                User user = (User) auth.getPrincipal();

                log.info("User {} accessing courses with effective filter: {}",
                                user.getEmail(), EffectiveFilterContext.getCurrentFilter());

                Page<Course> courses = courseAccessService.getCoursesWithFilter(user, pageable);
                Page<CourseResponseDto> courseDtos = mapToDto(courses);
                return ResponseEntity.ok(courseDtos);
        }

        /**
         * Example 2: Specific course access with ID-based permission
         */
        @GetMapping("/courses/{courseId}")
        @PreAuthorize("hasPermission(#courseId, 'Course', 'course:READ')")
        public ResponseEntity<CourseResponseDto> getCourse(@PathVariable String courseId) {
                log.info("Accessing course {} with filter: {}",
                                courseId, EffectiveFilterContext.getCurrentFilter());

                return courseAccessService.getCourseById(courseId)
                                .map(this::mapToDto)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        /**
         * Example 3: Update course with permission check
         */
        @PutMapping("/courses/{courseId}")
        @PreAuthorize("hasPermission(#courseId, 'Course', 'course:UPDATE')")
        public ResponseEntity<CourseResponseDto> updateCourse(@PathVariable String courseId,
                        @RequestBody Course course) {
                log.info("Updating course {} with filter: {}",
                                courseId, EffectiveFilterContext.getCurrentFilter());

                Course updated = courseAccessService.updateCourse(courseId, course);
                CourseResponseDto dto = mapToDto(updated);
                return ResponseEntity.ok(dto);
        }

        /**
         * Example 4: Create new course
         */
        @PostMapping("/courses")
        @PreAuthorize("hasPermission(null, 'Course', 'course:CREATE')")
        public ResponseEntity<CourseResponseDto> createCourse(@RequestBody Course course, Authentication auth) {
                User user = (User) auth.getPrincipal();

                log.info("User {} creating course with filter: {}",
                                user.getEmail(), EffectiveFilterContext.getCurrentFilter());

                // Set instructor to current user for OWN filter compliance
                course.setInstructor(user);

                Course created = courseAccessService.updateCourse(null, course);
                CourseResponseDto dto = mapToDto(created);
                return ResponseEntity.ok(dto);
        }

        /**
         * Example 5: Delete course
         */
        @DeleteMapping("/courses/{courseId}")
        @PreAuthorize("hasPermission(#courseId, 'Course', 'course:DELETE')")
        public ResponseEntity<Void> deleteCourse(@PathVariable String courseId) {
                log.info("Deleting course {} with filter: {}",
                                courseId, EffectiveFilterContext.getCurrentFilter());

                // Implementation would go here
                return ResponseEntity.noContent().build();
        }

        /**
         * Example 6: Get user's effective permissions (for debugging/admin)
         */
        @GetMapping("/permissions")
        public ResponseEntity<Map<String, EffectiveFilter>> getUserPermissions(Authentication auth) {
                User user = (User) auth.getPrincipal();

                Map<String, EffectiveFilter> permissions = Map.of(
                                "course:READ", authorizationService.getEffectiveFilter(user, "course:READ"),
                                "course:CREATE", authorizationService.getEffectiveFilter(user, "course:CREATE"),
                                "course:UPDATE", authorizationService.getEffectiveFilter(user, "course:UPDATE"),
                                "course:DELETE", authorizationService.getEffectiveFilter(user, "course:DELETE"));

                log.info("User {} permissions: {}", user.getEmail(), permissions);

                return ResponseEntity.ok(permissions);
        }

        /**
         * Example 7: Courses filtered by category with RBAC
         */
        @GetMapping("/courses/category/{categoryId}")
        @PreAuthorize("hasPermission(null, 'Course', 'course:READ')")
        public ResponseEntity<Page<CourseResponseDto>> getCoursesByCategory(@PathVariable String categoryId,
                        Pageable pageable,
                        Authentication auth) {
                User user = (User) auth.getPrincipal();

                log.info("User {} accessing courses in category {} with filter: {}",
                                user.getEmail(), categoryId, EffectiveFilterContext.getCurrentFilter());

                Page<Course> courses = courseAccessService.getCoursesWithBusinessFilter(user, categoryId, pageable);
                Page<CourseResponseDto> courseDtos = mapToDto(courses);
                return ResponseEntity.ok(courseDtos);
        }

        /**
         * Example 8: Check permission without data access (dry run)
         */
        @GetMapping("/permissions/check")
        public ResponseEntity<Map<String, Object>> checkPermission(@RequestParam String permission,
                        Authentication auth) {
                User user = (User) auth.getPrincipal();

                AuthorizationService.AuthorizationResult result = authorizationService.evaluatePermission(user,
                                permission);

                Map<String, Object> response = Map.of(
                                "hasPermission", result.isHasPermission(),
                                "effectiveFilter", result.getEffectiveFilter(),
                                "permissionKey", permission,
                                "user", user.getEmail());

                return ResponseEntity.ok(response);
        }

        /**
         * Example 9: Admin endpoint - requires specific role
         */
        @GetMapping("/admin/all-courses")
        @PreAuthorize("hasRole('ADMIN') and hasPermission(null, 'Course', 'course:READ')")
        public ResponseEntity<List<CourseResponseDto>> getAllCoursesAdmin(Authentication auth) {
                User user = (User) auth.getPrincipal();

                log.info("Admin {} accessing all courses with filter: {}",
                                user.getEmail(), EffectiveFilterContext.getCurrentFilter());

                // Admin should have ALL filter, so this returns everything
                List<Course> courses = courseAccessService.getAllAccessibleCourses(user);
                List<CourseResponseDto> courseDtos = mapToDto(courses);
                return ResponseEntity.ok(courseDtos);
        }

        /**
         * Example 10: Instructor endpoint - access own courses
         */
        @GetMapping("/instructor/my-courses")
        @PreAuthorize("hasRole('INSTRUCTOR') and hasPermission(null, 'Course', 'course:READ')")
        public ResponseEntity<List<CourseResponseDto>> getMyCoursesInstructor(Authentication auth) {
                User user = (User) auth.getPrincipal();

                log.info("Instructor {} accessing own courses with filter: {}",
                                user.getEmail(), EffectiveFilterContext.getCurrentFilter());

                // Instructor should have OWN filter, so this returns only their courses
                List<Course> courses = courseAccessService.getAllAccessibleCourses(user);
                List<CourseResponseDto> courseDtos = mapToDto(courses);
                return ResponseEntity.ok(courseDtos);
        }
}
