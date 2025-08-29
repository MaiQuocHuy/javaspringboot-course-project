package project.ktc.springboot_app.instructor_student.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import project.ktc.springboot_app.common.dto.PaginatedResponse.PageInfo;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;
import project.ktc.springboot_app.instructor_dashboard.dto.StatisticItemDto;
import project.ktc.springboot_app.instructor_student.dto.EnrolledCourse;
import project.ktc.springboot_app.instructor_student.dto.InstructorStudentDto;
import project.ktc.springboot_app.instructor_student.interfaces.InstructorStudentService;
import project.ktc.springboot_app.instructor_student.repositories.InstructorStudentRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstructorStudentServiceImpl implements InstructorStudentService {
  private final InstructorStudentRepository instructorStudentRepository;
  private final EnrollmentRepository enrollmentRepository;

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
      List<String> enrolledStudentIds = instructorStudentRepository.getEnrolledStudentsByInstructorId(instructorId);
      // Get detailed information for each student
      List<InstructorStudentDto> studentsList = new ArrayList<>();
      // Page<InstructorStudentDto> studentsList = new ArrayList<>();
      for (String studentId : enrolledStudentIds) {
        List<Object[]> studentInfo = instructorStudentRepository.getStudentById(studentId);
        if (studentInfo != null && !studentInfo.isEmpty()) {
          // Set information for each student
          InstructorStudentDto student = new InstructorStudentDto();
          for (Object[] row : studentInfo) {
            student.setId((String) row[0]);
            student.setName((String) row[1]);
            student.setEmail((String) row[2]);
            student.setThumbnailUrl((String) row[3]);
          }

          // Get student's enrolled courses
          List<Object[]> studentCourses = instructorStudentRepository.getStudentCourses(studentId);
          if (studentCourses != null && !studentCourses.isEmpty()) {
            List<EnrolledCourse> enrolledCourses = new ArrayList<>();
            for (Object[] course : studentCourses) {
              String id = (String) course[0];
              String title = (String) course[1];
              Double progress = calculateProgress(studentId, id);
              EnrolledCourse enrolledCourse = EnrolledCourse.builder()
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
}
