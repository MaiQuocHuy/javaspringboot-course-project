package project.ktc.springboot_app.earning.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.earning.dto.EarningDetailResponseDto;
import project.ktc.springboot_app.earning.dto.EarningsWithSummaryDto;
import project.ktc.springboot_app.earning.dto.MonthlyEarningsDto;
import project.ktc.springboot_app.earning.services.InstructorEarningServiceImp;

@RestController
@RequestMapping("/api/instructor/earnings")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('INSTRUCTOR')")
@Tag(name = "Instructor Earnings API", description = "Endpoints for managing instructor earnings")
public class InstructorEarningController {

  private final InstructorEarningServiceImp instructorEarningService;

  @GetMapping
  public ResponseEntity<ApiResponse<EarningsWithSummaryDto>> getEarnings(
      @RequestParam(required = false) String courseId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateTo,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt,asc") String sort) {

    log.info(
        "GET /api/instructor/earnings - status: {}, courseId: {}, dateFrom: {}, dateTo: {}, page: {}, size: {}, sort: {}",
        courseId,
        dateFrom,
        dateTo,
        page,
        size,
        sort);

    // Validate page and size parameters
    if (page < 0) {
      page = 0;
    }
    if (size < 1 || size > 100) {
      size = 10;
    }

    // Parse sort parameter
    Pageable pageable = createPageable(page, size, sort);

    return instructorEarningService.getEarnings(courseId, dateFrom, dateTo, pageable);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<EarningDetailResponseDto>> getEarningDetails(
      @PathVariable String id) {

    log.info("GET /api/instructor/earnings/{} - Fetching earning details", id);

    return instructorEarningService.getEarningDetails(id);
  }

  @GetMapping("/recent-revenues")
  public ResponseEntity<ApiResponse<List<MonthlyEarningsDto>>> getRecentRevenues() {
    return instructorEarningService.getRecentRevenues();
  }

  private Pageable createPageable(int page, int size, String sort) {
    try {
      String[] sortParams = sort.split(",");
      String property = sortParams[0];
      Sort.Direction direction =
          sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
              ? Sort.Direction.DESC
              : Sort.Direction.ASC;

      return PageRequest.of(page, size, Sort.by(direction, property));
    } catch (Exception e) {
      log.warn("Invalid sort parameter: {}, using default", sort);
      return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
  }
}
