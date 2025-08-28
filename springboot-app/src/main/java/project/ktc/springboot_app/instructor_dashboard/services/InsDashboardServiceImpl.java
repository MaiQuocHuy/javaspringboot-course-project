package project.ktc.springboot_app.instructor_dashboard.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cloudinary.http45.ApiUtils;
import com.mysql.cj.log.Log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.instructor_dashboard.dto.InsDashboardDto;
import project.ktc.springboot_app.instructor_dashboard.dto.StatisticItemDto;
import project.ktc.springboot_app.instructor_dashboard.interfaces.InsDashboardService;
import project.ktc.springboot_app.instructor_dashboard.repositories.InsDashboardRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsDashboardServiceImpl implements InsDashboardService {

  private final InsDashboardRepository insDashboardRepository;

  @Override
  public ResponseEntity<ApiResponse<InsDashboardDto>> getInsDashboardStatistics() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      User currentUser = (User) authentication.getPrincipal();
      String instructorId = currentUser.getId();

      // Get total instructor's courses and active courses
      Long totalCourses = insDashboardRepository.countTotalCoursesByInstructorId(instructorId);
      Long totalActiveCourses = insDashboardRepository.countTotalActiveCourses(instructorId);
      StatisticItemDto statisticItem = StatisticItemDto.builder()
          .title("Total Courses")
          .value(String.valueOf(totalCourses))
          .description(String.valueOf(totalActiveCourses))
          .build();

      // Calculate the percentage of enrolled students

      // Get total revenues

      // Calculate total revenues of current month

      // Get average rating
      InsDashboardDto dashboardDto = InsDashboardDto.builder()
          .courseStatistics(statisticItem)
          .build();
      return ApiResponseUtil.success(dashboardDto, "Ok");
    } catch (Exception e) {
      return ApiResponseUtil.internalServerError(e.getMessage());
    }
  }

}
