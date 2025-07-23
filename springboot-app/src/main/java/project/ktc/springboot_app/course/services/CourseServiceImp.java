package project.ktc.springboot_app.course.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.exception.ResourceNotFoundException;
import project.ktc.springboot_app.course.dto.CourseDetailResponseDto;
import project.ktc.springboot_app.course.dto.CoursePublicResponseDto;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.enums.CourseLevel;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.entity.Category;
import project.ktc.springboot_app.entity.Lesson;
import project.ktc.springboot_app.entity.Section;
import project.ktc.springboot_app.entity.VideoContent;
import project.ktc.springboot_app.video.repository.VideoContentRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseServiceImp {
    private final CourseRepository courseRepository;
    private final VideoContentRepository videoContentRepository;

    public PaginatedResponse<CoursePublicResponseDto> findAllPublic(
            String search,
            String categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            CourseLevel level,
            Pageable pageable) {

        log.info(
                "Finding public courses with filters: search={}, categoryId={}, minPrice={}, maxPrice={}, level={}, page={}",
                search, categoryId, minPrice, maxPrice, level, pageable.getPageNumber());

        // Validate price range
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }

        Page<Course> coursePage = courseRepository.findPublishedCoursesWithFilters(
                search, categoryId, minPrice, maxPrice, level, pageable);

        List<CoursePublicResponseDto> courseResponses = coursePage.getContent().stream()
                .map(this::mapToCoursePublicResponse)
                .collect(Collectors.toList());

        PaginatedResponse.PageInfo pageInfo = PaginatedResponse.PageInfo.builder()
                .number(coursePage.getNumber())
                .size(coursePage.getSize())
                .totalPages(coursePage.getTotalPages())
                .totalElements(coursePage.getTotalElements())
                .first(coursePage.isFirst())
                .last(coursePage.isLast())
                .build();

        return PaginatedResponse.<CoursePublicResponseDto>builder()
                .content(courseResponses)
                .page(pageInfo)
                .build();
    }

    public CourseDetailResponseDto findOnePublic(String courseId) {
        log.info("Finding course details for course ID: {}", courseId);

        // Step 1: Find the course with instructor only (avoid multiple bags)
        Course course = courseRepository.findPublishedCourseByIdWithDetails(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        // Step 2: Fetch categories separately to avoid MultipleBagFetchException
        Optional<Course> courseWithCategories = courseRepository.findCourseWithCategories(courseId);
        if (courseWithCategories.isPresent()) {
            course.setCategories(courseWithCategories.get().getCategories());
        }

        // Step 3: Fetch sections with lessons separately
        List<Section> sectionsWithLessons = courseRepository.findSectionsWithLessonsByCourseId(courseId);
        course.setSections(sectionsWithLessons);

        // Get rating information
        CourseDetailResponseDto.RatingSummary ratingSummary = getRatingSummary(courseId);

        // Get lesson count
        Long lessonCount = courseRepository.countLessonsByCourseId(courseId);

        // Check if current user is enrolled
        Boolean isEnrolled = getCurrentUserEnrollmentStatus(courseId);

        // Get sample video URL (first video lesson if available)
        String sampleVideoUrl = getSampleVideoUrl(course);

        // Generate slug from title
        String slug = generateSlug(course.getTitle());

        return mapToCourseDetailResponse(course, ratingSummary, lessonCount.intValue(),
                isEnrolled, sampleVideoUrl, slug);
    }

    private CourseDetailResponseDto.RatingSummary getRatingSummary(String courseId) {
        Double averageRating = courseRepository.findAverageRatingByCourseId(courseId).orElse(0.0);
        Long totalReviews = courseRepository.countReviewsByCourseId(courseId);

        return CourseDetailResponseDto.RatingSummary.builder()
                .average(averageRating)
                .totalReviews(totalReviews)
                .build();
    }

    private Boolean getCurrentUserEnrollmentStatus(String courseId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {

                User currentUser = (User) authentication.getPrincipal();
                return courseRepository.isUserEnrolledInCourse(courseId, currentUser.getId());
            }
        } catch (Exception e) {
            log.warn("Could not determine enrollment status: {}", e.getMessage());
        }
        return false;
    }

    private String getSampleVideoUrl(Course course) {
        if (course.getSections() != null && !course.getSections().isEmpty()) {
            for (Section section : course.getSections()) {
                if (section.getLessons() != null && !section.getLessons().isEmpty()) {
                    for (Lesson lesson : section.getLessons()) {
                        if ("VIDEO".equals(lesson.getType()) && lesson.getContentId() != null) {
                            // Get the actual video URL from VideoContent entity
                            Optional<VideoContent> videoContent = videoContentRepository
                                    .findById(lesson.getContentId());
                            if (videoContent.isPresent()) {
                                return videoContent.get().getUrl();
                            } else {
                                log.warn("VideoContent not found for contentId: {}", lesson.getContentId());
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private String generateSlug(String title) {
        if (title == null)
            return "";
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private CourseDetailResponseDto mapToCourseDetailResponse(Course course,
            CourseDetailResponseDto.RatingSummary ratingSummary,
            Integer lessonCount,
            Boolean isEnrolled,
            String sampleVideoUrl,
            String slug) {

        // Map instructor
        CourseDetailResponseDto.InstructorSummary instructorSummary = null;
        if (course.getInstructor() != null) {
            instructorSummary = CourseDetailResponseDto.InstructorSummary.builder()
                    .id(course.getInstructor().getId())
                    .name(course.getInstructor().getName())
                    .build();
        }

        // Map sections and lessons
        List<CourseDetailResponseDto.SectionSummary> sectionSummaries = null;
        if (course.getSections() != null) {
            sectionSummaries = course.getSections().stream()
                    .sorted((s1, s2) -> Integer.compare(
                            s1.getOrderIndex() != null ? s1.getOrderIndex() : 0,
                            s2.getOrderIndex() != null ? s2.getOrderIndex() : 0))
                    .map(this::mapToSectionSummary)
                    .collect(Collectors.toList());
        }

        return CourseDetailResponseDto.builder()
                .id(course.getId())
                .slug(slug)
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .level(course.getLevel())
                .thumbnailUrl(course.getThumbnailUrl())
                .lessonCount(lessonCount)
                .sampleVideoUrl(sampleVideoUrl)
                .rating(ratingSummary)
                .isEnrolled(isEnrolled)
                .instructor(instructorSummary)
                .sections(sectionSummaries)
                .build();
    }

    private CourseDetailResponseDto.SectionSummary mapToSectionSummary(Section section) {
        List<CourseDetailResponseDto.LessonSummary> lessonSummaries = null;
        if (section.getLessons() != null) {
            lessonSummaries = section.getLessons().stream()
                    .sorted((l1, l2) -> Integer.compare(
                            l1.getOrderIndex() != null ? l1.getOrderIndex() : 0,
                            l2.getOrderIndex() != null ? l2.getOrderIndex() : 0))
                    .map(lesson -> CourseDetailResponseDto.LessonSummary.builder()
                            .id(lesson.getId())
                            .title(lesson.getTitle())
                            .type(lesson.getType())
                            .build())
                    .collect(Collectors.toList());
        }

        return CourseDetailResponseDto.SectionSummary.builder()
                .id(section.getId())
                .title(section.getTitle())
                .lessons(lessonSummaries)
                .build();
    }

    private CoursePublicResponseDto mapToCoursePublicResponse(Course course) {
        // Get primary category (first one if multiple)
        CoursePublicResponseDto.CategorySummary categorySum = null;
        if (course.getCategories() != null && !course.getCategories().isEmpty()) {
            Category primaryCategory = course.getCategories().get(0);
            categorySum = CoursePublicResponseDto.CategorySummary.builder()
                    .id(primaryCategory.getId())
                    .name(primaryCategory.getName())
                    .build();
        }

        // Get instructor info
        CoursePublicResponseDto.InstructorSummary instructorSum = null;
        if (course.getInstructor() != null) {
            instructorSum = CoursePublicResponseDto.InstructorSummary.builder()
                    .id(course.getInstructor().getId())
                    .name(course.getInstructor().getName())
                    .avatar(course.getInstructor().getThumbnailUrl()) // Assuming thumbnail_url is avatar
                    .build();
        }

        return CoursePublicResponseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .level(course.getLevel())
                .thumbnailUrl(course.getThumbnailUrl())
                .category(categorySum)
                .instructor(instructorSum)
                .build();
    }
}
