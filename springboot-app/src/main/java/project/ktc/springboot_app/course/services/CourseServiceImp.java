package project.ktc.springboot_app.course.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.exception.ResourceNotFoundException;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.dto.CourseAdminResponseDto;
import project.ktc.springboot_app.course.dto.CourseApprovalResponseDto;
import project.ktc.springboot_app.course.dto.CourseDetailResponseDto;
import project.ktc.springboot_app.course.dto.CoursePublicResponseDto;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.enums.CourseLevel;
import project.ktc.springboot_app.course.interfaces.CourseService;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;
import project.ktc.springboot_app.entity.QuizQuestion;
import project.ktc.springboot_app.lesson.entity.Lesson;
import project.ktc.springboot_app.quiz.repositories.QuizQuestionRepository;
import project.ktc.springboot_app.section.dto.LessonDto;
import project.ktc.springboot_app.section.dto.QuizDto;
import project.ktc.springboot_app.section.dto.QuizQuestionDto;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;
import project.ktc.springboot_app.section.dto.VideoDto;
import project.ktc.springboot_app.section.entity.Section;
import project.ktc.springboot_app.section.repositories.InstructorSectionRepository;
import project.ktc.springboot_app.utils.SecurityUtil;
import project.ktc.springboot_app.utils.StringUtil;
import project.ktc.springboot_app.utils.MathUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseServiceImp implements CourseService {
        private final CourseRepository courseRepository;
        private final InstructorSectionRepository sectionRepository;
        private final QuizQuestionRepository quizQuestionRepository;
        private final EnrollmentRepository enrollmentRepository;

        @Override
        public ResponseEntity<ApiResponse<PaginatedResponse<CoursePublicResponseDto>>> findAllPublic(
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
                log.info("Found {} public courses", coursePage);
                // Load categories separately for each course to avoid N+1 problem
                List<Course> coursesWithCategories = coursePage.getContent().stream()
                                .map(course -> {
                                        Optional<Course> courseWithCats = courseRepository
                                                        .findCourseWithCategories(course.getId());
                                        log.info("Found categories for course {}: {}", course.getId(),
                                                        courseWithCats.get().getCategories());
                                        if (courseWithCats.isPresent()) {
                                                course.setCategories(courseWithCats.get().getCategories());
                                        }
                                        return course;
                                })
                                .collect(Collectors.toList());
                // Get enrollment counts for all courses in this page
                List<String> courseIds = coursesWithCategories.stream()
                                .map(Course::getId)
                                .collect(Collectors.toList());

                final Map<String, Long> enrollmentCounts;
                if (!courseIds.isEmpty()) {
                        List<Object[]> enrollmentData = courseRepository.findEnrollmentCountsByCourseIds(courseIds);
                        enrollmentCounts = enrollmentData.stream()
                                        .collect(Collectors.toMap(
                                                        data -> (String) data[0], // courseId
                                                        data -> (Long) data[1] // enrollmentCount
                                        ));
                } else {
                        enrollmentCounts = new HashMap<>();
                }

                // Get current user ID for enrollment check
                String currentUserId = SecurityUtil.getCurrentUserId();

                // Get enrollment status for current user if logged in
                final Map<String, Boolean> enrollmentStatus;
                if (currentUserId != null && !courseIds.isEmpty()) {
                        enrollmentStatus = courseIds.stream()
                                        .collect(Collectors.toMap(
                                                        courseId -> courseId,
                                                        courseId -> enrollmentRepository.existsByUserIdAndCourseId(
                                                                        currentUserId, courseId)));
                } else {
                        enrollmentStatus = new HashMap<>();
                }

                List<CoursePublicResponseDto> courseResponses = coursesWithCategories.stream()
                                .map(course -> mapToCoursePublicResponse(course,
                                                enrollmentCounts.getOrDefault(course.getId(), 0L),
                                                enrollmentStatus.getOrDefault(course.getId(), false)))
                                .collect(Collectors.toList());

                // Create paginated response
                PaginatedResponse<CoursePublicResponseDto> paginatedResponse = PaginatedResponse
                                .<CoursePublicResponseDto>builder()
                                .content(courseResponses)
                                .page(PaginatedResponse.PageInfo.builder()
                                                .number(coursePage.getNumber())
                                                .size(coursePage.getSize())
                                                .totalPages(coursePage.getTotalPages())
                                                .totalElements(coursePage.getTotalElements())
                                                .first(coursePage.isFirst())
                                                .last(coursePage.isLast())
                                                .build())
                                .build();

                return ApiResponseUtil.success(paginatedResponse, "Public courses retrieved successfully");
        }

        @Override
        public ResponseEntity<ApiResponse<CourseDetailResponseDto>> findOnePublic(String courseId) {
                log.info("Finding course details for course ID: {}", courseId);

                // Step 1: Find the course with instructor only (avoid multiple bags)
                Course course = courseRepository.findPublishedCourseByIdWithDetails(courseId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Course not found with ID: " + courseId));

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
                Long quizCount = courseRepository.countQuizLessonsByCourseId(courseId);
                Long questionCount = courseRepository.countQuizQuestionsByCourseId(courseId);

                Long enrollMentCount = courseRepository.countUserEnrolledInCourse(courseId);

                // Check if current user is enrolled
                Boolean isEnrolled = getCurrentUserEnrollmentStatus(courseId);

                // Get sample video URL (first video lesson if available)
                String sampleVideoUrl = getSampleVideoUrl(course);

                // Generate slug from title
                String slug = StringUtil.generateSlug(course.getTitle());

                // Map to DTO
                CourseDetailResponseDto responseDto = mapToCourseDetailResponse(
                                course, ratingSummary, lessonCount.intValue(), quizCount.intValue(),
                                questionCount.intValue(),
                                isEnrolled, sampleVideoUrl, slug, enrollMentCount.intValue());

                return ApiResponseUtil.success(responseDto, "Course details retrieved successfully");
        }

        @Override
        public ResponseEntity<ApiResponse<CourseDetailResponseDto>> findOneBySlug(String slug) {
                log.info("Finding course details for slug: {}", slug);

                // Step 1: Find the course with instructor by slug
                Course course = courseRepository.findPublishedCourseBySlugWithDetails(slug)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Course not found with slug: " + slug));

                // Step 2: Fetch categories separately to avoid MultipleBagFetchException
                Optional<Course> courseWithCategories = courseRepository.findCourseWithCategories(course.getId());
                if (courseWithCategories.isPresent()) {
                        course.setCategories(courseWithCategories.get().getCategories());
                }

                // Step 3: Fetch sections with lessons separately
                List<Section> sectionsWithLessons = courseRepository.findSectionsWithLessonsByCourseId(course.getId());
                course.setSections(sectionsWithLessons);

                // Get rating information
                CourseDetailResponseDto.RatingSummary ratingSummary = getRatingSummary(course.getId());

                // Get lesson and quiz counts
                Long lessonCount = courseRepository.countLessonsByCourseId(course.getId());
                Long quizCount = courseRepository.countQuizLessonsByCourseId(course.getId());
                Long questionCount = courseRepository.countQuizQuestionsByCourseId(course.getId());

                Long enrollMentCount = courseRepository.countUserEnrolledInCourse(course.getId());

                // Check if current user is enrolled
                Boolean isEnrolled = getCurrentUserEnrollmentStatus(course.getId());

                // Get sample video URL (first video lesson if available)
                String sampleVideoUrl = getSampleVideoUrl(course);

                // Map to DTO with quiz count
                CourseDetailResponseDto responseDto = mapToCourseDetailResponse(
                                course, ratingSummary, lessonCount.intValue(), quizCount.intValue(),
                                questionCount.intValue(),
                                isEnrolled, sampleVideoUrl, slug, enrollMentCount.intValue());

                return ApiResponseUtil.success(responseDto, "Course details retrieved successfully");
        }

        @Override
        public ResponseEntity<ApiResponse<PaginatedResponse<CourseAdminResponseDto>>> findCoursesForAdmin(
                        Boolean isApproved,
                        String categoryId,
                        String search,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        CourseLevel level,
                        Pageable pageable) {

                log.info(
                                "Finding courses for admin with filters: isApproved={}, categoryId={}, search={}, minPrice={}, maxPrice={}, level={}, page={}",
                                isApproved, categoryId, search, minPrice, maxPrice, level, pageable.getPageNumber());

                // Validate price range
                if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
                        throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
                }

                Page<Course> coursePage = courseRepository.findCoursesForAdmin(
                                isApproved, categoryId, search, minPrice, maxPrice, level, pageable);

                // Load categories separately for each course to avoid N+1 problem
                List<Course> coursesWithCategories = coursePage.getContent().stream()
                                .map(course -> {
                                        Optional<Course> courseWithCats = courseRepository
                                                        .findCourseWithCategories(course.getId());
                                        if (courseWithCats.isPresent()) {
                                                course.setCategories(courseWithCats.get().getCategories());
                                        }
                                        return course;
                                })
                                .collect(Collectors.toList());

                // Get enrollment counts for all courses in this page
                List<String> courseIds = coursesWithCategories.stream()
                                .map(Course::getId)
                                .collect(Collectors.toList());

                final Map<String, Long> enrollmentCounts;
                if (!courseIds.isEmpty()) {
                        List<Object[]> enrollmentData = courseRepository.findEnrollmentCountsByCourseIds(courseIds);
                        enrollmentCounts = enrollmentData.stream()
                                        .collect(Collectors.toMap(
                                                        data -> (String) data[0], // courseId
                                                        data -> (Long) data[1] // enrollmentCount
                                        ));
                } else {
                        enrollmentCounts = new HashMap<>();
                }

                List<CourseAdminResponseDto> courseResponses = coursesWithCategories.stream()
                                .map(course -> mapToCourseAdminResponse(course,
                                                enrollmentCounts.getOrDefault(course.getId(), 0L)))
                                .collect(Collectors.toList());

                // Create paginated response
                PaginatedResponse<CourseAdminResponseDto> paginatedResponse = PaginatedResponse
                                .<CourseAdminResponseDto>builder()
                                .content(courseResponses)
                                .page(PaginatedResponse.PageInfo.builder()
                                                .number(coursePage.getNumber())
                                                .size(coursePage.getSize())
                                                .totalPages(coursePage.getTotalPages())
                                                .totalElements(coursePage.getTotalElements())
                                                .first(coursePage.isFirst())
                                                .last(coursePage.isLast())
                                                .build())
                                .build();

                return ApiResponseUtil.success(paginatedResponse, "Courses retrieved successfully for admin");
        }

        private CourseDetailResponseDto.RatingSummary getRatingSummary(String courseId) {
                Double averageRating = courseRepository.findAverageRatingByCourseId(courseId).orElse(0.0);
                Long totalReviews = courseRepository.countReviewsByCourseId(courseId);

                return CourseDetailResponseDto.RatingSummary.builder()
                                .average(MathUtil.roundToTwoDecimals(averageRating)) // Consistent rounding
                                .totalReviews(totalReviews)
                                .build();
        }

        /**
         * Get instructor overview statistics including average rating and total courses
         * count
         * Both queries now use consistent filtering for published, approved, and
         * non-deleted courses
         * 
         * @param instructorId the instructor ID
         * @return OverViewInstructorSummary containing average rating and total courses
         */
        private CourseDetailResponseDto.OverViewInstructorSummary getOverViewInstructorSummary(String instructorId) {
                if (instructorId == null || instructorId.trim().isEmpty()) {
                        return null;
                }

                try {
                        Double averageRating = courseRepository.findAverageRatingByInstructorId(instructorId)
                                        .orElse(0.0);
                        Long totalCourses = courseRepository.countCoursesByInstructorId(instructorId);

                        return CourseDetailResponseDto.OverViewInstructorSummary.builder()
                                        .average(MathUtil.roundToTwoDecimals(averageRating)) // Consistent rounding
                                                                                             // using utility
                                        .totalCoursesByInstructor(totalCourses != null ? totalCourses : 0L)
                                        .build();
                } catch (Exception e) {
                        log.warn("Error getting instructor overview summary for instructorId: {}", instructorId, e);
                        return CourseDetailResponseDto.OverViewInstructorSummary.builder()
                                        .average(0.0)
                                        .totalCoursesByInstructor(0L)
                                        .build();
                }
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
                                                if (lesson.getLessonType() != null
                                                                && "VIDEO".equals(lesson.getLessonType().getName())
                                                                && lesson.getContent() != null) {
                                                        // Get the actual video URL from VideoContent entity
                                                        return lesson.getContent().getUrl();
                                                }
                                        }
                                }
                        }
                }
                return null;
        }

        private CourseDetailResponseDto mapToCourseDetailResponse(Course course,
                        CourseDetailResponseDto.RatingSummary ratingSummary,
                        Integer lessonCount,
                        Integer quizCount,
                        Integer questionCount,
                        Boolean isEnrolled,
                        String sampleVideoUrl,
                        String slug,
                        Integer enrollCount) {

                // Map instructor
                CourseDetailResponseDto.InstructorSummary instructorSummary = null;
                CourseDetailResponseDto.OverViewInstructorSummary overViewInstructorSummary = null;
                if (course.getInstructor() != null) {
                        instructorSummary = CourseDetailResponseDto.InstructorSummary.builder()
                                        .id(course.getInstructor().getId())
                                        .name(course.getInstructor().getName())
                                        .bio(course.getInstructor().getBio())
                                        .thumbnailUrl(course.getInstructor().getThumbnailUrl())
                                        .build();

                        // Get instructor overview statistics
                        overViewInstructorSummary = getOverViewInstructorSummary(course.getInstructor().getId());
                }

                // Map sections and lessons
                List<CourseDetailResponseDto.SectionSummary> sectionSummaries = null;
                Integer totalDuration = null;

                if (course.getSections() != null) {
                        sectionSummaries = course.getSections().stream()
                                        .sorted((s1, s2) -> Integer.compare(
                                                        s1.getOrderIndex() != null ? s1.getOrderIndex() : 0,
                                                        s2.getOrderIndex() != null ? s2.getOrderIndex() : 0))
                                        .map(this::mapToSectionSummary)
                                        .collect(Collectors.toList());

                        // Calculate total course duration (sum of all section durations)
                        totalDuration = sectionSummaries.stream()
                                        .filter(section -> section.getDuration() != null)
                                        .mapToInt(CourseDetailResponseDto.SectionSummary::getDuration)
                                        .sum();

                        if (totalDuration == 0) {
                                totalDuration = null; // Don't show 0 duration
                        }
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
                                .quizCount(quizCount)
                                .questionCount(questionCount)
                                .enrollCount(enrollCount)
                                .sampleVideoUrl(sampleVideoUrl)
                                .totalDuration(totalDuration)
                                .rating(ratingSummary)
                                .isEnrolled(isEnrolled)
                                .instructor(instructorSummary)
                                .overViewInstructorSummary(overViewInstructorSummary)
                                .sections(sectionSummaries)
                                .build();
        }

        private CourseDetailResponseDto.SectionSummary mapToSectionSummary(Section section) {
                List<CourseDetailResponseDto.LessonSummary> lessonSummaries = null;
                int sectionDuration = 0; // Calculate total section duration
                int sectionLessonCount = 0;
                int sectionQuizCount = 0;

                if (section.getLessons() != null) {
                        sectionLessonCount = section.getLessons().size();

                        // Count quiz lessons in this section based on business rules:
                        // - If lesson has type-002 (QUIZ) and content_id is null: count questions from
                        // quiz_questions table
                        // - Otherwise: count quiz lessons normally
                        for (Lesson lesson : section.getLessons()) {
                                if (lesson.getLessonType() != null && "QUIZ".equals(lesson.getLessonType().getName())) {
                                        // Check if this is type-002 with null content_id
                                        if (lesson.getContent() == null) {
                                                // Count questions from quiz_questions table for this lesson
                                                Long questionCount = courseRepository
                                                                .countQuizQuestionsBySectionId(section.getId());
                                                sectionQuizCount += questionCount != null ? questionCount.intValue()
                                                                : 0;
                                        } else {
                                                // Regular quiz lesson with content_id
                                                sectionQuizCount++;
                                        }
                                }
                        }

                        lessonSummaries = section.getLessons().stream()
                                        .sorted((l1, l2) -> Integer.compare(
                                                        l1.getOrderIndex() != null ? l1.getOrderIndex() : 0,
                                                        l2.getOrderIndex() != null ? l2.getOrderIndex() : 0))
                                        .map(lesson -> {
                                                Integer lessonDuration = null;
                                                // Only get duration for VIDEO type lessons
                                                if (lesson.getLessonType() != null &&
                                                                "VIDEO".equals(lesson.getLessonType().getName()) &&
                                                                lesson.getContent() != null) {
                                                        lessonDuration = lesson.getContent().getDuration();
                                                }

                                                return CourseDetailResponseDto.LessonSummary.builder()
                                                                .id(lesson.getId())
                                                                .title(lesson.getTitle())
                                                                .type(lesson.getLessonType() != null
                                                                                ? lesson.getLessonType().getName()
                                                                                : "UNKNOWN")
                                                                .duration(lessonDuration)
                                                                .build();
                                        })
                                        .collect(Collectors.toList());

                        // Calculate section duration (sum of all VIDEO lesson durations)
                        sectionDuration = section.getLessons().stream()
                                        .filter(lesson -> lesson.getLessonType() != null &&
                                                        "VIDEO".equals(lesson.getLessonType().getName()) &&
                                                        lesson.getContent() != null &&
                                                        lesson.getContent().getDuration() != null)
                                        .mapToInt(lesson -> lesson.getContent().getDuration())
                                        .sum();
                }

                return CourseDetailResponseDto.SectionSummary.builder()
                                .id(section.getId())
                                .title(section.getTitle())
                                .lessonCount(sectionLessonCount)
                                .quizCount(sectionQuizCount)
                                .duration(sectionDuration > 0 ? sectionDuration : null)
                                .lessons(lessonSummaries)
                                .build();
        }

        private CoursePublicResponseDto mapToCoursePublicResponse(Course course, Long enrollCount, Boolean isEnrolled) {
                // Get primary category (first one if multiple)
                List<CoursePublicResponseDto.CategorySummary> categorySummaries = new ArrayList<>();
                if (course.getCategories() != null && !course.getCategories().isEmpty()) {
                        categorySummaries = course.getCategories().stream()
                                        .map(cat -> CoursePublicResponseDto.CategorySummary.builder()
                                                        .id(cat.getId())
                                                        .name(cat.getName())
                                                        .build())
                                        .collect(Collectors.toList());
                }

                // Get instructor info
                CoursePublicResponseDto.InstructorSummary instructorSum = null;
                if (course.getInstructor() != null) {
                        instructorSum = CoursePublicResponseDto.InstructorSummary.builder()
                                        .id(course.getInstructor().getId())
                                        .name(course.getInstructor().getName())
                                        .avatar(course.getInstructor().getThumbnailUrl()) // Assuming thumbnail_url is
                                                                                          // avatar
                                        .build();
                }

                // Average rating
                Double averageRating = courseRepository.findAverageRatingByCourseId(course.getId()).orElse(0.0);
                averageRating = MathUtil.roundToTwoDecimals(averageRating); // Consistent rounding
                // Section Count
                Long sectionCount = sectionRepository.countSectionsByCourseId(course.getId());
                return CoursePublicResponseDto.builder()
                                .id(course.getId())
                                .title(course.getTitle())
                                .description(course.getDescription())
                                .price(course.getPrice())
                                .level(course.getLevel())
                                .thumbnailUrl(course.getThumbnailUrl())
                                .slug(course.getSlug())
                                .enrollCount(enrollCount)
                                .averageRating(averageRating)
                                .sectionCount(sectionCount)
                                .isEnrolled(isEnrolled)
                                .categories(categorySummaries)
                                .instructor(instructorSum)
                                .build();
        }

        private CourseAdminResponseDto mapToCourseAdminResponse(Course course, Long enrollCount) {
                // Get primary category (first one if multiple)
                List<CourseAdminResponseDto.CategoryInfo> categoryInfos = new ArrayList<>();
                if (course.getCategories() != null && !course.getCategories().isEmpty()) {
                        course.getCategories().forEach(cat -> {
                                CourseAdminResponseDto.CategoryInfo categoryInfo = CourseAdminResponseDto.CategoryInfo
                                                .builder()
                                                .id(cat.getId())
                                                .name(cat.getName())
                                                .build();
                                categoryInfos.add(categoryInfo);
                        });
                }

                // Get instructor info
                CourseAdminResponseDto.InstructorInfo instructorInfo = null;
                if (course.getInstructor() != null) {
                        instructorInfo = CourseAdminResponseDto.InstructorInfo.builder()
                                        .id(course.getInstructor().getId())
                                        .name(course.getInstructor().getName())
                                        .email(course.getInstructor().getEmail())
                                        .avatar(course.getInstructor().getThumbnailUrl()) // Assuming thumbnail_url is
                                                                                          // avatar
                                        .build();
                }

                // Average rating and rating count
                Double averageRating = courseRepository.findAverageRatingByCourseId(course.getId()).orElse(0.0);
                averageRating = MathUtil.roundToTwoDecimals(averageRating); // Consistent rounding
                Long ratingCount = courseRepository.countReviewsByCourseId(course.getId());

                // Section Count
                Long sectionCount = sectionRepository.countSectionsByCourseId(course.getId());

                return CourseAdminResponseDto.builder()
                                .id(course.getId())
                                .title(course.getTitle())
                                .description(course.getDescription())
                                .thumbnailUrl(course.getThumbnailUrl())
                                .instructor(instructorInfo)
                                .isApproved(course.getIsApproved())
                                .isPublished(course.getIsPublished())
                                .level(course.getLevel())
                                .price(course.getPrice())
                                .enrollmentCount(enrollCount)
                                .averageRating(averageRating)
                                .ratingCount(ratingCount)
                                .sectionCount(sectionCount)
                                .categories(categoryInfos)
                                .createdAt(course.getCreatedAt())
                                .updatedAt(course.getUpdatedAt())
                                .build();
        }

        @Override
        public ResponseEntity<ApiResponse<CourseApprovalResponseDto>> approveCourse(String courseId) {
                log.info("Approving course with ID: {}", courseId);

                // Find the course
                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Course not found with ID: " + courseId));

                // Check if course is already approved
                if (Boolean.TRUE.equals(course.getIsApproved())) {
                        throw new IllegalStateException("Course is already approved");
                }

                // Check if course is deleted
                if (Boolean.TRUE.equals(course.getIsDeleted())) {
                        throw new IllegalStateException("Cannot approve a deleted course");
                }

                // Approve the course
                course.setIsApproved(true);
                course.setUpdatedAt(LocalDateTime.now());

                Course savedCourse = courseRepository.save(course);

                log.info("Course {} approved successfully", courseId);

                // Create response DTO
                CourseApprovalResponseDto responseDto = CourseApprovalResponseDto.builder()
                                .id(savedCourse.getId())
                                .title(savedCourse.getTitle())
                                .isApproved(savedCourse.getIsApproved())
                                .approvedAt(savedCourse.getUpdatedAt())
                                .build();

                return ApiResponseUtil.success(responseDto, "Course approved successfully");
        }

        @Override
        public ResponseEntity<ApiResponse<List<SectionWithLessonsDto>>> getCourseDetailsForAdmin(String courseId) {
                log.info("Admin retrieving course details for course ID: {}", courseId);

                // Verify course exists
                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Course not found with ID: " + courseId));

                log.info("Found course: {} for admin review", course.getTitle());

                // Get sections with lessons for the course
                List<Section> sectionsWithLessons = courseRepository.findSectionsWithLessonsByCourseId(courseId);

                // Map to DTOs
                List<SectionWithLessonsDto> sectionDtos = sectionsWithLessons.stream()
                                .map(this::mapToSectionWithLessonsDto)
                                .collect(Collectors.toList());

                log.info("Retrieved {} sections for course {}", sectionDtos.size(), courseId);

                return ApiResponseUtil.success(sectionDtos, "Course sections retrieved successfully");
        }

        private SectionWithLessonsDto mapToSectionWithLessonsDto(Section section) {
                List<LessonDto> lessonDtos = null;

                if (section.getLessons() != null && !section.getLessons().isEmpty()) {
                        lessonDtos = section.getLessons().stream()
                                        .sorted((l1, l2) -> Integer.compare(
                                                        l1.getOrderIndex() != null ? l1.getOrderIndex() : 0,
                                                        l2.getOrderIndex() != null ? l2.getOrderIndex() : 0))
                                        .map(this::mapToLessonDto)
                                        .collect(Collectors.toList());
                }

                return SectionWithLessonsDto.builder()
                                .id(section.getId())
                                .title(section.getTitle())
                                .orderIndex(section.getOrderIndex())
                                .lessonCount(lessonDtos != null ? lessonDtos.size() : 0)
                                .lessons(lessonDtos)
                                .build();
        }

        private LessonDto mapToLessonDto(Lesson lesson) {
                String lessonType = lesson.getLessonType() != null ? lesson.getLessonType().getName() : "UNKNOWN";

                LessonDto.LessonDtoBuilder builder = LessonDto.builder()
                                .id(lesson.getId())
                                .title(lesson.getTitle())
                                .type(lessonType)
                                .order(lesson.getOrderIndex())
                                .isCompleted(false); // For admin view, completion status is not relevant

                // Handle video content
                if ("VIDEO".equals(lessonType) && lesson.getContent() != null) {
                        VideoDto videoDto = VideoDto.builder()
                                        .id(lesson.getContent().getId())
                                        .url(lesson.getContent().getUrl())
                                        .duration(lesson.getContent().getDuration())
                                        .build();
                        builder.video(videoDto);
                }

                // Handle quiz content
                if ("QUIZ".equals(lessonType)) {
                        List<QuizQuestion> questions = quizQuestionRepository.findQuestionsByLessonId(lesson.getId());
                        if (!questions.isEmpty()) {
                                List<QuizQuestionDto> questionDtos = questions.stream()
                                                .map(this::mapToQuizQuestionDto)
                                                .collect(Collectors.toList());

                                QuizDto quizDto = QuizDto.builder()
                                                .questions(questionDtos)
                                                .build();
                                builder.quiz(quizDto);
                        }
                }

                return builder.build();
        }

        private QuizQuestionDto mapToQuizQuestionDto(QuizQuestion question) {
                // Parse options from JSON string
                Map<String, String> options = null;
                try {
                        if (question.getOptions() != null) {
                                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                options = mapper.readValue(question.getOptions(),
                                                new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {
                                                });
                        }
                } catch (Exception e) {
                        log.warn("Failed to parse quiz question options for question {}: {}", question.getId(),
                                        e.getMessage());
                }

                return QuizQuestionDto.builder()
                                .id(question.getId())
                                .questionText(question.getQuestionText())
                                .options(options)
                                .correctAnswer(question.getCorrectAnswer())
                                .explanation(question.getExplanation())
                                .build();
        }
}
