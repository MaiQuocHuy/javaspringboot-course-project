package project.ktc.springboot_app.instructor_student.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.category.entity.Category;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse.PageInfo;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.dto.common.BaseCourseResponseDto.CategoryInfo;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;
import project.ktc.springboot_app.instructor_student.dto.EnrolledCourses;
import project.ktc.springboot_app.instructor_student.dto.EnrolledCoursesDetails;
import project.ktc.springboot_app.instructor_student.dto.InstructorStudentDetailsDto;
import project.ktc.springboot_app.instructor_student.dto.InstructorStudentDto;
import project.ktc.springboot_app.instructor_student.interfaces.InstructorStudentService;
import project.ktc.springboot_app.instructor_student.repositories.InstructorStudentRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.utils.SecurityUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstructorStudentServiceImpl implements InstructorStudentService {
  private final InstructorStudentRepository instructorStudentRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final UserRepository userRepository;
  private final CourseRepository courseRepository;

  private List<String> getEnrolledStudentIds(String instructorId) {
    return instructorStudentRepository.getEnrolledStudentsByInstructorId(instructorId);
  }

  private Double calculateProgress(String userId, String courseId) {
    try {
      Long completedLessons = enrollmentRepository.countCompletedLessonsByUserAndCourse(userId, courseId);
      Long totalLessons = enrollmentRepository.countTotalLessonsByCourse(courseId);

      if (totalLessons == null || totalLessons == 0) {
        return 0.0;
      }

      double progress = (double) completedLessons / totalLessons;
      return BigDecimal.valueOf(progress)
          .setScale(2, RoundingMode.HALF_UP)
          .doubleValue();
    } catch (Exception e) {
      log.warn("Failed to calculate progress for user {} and course {}: {}", userId, courseId, e.getMessage());
      return 0.0;
    }
  }

  public <T> PaginatedResponse<T> getPaginatedList(List<T> fullList, Pageable pageable) {
    PagedListHolder<T> pagedListHolder = new PagedListHolder<>(fullList);
    pagedListHolder.setPageSize(pageable.getPageSize());
    pagedListHolder.setPage(pageable.getPageNumber());

    List<T> pageContent = pagedListHolder.getPageList();
    PageInfo pageInfo = PageInfo.builder()
        .number(pagedListHolder.getPage())
        .size(pagedListHolder.getPageSize())
        .totalPages(pagedListHolder.getPageCount())
        .totalElements(fullList.size())
        .first(pagedListHolder.isFirstPage())
        .last(pagedListHolder.isLastPage())
        .build();
    return new PaginatedResponse<>(pageContent, pageInfo);
  }

  @Override
  public ResponseEntity<ApiResponse<PaginatedResponse<InstructorStudentDto>>> getEnrolledStudents(Pageable pageable) {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      User currentUser = (User) authentication.getPrincipal();
      String instructorId = currentUser.getId();

      // Get total enrolled students
      List<String> enrolledStudentIds = getEnrolledStudentIds(instructorId);
      // Get detailed information for each student
      List<InstructorStudentDto> studentsList = new ArrayList<>();
      // Page<InstructorStudentDto> studentsList = new ArrayList<>();
      for (String studentId : enrolledStudentIds) {
        List<Object[]> studentInfo = instructorStudentRepository.getStudentById(studentId);
        if (studentInfo != null && !studentInfo.isEmpty()) {
          // Set information for each student
          InstructorStudentDto student = InstructorStudentDto.builder()
              .id((String) studentInfo.get(0)[0])
              .name((String) studentInfo.get(0)[1])
              .email((String) studentInfo.get(0)[2])
              .thumbnailUrl((String) studentInfo.get(0)[3])
              .build();

          // Get student's enrolled courses
          List<Object[]> studentCourses = instructorStudentRepository.getStudentCourses(studentId);
          if (studentCourses != null && !studentCourses.isEmpty()) {
            List<EnrolledCourses> enrolledCourses = new ArrayList<>();
            for (Object[] course : studentCourses) {
              String id = (String) course[0];
              String title = (String) course[1];
              Double progress = calculateProgress(studentId, id);
              EnrolledCourses enrolledCourse = EnrolledCourses.builder()
                  .courseId(id)
                  .title(title)
                  .progress(progress)
                  .build();
              enrolledCourses.add(enrolledCourse);
            }
            student.setEnrolledCourses(enrolledCourses);
          }
          studentsList.add(student);
        }
      }
      PaginatedResponse<InstructorStudentDto> pagedStudentsList = getPaginatedList(studentsList, pageable);
      return ApiResponseUtil.success(pagedStudentsList, "Get enrolled students successfully");
    } catch (Exception e) {
      return ApiResponseUtil.internalServerError(e.getMessage());
    }
  }

  @Override
  public ResponseEntity<ApiResponse<InstructorStudentDetailsDto>> getEnrolledStudentDetails(String studentId) {
    try {
      // Validate student ID
      if (studentId == null || studentId.trim().isEmpty()) {
        return ApiResponseUtil.badRequest("Invalid student ID");
      }

      // Check if student exists
      Optional<User> userOpt = userRepository.findById(studentId);
      if (!userOpt.isPresent()) {
        return ApiResponseUtil.notFound("Student not found");
      } else {
        // Check if student has already enrolled in any courses
        String instructorId = SecurityUtil.getCurrentUserId();
        List<String> enrolledStudentIds = getEnrolledStudentIds(instructorId);
        if (!enrolledStudentIds.contains(studentId)) {
          return ApiResponseUtil.notFound("The student has not enrolled in any of your courses");
        } else {
          // Get student details
          User student = userOpt.get();
          InstructorStudentDetailsDto studentDetails = InstructorStudentDetailsDto.builder()
              .id(student.getId())
              .name(student.getName())
              .email(student.getEmail())
              .thumbnailUrl(student.getThumbnailUrl())
              .build();

          // Get student's enrolled courses
          List<Object[]> enrolledCoursesData = instructorStudentRepository
              .getStudentCoursesDetails(student.getId());
          List<EnrolledCoursesDetails> enrolledCoursesDetails = new ArrayList<>();
          Set<String> processedCourseIds = new HashSet<>();

          for (Object[] courseData : enrolledCoursesData) {
            Course course = (Course) courseData[0];

            // Skip if already processed this course
            if (!processedCourseIds.add(course.getId())) {
              continue;
            }
            LocalDateTime enrolledAt = (LocalDateTime) courseData[1];
            String courseId = course.getId();

            List<CategoryInfo> categoryInfos = new ArrayList<>();
            if (course.getCategories() != null && !course.getCategories().isEmpty()) {
              for (Category category : course.getCategories()) {
                CategoryInfo categoryInfo = CategoryInfo.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .build();
                categoryInfos.add(categoryInfo);
              }
            }

            EnrolledCoursesDetails details = EnrolledCoursesDetails.builder()
                .courseId(courseId)
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .thumbnailUrl(course.getThumbnailUrl())
                .level(course.getLevel().name())
                .categories(categoryInfos)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .enrolledAt(enrolledAt)
                .build();

            // Calculate student's progress
            Double progress = calculateProgress(studentId, courseId);
            details.setProgress(progress);

            // Get course's average rating
            Optional<Double> avgRatingOpt = courseRepository.findAverageRatingByCourseId(courseId);
            if (avgRatingOpt.isPresent()) {
              details.setAverageRating(avgRatingOpt.get());
            }
            // Get course's total ratings
            Long totalRatings = courseRepository.countReviewsByCourseId(courseId);
            details.setTotalRating(totalRatings);

            enrolledCoursesDetails.add(details);
          }
          studentDetails.setEnrolledCourses(enrolledCoursesDetails);

          return ApiResponseUtil.success(studentDetails, "Get enrolled student details successfully");
        }

      }
    } catch (Exception e) {
      return ApiResponseUtil.internalServerError(e.getMessage());
    }
  }
}
