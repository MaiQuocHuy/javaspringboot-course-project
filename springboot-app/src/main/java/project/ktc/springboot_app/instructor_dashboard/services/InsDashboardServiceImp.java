package project.ktc.springboot_app.instructor_dashboard.services;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.cache.services.domain.InstructorStatisticsCacheService;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.course.repositories.InstructorCourseRepository;
import project.ktc.springboot_app.earning.repositories.InstructorEarningRepository;
import project.ktc.springboot_app.instructor_dashboard.dto.InsDashboardDto;
import project.ktc.springboot_app.instructor_dashboard.dto.StatisticItemDto;
import project.ktc.springboot_app.instructor_dashboard.interfaces.InsDashboardService;
import project.ktc.springboot_app.instructor_student.repositories.InstructorStudentRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsDashboardServiceImp implements InsDashboardService {

  private final InstructorStudentRepository instructorStudentRepository;
  private final InstructorCourseRepository instructorCourseRepository;
  private final InstructorEarningRepository instructorEarningRepository;
  private final CourseRepository courseRepository;
  private final InstructorStatisticsCacheService instructorStatisticsCacheService;

  private StatisticItemDto courseStatistics(String instructorId) {
    // Get total instructor's courses and active courses
    Long totalCourses = instructorCourseRepository.countTotalCoursesByInstructorId(instructorId);
    Long totalActiveCourses = instructorCourseRepository.countTotalActiveCoursesByInstructorId(instructorId);
    String courseStatisticsDescription;
    if (totalActiveCourses > 0) {
      if (totalActiveCourses == 1) {
        courseStatisticsDescription = totalActiveCourses + " active course";
      } else {
        courseStatisticsDescription = totalActiveCourses + " active courses";
      }
    } else {
      courseStatisticsDescription = "No active courses";
    }
    StatisticItemDto courseStatistics = StatisticItemDto.builder()
        .title("Total Courses")
        .value(String.valueOf(totalCourses))
        .description(courseStatisticsDescription)
        .build();

    return courseStatistics;
  }

  private StatisticItemDto studentStatistics(String instructorId) {
    // Get total enrolled students
    List<String> enrolledStudentIds = instructorStudentRepository.countTotalEnrolledStudents(instructorId);
    // Get current and last month enrolled students
    Long totalCurrentEnrolledStudents = instructorStudentRepository.countStudentsEnrolledByMonth(
        LocalDate.now().getYear(),
        LocalDate.now().getMonthValue());
    Long lastMonthEnrolledStudents = instructorStudentRepository.countStudentsEnrolledByMonth(
        LocalDate.now().getYear(),
        LocalDate.now().minusMonths(1).getMonthValue());
    BigDecimal growthRate;
    if (lastMonthEnrolledStudents != 0) {
      growthRate = (BigDecimal.valueOf(totalCurrentEnrolledStudents)
          .divide(BigDecimal.valueOf(lastMonthEnrolledStudents), 2, RoundingMode.HALF_UP))
          .multiply(BigDecimal.valueOf(100));
    } else {
      growthRate = BigDecimal.valueOf(100);
    }

    String studentStatisticsDescription = growthRate + "% from last month";
    if (growthRate.compareTo(BigDecimal.ZERO) > 0) {
      studentStatisticsDescription = "+" + studentStatisticsDescription;
    } else if (growthRate.compareTo(BigDecimal.ZERO) < 0) {
      studentStatisticsDescription = "-" + studentStatisticsDescription;
    }

    StatisticItemDto studentStatistics = StatisticItemDto.builder()
        .title("Total Students")
        .value(String.valueOf(enrolledStudentIds.size()))
        .description(studentStatisticsDescription)
        .build();

    return studentStatistics;
  }

  private StatisticItemDto revenueStatistics(String instructorId) {
    BigDecimal totalRevenues = instructorEarningRepository.getTotalEarningsByInstructor(instructorId);
    BigDecimal currentMonthRevenues = instructorEarningRepository.getTotalEarningsByMonth(instructorId,
        LocalDate.now().getYear(),
        LocalDate.now().getMonthValue());
    String revenueStatisticsDescription = " This month: $" + currentMonthRevenues;

    StatisticItemDto revenueStatistics = StatisticItemDto.builder()
        .title("Total Revenues")
        .value(String.valueOf(totalRevenues))
        .description(revenueStatisticsDescription)
        .build();

    return revenueStatistics;
  }

  private StatisticItemDto ratingStatistics(String instructorId) {
    Optional<Double> averageRating = courseRepository.findAverageRatingByInstructorId(instructorId);

    StatisticItemDto ratingStatistics = StatisticItemDto.builder()
        .title("Avg Rating")
        .value(averageRating.map(String::valueOf).orElse("0"))
        .description("Across all courses")
        .build();

    return ratingStatistics;
  }

  @Override
  public ResponseEntity<ApiResponse<InsDashboardDto>> getInsDashboardStatistics() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      User currentUser = (User) authentication.getPrincipal();
      String instructorId = currentUser.getId();

      log.debug("Fetching instructor dashboard statistics for instructor: {}", instructorId);

      // Try to get from cache first
      InsDashboardDto cachedStatistics = instructorStatisticsCacheService.getInstructorStatistics(instructorId);
      if (cachedStatistics != null) {
        log.debug("Returning cached instructor statistics for instructor: {}", instructorId);
        return ApiResponseUtil.success(cachedStatistics, "Get instructor dashboard statistics successfully (cached)");
      }

      // Cache miss - fetch from database
      log.debug("Cache miss - fetching instructor statistics from database for instructor: {}", instructorId);

      // Get course statistics
      StatisticItemDto courseStatistics = courseStatistics(instructorId);
      // Get student statistics
      StatisticItemDto studentStatistics = studentStatistics(instructorId);
      // Get revenue statistics
      StatisticItemDto revenueStatistics = revenueStatistics(instructorId);
      // Get average rating
      StatisticItemDto ratingStatistics = ratingStatistics(instructorId);

      InsDashboardDto dashboardDto = InsDashboardDto.builder()
          .courseStatistics(courseStatistics)
          .studentStatistics(studentStatistics)
          .revenueStatistics(revenueStatistics)
          .ratingStatistics(ratingStatistics)
          .build();

      // Store in cache for future requests
      instructorStatisticsCacheService.storeInstructorStatistics(instructorId, dashboardDto);
      log.debug("Stored instructor statistics in cache for instructor: {}", instructorId);

      return ApiResponseUtil.success(dashboardDto, "Get instructor dashboard statistics successfully");
    } catch (Exception e) {
      log.error("Error fetching instructor dashboard statistics", e);
      return ApiResponseUtil.internalServerError(e.getMessage());
    }
  }

}
